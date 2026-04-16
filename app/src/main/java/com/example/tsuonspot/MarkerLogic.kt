package com.example.tsuonspot

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alghoritms.Data.GridCell
import com.example.alghoritms.pathFind.GridAStar
import com.example.tsuonspot.AntAlghoritm.RouteFinder

data class MapCamera(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val cellSize: Float
)

val LocalMapCamera = compositionLocalOf { MapCamera(0f, 0f, 0f, 0f) }

internal fun MapCamera.toScreenX(gridX: Int) = (gridX * scale * cellSize + offsetX).roundToInt()
internal fun MapCamera.toScreenY(gridY: Int) = (gridY * scale * cellSize + offsetY).roundToInt()

data class MapState(
    val scale: Float = 3.5f,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val startPoint: Pair<Int, Int>? = null,
    val endPoint: Pair<Int, Int>? = null,
    val pathPoints: List<GridCell>? = null,
    val pathError: Boolean = false,

    val selectedPoi: PointOfInterest? = null,
    val isWaitingForPoiStart: Boolean = false,

    val showClusters: Boolean = false,
    val clusters: List<ClusterResult> = emptyList(),
    val clusterMetricIsPedestrian: Boolean = false,
    val isClustering: Boolean = false,
    val showClusterInfoSheet: Boolean = false,
    val voronoiBitmap: Bitmap? = null,

    val mapLayer: MapLayer = MapLayer.FOOD,
    val selectedAttraction: Attraction? = null,

    val acoPath: List<GridCell>? = null,
    val acoOrderedPoints: List<GridCell>? = null,
    val isWaitingForAttractionStart: Boolean = false,
    val pendingAttractionRoute: List<Attraction> = emptyList()
)

class MapViewModel(application: Application) : AndroidViewModel(application) {
    private val _cellSize = MutableStateFlow(3f)
    val cellSize: StateFlow<Float> = _cellSize.asStateFlow()
    fun updateCellSize(newCellSize: Float) {
        if (newCellSize > 0f) _cellSize.value = newCellSize
    }
    private val panSpeed: Float = 1f
    private val minScale = 1.4f
    private val maxScale = 10f
    private val _mapState = MutableStateFlow(MapState())
    val mapState: StateFlow<MapState> = _mapState.asStateFlow()
    private var gridAStar: GridAStar? = null
    private val _isMatrixLoading = MutableStateFlow(true)
    val isMatrixLoading: StateFlow<Boolean> = _isMatrixLoading.asStateFlow()

    init {
        loadMatrix()
    }

    fun setMapLayer(layer: MapLayer) {
        _mapState.update { it.copy(mapLayer = layer) }
    }

    fun clearMap() {
        _mapState.update {
            it.copy(
                startPoint = null,
                endPoint = null,
                pathPoints = null,
                pathError = false
            )
        }
    }

    fun onMapDoubleTap(x: Float, y: Float) {
        val current = _mapState.value
        val gridX = ((x - current.offsetX) / (current.scale * _cellSize.value)).toInt()
        val gridY = ((y - current.offsetY) / (current.scale * _cellSize.value)).toInt()
        val hitRadius = 20
        val clickedOnStart = current.startPoint?.let {
            kotlin.math.abs(it.first - gridX) < hitRadius && kotlin.math.abs(it.second - gridY) < hitRadius
        } ?: false
        val clickedOnEnd = current.endPoint?.let {
            kotlin.math.abs(it.first - gridX) < hitRadius && kotlin.math.abs(it.second - gridY) < hitRadius
        } ?: false
        if (clickedOnStart || clickedOnEnd) {
            clearMap()
            clearAcoRoute()
        }
    }

    private fun loadMatrix() {
        viewModelScope.launch {
            gridAStar = withContext(Dispatchers.IO) {
                try {
                    GridAStar.loadFromAssets(getApplication(), "output_matrix.txt")
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
            _isMatrixLoading.value = false
        }
    }

    fun onPoiClick(poi: PointOfInterest) {
        _mapState.update { it.copy(selectedPoi = poi, isWaitingForPoiStart = false) }
    }

    fun onPoiDismiss() {
        _mapState.update { it.copy(selectedPoi = null, isWaitingForPoiStart = false) }
    }

    fun onAttractionClick(attraction: Attraction) {
        _mapState.update { it.copy(selectedAttraction = attraction) }
    }

    fun onAttractionDismiss() {
        _mapState.update { it.copy(selectedAttraction = null) }
    }

    fun onBuildRouteToPoiRequested() {
        val poi = _mapState.value.selectedPoi ?: return
        _mapState.update { current ->
            current.copy(
                endPoint = Pair(poi.gridX, poi.gridY),
                startPoint = null,
                pathPoints = null,
                pathError = false,
                isWaitingForPoiStart = true
            )
        }
    }

    fun onBuildRouteToAttractionRequested() {
        val attraction = _mapState.value.selectedAttraction ?: return
        _mapState.update { current ->
            current.copy(
                endPoint = Pair(attraction.gridX, attraction.gridY),
                startPoint = null,
                pathPoints = null,
                pathError = false,
                isWaitingForPoiStart = true
            )
        }
    }

    fun onBuildAttractionRoute(selected: List<Attraction>) {
        _mapState.update {
            it.copy(
                isWaitingForAttractionStart = true,
                pendingAttractionRoute = selected,
                acoPath = null,
                acoOrderedPoints = null,
                startPoint = null
            )
        }
    }

    fun onAttractionStartPointSet(x: Float, y: Float) {
        val current = _mapState.value
        val gridX = ((x - current.offsetX) / (current.scale * _cellSize.value)).toInt()
        val gridY = ((y - current.offsetY) / (current.scale * _cellSize.value)).toInt()
        val userCell = GridCell(gridX, gridY, false)

        _mapState.update { it.copy(
            startPoint = Pair(gridX, gridY),
            isWaitingForAttractionStart = false
        )}

        val astar = gridAStar ?: return
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                try {
                    val attractionCells = current.pendingAttractionRoute.map {
                        GridCell(it.gridX, it.gridY, true)
                    }
                    val finder = RouteFinder(emptyArray(), astar)
                    finder.fundOptimalRoute(userCell, attractionCells)
                } catch (e: Exception) {
                    null
                }
            }

            _mapState.update { it.copy(
                acoPath = result?.fullPath,
                acoOrderedPoints = result?.orderedPoints
            )}
        }
    }

    fun clearAcoRoute() {
        _mapState.update { it.copy(
            acoPath = null,
            acoOrderedPoints = null,
            startPoint = null,
            endPoint = null,
            pendingAttractionRoute = emptyList()
        )}
    }

    fun onMapClick(x: Float, y: Float) {
        val current = _mapState.value
        if (current.showClusters) return

        val camera = MapCamera(
            scale = current.scale,
            offsetX = current.offsetX,
            offsetY = current.offsetY,
            cellSize = _cellSize.value
        )
        if (current.isWaitingForAttractionStart) {
            onAttractionStartPointSet(x, y)
            return
        }
        if (current.mapLayer == MapLayer.FOOD) {
            val tappedPoi = findTappedPoi(x, y, pointsOfInterest, camera)
            if (tappedPoi != null) {
                onPoiClick(tappedPoi)
                return
            }
        }

        if (current.mapLayer == MapLayer.ATTRACTIONS) {
            val tappedAttraction = findTappedAttraction(x, y, attractions, camera)
            if (tappedAttraction != null) {
                onAttractionClick(tappedAttraction)
                return
            }
        }

        if (current.isWaitingForPoiStart && current.endPoint != null) {
            val gridX = ((x - current.offsetX) / (current.scale * _cellSize.value)).toInt()
            val gridY = ((y - current.offsetY) / (current.scale * _cellSize.value)).toInt()
            val startPoint = Pair(gridX, gridY)
            _mapState.update { it.copy(startPoint = startPoint, isWaitingForPoiStart = false) }
            findPath(startPoint, current.endPoint)
            return
        }

        val gridX = ((x - current.offsetX) / (current.scale * _cellSize.value)).toInt()
        val gridY = ((y - current.offsetY) / (current.scale * _cellSize.value)).toInt()
        val clickedPoint = Pair(gridX, gridY)

        when {
            current.endPoint == null -> {
                _mapState.update { it.copy(endPoint = clickedPoint, pathPoints = null, pathError = false) }
            }
            current.startPoint == null -> {
                _mapState.update { it.copy(startPoint = clickedPoint, pathPoints = null, pathError = false) }
                findPath(clickedPoint, current.endPoint)
            }
            else -> {
                _mapState.update {
                    it.copy(
                        endPoint = clickedPoint,
                        startPoint = null,
                        pathPoints = null,
                        pathError = false
                    )
                }
            }
        }
    }

    private fun findPath(from: Pair<Int, Int>, to: Pair<Int, Int>) {
        val astar = gridAStar ?: return
        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                try {
                    val startCell = astar.findNearestWalkable(from.first, from.second)
                    val endCell = astar.findNearestWalkable(to.first, to.second)
                    if (startCell == null || endCell == null) return@withContext null
                    val (path, _) = astar.findPath(startCell, endCell)
                    path
                } catch (e: IllegalArgumentException) {
                    null
                }
            }
            _mapState.update { current ->
                if (result != null) {
                    current.copy(pathPoints = result, pathError = false)
                } else {
                    current.copy(pathPoints = null, pathError = true)
                }
            }
        }
    }

    fun onTransform(panX: Float, panY: Float, zoomChange: Float) {
        _mapState.update { current ->
            val newScale = (current.scale * zoomChange).coerceIn(minScale, maxScale)
            current.copy(
                scale = newScale,
                offsetX = current.offsetX + panX * panSpeed,
                offsetY = current.offsetY + panY * panSpeed
            )
        }
    }

    fun enterClusterMode() {
        _mapState.update {
            it.copy(
                showClusters = true,
                isClustering = true,
                clusters = emptyList(),
                clusterMetricIsPedestrian = false
            )
        }
        launchClustering(usePedestrian = false)
    }

    fun exitClusterMode() {
        _mapState.update {
            it.copy(
                showClusters = false,
                clusters = emptyList(),
                isClustering = false,
                showClusterInfoSheet = false,
                voronoiBitmap = null
            )
        }
    }

    fun toggleClusterMetric() {
        val newPedestrian = !_mapState.value.clusterMetricIsPedestrian
        _mapState.update { it.copy(clusterMetricIsPedestrian = newPedestrian, isClustering = true, clusters = emptyList()) }
        launchClustering(newPedestrian)
    }

    fun showClusterInfo() {
        _mapState.update { it.copy(showClusterInfoSheet = true) }
    }

    fun dismissClusterInfo() {
        _mapState.update { it.copy(showClusterInfoSheet = false) }
    }

    private fun launchClustering(usePedestrian: Boolean) {
        viewModelScope.launch {
            val result = try {
                computeClusters(pointsOfInterest, usePedestrian, gridAStar)
            } catch (e: Exception) {
                emptyList()
            }

            val bitmap = if (result.isNotEmpty()) {
                generateVoronoiBitmap(result, 722, 501)
            } else null

            _mapState.update { it.copy(
                clusters = result,
                isClustering = false,
                voronoiBitmap = bitmap
            )}
        }
    }
}

@Composable
fun MapScreen(
    vm: MapViewModel = viewModel()
) {
    val state = vm.mapState.collectAsState().value
    val cellSize by vm.cellSize.collectAsState()
    val camera = MapCamera(
        scale = state.scale,
        offsetX = state.offsetX,
        offsetY = state.offsetY,
        cellSize = cellSize
    )

    val context = LocalContext.current
    val weightsPath = remember {
        context.filesDir.absolutePath + "/data_for_nn.json"
    }

    CompositionLocalProvider(LocalMapCamera provides camera) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                    vm.updateCellSize(size.width.toFloat() / 722f)
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset -> vm.onMapClick(offset.x, offset.y) },
                        onDoubleTap = { offset -> vm.onMapDoubleTap(offset.x, offset.y) }
                    )
                }
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        vm.onTransform(pan.x, pan.y, zoom)
                    }
                }
        ) {
            DrawMap()
            AnimatedVisibility(
                visible = state.showClusters && state.clusters.isNotEmpty(),
                enter = fadeIn(animationSpec = androidx.compose.animation.core.tween(600)),
                exit = fadeOut(animationSpec = androidx.compose.animation.core.tween(400))
            ) {
                VoronoiClusterOverlay(
                    clusters = state.clusters,
                    camera = camera,
                    voronoiBitmap = state.voronoiBitmap
                )
            }

            DrawWay(pathPoints = state.pathPoints)
            DrawWay(pathPoints = state.acoPath ?: state.pathPoints)
            if (state.isWaitingForAttractionStart) {
                WaitingForStartHint()
            }
            if (state.mapLayer == MapLayer.FOOD) {
                DrawPoiMarkers(
                    pois = pointsOfInterest,
                    selectedPoi = state.selectedPoi,
                    onPoiClick = { vm.onPoiClick(it) }
                )
            }
            if (state.mapLayer == MapLayer.ATTRACTIONS) {
                DrawAttractionMarkers(
                    attractions = attractions,
                    onAttractionClick = { vm.onAttractionClick(it) }
                )
            }
            DrawToMarker(endPoint = state.endPoint)
            DrawFromMarker(startPoint = state.startPoint)

            PoiPopup(
                poi = state.selectedPoi,
                isWaitingForStartPoint = state.isWaitingForPoiStart,
                onBuildRouteClick = { vm.onBuildRouteToPoiRequested() },
                onDismiss = { vm.onPoiDismiss() }
            )
            AttractionPopup(
                attraction = state.selectedAttraction,
                isWaitingForStartPoint = state.isWaitingForPoiStart,
                onBuildRouteClick = { vm.onBuildRouteToAttractionRequested() },
                onDismiss = { vm.onAttractionDismiss() },
                nnWeightsPath = weightsPath
            )

            if (!state.showClusters) {
                DistanceOverlay(
                    startPoint = state.startPoint,
                    endPoint = state.endPoint,
                    pathPoints = state.pathPoints,
                    cellSize = cellSize
                )
            }

            AnimatedVisibility(
                visible = state.showClusters,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                ClusterControlsHud(
                    usePedestrian = state.clusterMetricIsPedestrian,
                    isLoading = state.isClustering,
                    onToggleMetric = { vm.toggleClusterMetric() },
                    onShowInfo = { vm.showClusterInfo() },
                    onExit = { vm.exitClusterMode() }
                )
            }
        }
    }

    if (state.showClusterInfoSheet) {
        ClusterInfoSheet(
            clusters = state.clusters,
            onDismiss = { vm.dismissClusterInfo() }
        )
    }
}

@Composable
fun DrawMap() {
    val camera = LocalMapCamera.current
    Image(
        painter = painterResource(R.drawable.map),
        contentDescription = "Map",
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = camera.scale
                scaleY = camera.scale
                translationX = camera.offsetX
                translationY = camera.offsetY
                transformOrigin = TransformOrigin(0f, 0f)
            },
        alignment = Alignment.TopStart
    )
}

@Composable
fun DrawWay(
    pathPoints: List<GridCell>?,
    pathColor: Color = Color(standardTSUColor.toColorInt()),
    strokeWidthDp: Float = 4f
) {
    if (pathPoints == null || pathPoints.size < 2) return
    val camera = LocalMapCamera.current
    val density = LocalDensity.current
    val strokeWidthPx = with(density) { strokeWidthDp.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val transformed = pathPoints.map { cell ->
            Offset(
                x = (cell.x * camera.scale * camera.cellSize) + camera.offsetX,
                y = (cell.y * camera.scale * camera.cellSize) + camera.offsetY
            )
        }
        val path = Path().apply {
            transformed.forEachIndexed { i, offset ->
                if (i == 0) moveTo(offset.x, offset.y)
                else lineTo(offset.x, offset.y)
            }
        }
        drawPath(
            path = path,
            color = pathColor.copy(alpha = 0.85f),
            style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun DrawToMarker(endPoint: Pair<Int, Int>?) {
    if (endPoint == null) return
    val camera = LocalMapCamera.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = camera.toScreenX(endPoint.first) - size.width / 2,
                    y = (camera.toScreenY(endPoint.second) - size.height / 1.2f).roundToInt()
                )
            }
            .onSizeChanged { size = it }
    ) {
        Image(
            painter = painterResource(id = R.drawable.marker_icon),
            contentDescription = null,
            modifier = Modifier.graphicsLayer(scaleX = 0.8f, scaleY = 0.8f)
        )
    }
}

@Composable
fun DrawFromMarker(startPoint: Pair<Int, Int>?) {
    if (startPoint == null) return
    val camera = LocalMapCamera.current
    var size by remember { mutableStateOf(IntSize.Zero) }
    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = camera.toScreenX(startPoint.first) - size.width / 2,
                    y = camera.toScreenY(startPoint.second) - size.height / 2
                )
            }
            .onSizeChanged { size = it }
    ) {
        Image(
            painter = painterResource(id = R.drawable.start_point_icon),
            contentDescription = null,
            modifier = Modifier.graphicsLayer(scaleX = 1f, scaleY = 1f)
        )
    }
}