package com.example.tsuonspot

import android.graphics.Bitmap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.toColorLong
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.alghoritms.Data.Point
import com.example.alghoritms.clustering.EuclidianDistance
import com.example.alghoritms.clustering.KMeans
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sqrt
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColor
import com.example.alghoritms.clustering.PedestrianDistance

val CLUSTER_COLORS = listOf(
    Color(0x550072BC),
    Color(0x55E74C3C),
    Color(0x5527AE60),
    Color(0x55F39C12),
    Color(0x558E44AD),
    Color(0x5516A085),
    Color(0x55E67E22),
    Color(0x552980B9),
)

val CLUSTER_BORDER_COLORS = listOf(
    Color(0xFF0072BC),
    Color(0xFFE74C3C),
    Color(0xFF27AE60),
    Color(0xFFF39C12),
    Color(0xFF8E44AD),
    Color(0xFF16A085),
    Color(0xFFE67E22),
    Color(0xFF2980B9),
)

data class ClusterResult(
    val clusterIndex: Int,
    val pois: List<PointOfInterest>,
    val centroid: Pair<Float, Float>
)

suspend fun generateVoronoiBitmap(
    clusters: List<ClusterResult>,
    gridWidth: Int,
    gridHeight: Int
): Bitmap = withContext(Dispatchers.Default) {
    val pixels = IntArray(gridWidth * gridHeight)

    val clusterColorInts = CLUSTER_COLORS.map { it.toArgb() }

    for (y in 0 until gridHeight) {
        for (x in 0 until gridWidth) {
            var minDistSq = Float.MAX_VALUE
            var clusterIdx = 0

            for (i in clusters.indices) {
                val c = clusters[i]
                val dx = x - c.centroid.first
                val dy = y - c.centroid.second
                val distSq = dx * dx + dy * dy
                if (distSq < minDistSq) {
                    minDistSq = distSq
                    clusterIdx = i
                }
            }

            pixels[y * gridWidth + x] = clusterColorInts[clusterIdx % clusterColorInts.size]
        }
    }

    val bitmap = createBitmap(gridWidth, gridHeight)
    bitmap.setPixels(pixels, 0, gridWidth, 0, 0, gridWidth, gridHeight)

    return@withContext bitmap
}

suspend fun computeClusters(
    pois: List<PointOfInterest>,
    usePedestrian: Boolean,
    gridAStar: com.example.alghoritms.pathFind.GridAStar?
): List<ClusterResult> = withContext(Dispatchers.Default) {

    val indexedPoints = pois.mapIndexed { idx, poi ->
        Point(poi.gridX.toDouble() + idx * 1e-9, poi.gridY.toDouble())
    }

    val metric = if (usePedestrian && gridAStar != null) {
        val cache = mutableMapOf<Pair<Int, Int>, Double>()
        for (i in pois.indices) {
            val startCell = gridAStar.findNearestWalkable(pois[i].gridX, pois[i].gridY)
            for (j in i until pois.size) {
                if (i == j) { cache[i to j] = 0.0; continue }
                val endCell = gridAStar.findNearestWalkable(pois[j].gridX, pois[j].gridY)
                val dist = if (startCell != null && endCell != null) {
                    try {
                        val (_, cost) = gridAStar.findPath(startCell, endCell)
                        cost
                    } catch (e: Exception) {
                        euclideanPoi(pois[i], pois[j])
                    }
                } else {
                    euclideanPoi(pois[i], pois[j])
                }
                cache[i to j] = dist
            }
        }
        PedestrianDistance(cache, indexedPoints)
    } else {
        EuclidianDistance()
    }

    val maxK = minOf(8, pois.size - 1)
    val kFinder = KMeans(
        clustersQuantity = 2,
        distanceMetric = metric,
        normalize = false
    )
    val k = kFinder.findOptimalClusters(indexedPoints, maxK)

    val kMeans = KMeans(
        clustersQuantity = k,
        distanceMetric = metric,
        normalize = false
    )
    val clusters = kMeans.findClusters(indexedPoints)

    clusters.mapIndexed { idx, cluster ->
        val clusterPois = cluster.points.mapNotNull { p ->
            val originalIdx = indexedPoints.indexOfFirst {
                kotlin.math.abs(it.x - p.x) < 1e-6 && it.y == p.y
            }
            pois.getOrNull(originalIdx)
        }
        val cx = cluster.centroid?.x?.toFloat()
            ?: clusterPois.map { it.gridX.toFloat() }.average().toFloat()
        val cy = cluster.centroid?.y?.toFloat()
            ?: clusterPois.map { it.gridY.toFloat() }.average().toFloat()
        ClusterResult(idx, clusterPois, cx to cy)
    }
}

private fun euclideanPoi(a: PointOfInterest, b: PointOfInterest): Double {
    val dx = (a.gridX - b.gridX).toDouble()
    val dy = (a.gridY - b.gridY).toDouble()
    return sqrt(dx * dx + dy * dy)
}

@Composable
fun VoronoiClusterOverlay(
    clusters: List<ClusterResult>,
    camera: MapCamera,
    voronoiBitmap: Bitmap?
) {
    if (voronoiBitmap == null) return

    val alpha by animateFloatAsState(
        targetValue = 0.6f,
        animationSpec = tween(600),
        label = "voronoi_alpha"
    )

    Image(
        bitmap = voronoiBitmap.asImageBitmap(),
        contentDescription = null,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = camera.scale * camera.cellSize
                scaleY = camera.scale * camera.cellSize
                translationX = camera.offsetX
                translationY = camera.offsetY
                transformOrigin = TransformOrigin(0f, 0f)
                this.alpha = alpha
            },
        contentScale = ContentScale.None,
        alignment = Alignment.TopStart
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        clusters.forEachIndexed { idx, c ->
            val sx = c.centroid.first * camera.scale * camera.cellSize + camera.offsetX
            val sy = c.centroid.second * camera.scale * camera.cellSize + camera.offsetY
            val border = CLUSTER_BORDER_COLORS.getOrElse(idx) { CLUSTER_BORDER_COLORS[0] }

            drawCircle(Color.White, radius = 8f, center = Offset(sx, sy))
            drawCircle(border, radius = 8f, center = Offset(sx, sy),
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
        }
    }
}

private fun DrawScope.drawVoronoi(
    clusters: List<ClusterResult>,
    camera: MapCamera,
    canvasW: Float,
    canvasH: Float
) {
    if (clusters.isEmpty()) return

    val blockSize = 6f

    val cols = (canvasW / blockSize).toInt() + 1
    val rows = (canvasH / blockSize).toInt() + 1

    val screenCentroids = clusters.map { c ->
        val sx = c.centroid.first * camera.scale * camera.cellSize + camera.offsetX
        val sy = c.centroid.second * camera.scale * camera.cellSize + camera.offsetY
        sx to sy
    }

    for (row in 0..rows) {
        for (col in 0..cols) {
            val px = col * blockSize
            val py = row * blockSize

            var minDist = Float.MAX_VALUE
            var clusterIdx = 0

            screenCentroids.forEachIndexed { idx, (cx, cy) ->
                val dx = px - cx
                val dy = py - cy
                val dist = dx * dx + dy * dy
                if (dist < minDist) {
                    minDist = dist
                    clusterIdx = idx
                }
            }

            val color = CLUSTER_COLORS.getOrElse(clusterIdx) { CLUSTER_COLORS[0] }
            drawRect(
                color = color,
                topLeft = Offset(px, py),
                size = androidx.compose.ui.geometry.Size(blockSize, blockSize)
            )
        }
    }

    screenCentroids.forEachIndexed { idx, (cx, cy) ->
        val border = CLUSTER_BORDER_COLORS.getOrElse(idx) { CLUSTER_BORDER_COLORS[0] }
        drawCircle(color = Color.White, radius = 10f, center = Offset(cx, cy))
        drawCircle(color = border, radius = 10f, center = Offset(cx, cy),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f))
    }
}

private fun Modifier.graphicsAlpha(alpha: Float): Modifier =
    this.alpha(alpha)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClusterInfoSheet(
    clusters: List<ClusterResult>,
    onDismiss: () -> Unit
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.Transparent,
        dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Transparent) },
        contentWindowInsets = { WindowInsets(top = 80.dp) }
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(50.dp, 5.dp)
                        .background(tsuBlue.copy(alpha = 0.3f), CircleShape)
                )

                Text(
                    text = "Зоны еды",
                    fontSize = 20.sp,
                    fontFamily = standardTSUFont,
                    fontWeight = FontWeight.Bold,
                    color = tsuBlue,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(clusters) { idx, cluster ->
                        ClusterCard(idx, cluster)
                    }
                    item { Spacer(Modifier.navigationBarsPadding()) }
                }
            }
        }
    }
}

@Composable
private fun ClusterCard(idx: Int, cluster: ClusterResult) {
    val tsuBlue = Color(standardTSUColor.toColorInt())
    val borderColor = CLUSTER_BORDER_COLORS.getOrElse(idx) { tsuBlue }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(2.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(borderColor, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Зона ${idx + 1}",
                    fontSize = 15.sp,
                    fontFamily = standardTSUFont,
                    fontWeight = FontWeight.Bold,
                    color = borderColor
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "${cluster.pois.size} мест",
                    fontSize = 12.sp,
                    fontFamily = standardTSUFont,
                    color = tsuBlue.copy(alpha = 0.6f)
                )
            }
            Spacer(Modifier.height(8.dp))
            cluster.pois.forEach { poi ->
                Row(
                    modifier = Modifier.padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(borderColor.copy(alpha = 0.6f), CircleShape)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = poi.title,
                        fontSize = 13.sp,
                        fontFamily = standardTSUFont,
                        color = tsuBlue
                    )
                }
            }
        }
    }
}

@Composable
fun ClusterControlsHud(
    usePedestrian: Boolean,
    isLoading: Boolean,
    onToggleMetric: () -> Unit,
    onShowInfo: () -> Unit,
    onExit: () -> Unit
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())

    val thumbOffset by animateFloatAsState(
        targetValue = if (usePedestrian) 1f else 0f,
        animationSpec = tween(600),
        label = "metric_toggle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(7.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.5f)
                        .offset(x = (thumbOffset * 100).dp * 0f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Transparent)
                )

                Row(modifier = Modifier.fillMaxSize()) {
                    MetricButton(
                        label = "Евклидово",
                        isActive = !usePedestrian,
                        tsuBlue = tsuBlue,
                        modifier = Modifier.weight(1f)
                    ) { if (!isLoading && usePedestrian) onToggleMetric() }

                    MetricButton(
                        label = "Пешеходное",
                        isActive = usePedestrian,
                        tsuBlue = tsuBlue,
                        modifier = Modifier.weight(1f)
                    ) { if (!isLoading && !usePedestrian) onToggleMetric() }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = Color.White)
                    ) { onShowInfo() },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(7.dp),
                colors = CardDefaults.cardColors(containerColor = tsuBlue)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            "Список зон",
                            fontSize = 15.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Card(
                modifier = Modifier
                    .size(56.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(color = tsuBlue)
                    ) { onExit() },
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(7.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "✕",
                        fontSize = 20.sp,
                        fontFamily = standardTSUFont,
                        fontWeight = FontWeight.Bold,
                        color = tsuBlue
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun MetricButton(
    label: String,
    isActive: Boolean,
    tsuBlue: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) tsuBlue else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = tsuBlue)
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontFamily = standardTSUFont,
            fontWeight = FontWeight.Bold,
            color = if (isActive) Color.White else tsuBlue.copy(alpha = 0.6f)
        )
    }
}