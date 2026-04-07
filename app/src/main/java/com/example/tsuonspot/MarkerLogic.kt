package com.example.tsuonspot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.alghoritms.Data.GridCell
import com.example.alghoritms.pathFind.GridAStar
import kotlin.math.roundToInt

class MarkerLogic {

    private val IMG_WIDTH_PX = 1345f
    private val IMG_HEIGHT_PX = 933f
    private val MATRIX_WIDTH = 722f
    private val MATRIX_HEIGHT = 501f

    private val DISPLAY_SCALE = 3.0f
    private val SCALE_X = IMG_WIDTH_PX / MATRIX_WIDTH
    private val SCALE_Y = IMG_HEIGHT_PX / MATRIX_HEIGHT

    @Composable
    fun DrawMap() {
        val correctionX = 155f * DISPLAY_SCALE
        val correctionY = 5f * DISPLAY_SCALE
        val context = LocalContext.current
        val density = LocalDensity.current
        val densityValue = density.density

        val mapWidthDp = (IMG_WIDTH_PX / densityValue * DISPLAY_SCALE).dp
        val mapHeightDp = (IMG_HEIGHT_PX / densityValue * DISPLAY_SCALE).dp

        val aStar = remember { GridAStar.loadFromAssets(context, "output_matrix.txt") }

        var offset by remember { mutableStateOf(Offset.Zero) }
        var markerMapPos by remember { mutableStateOf<Offset?>(null) }
        var startMarkerMapPos by remember { mutableStateOf<Offset?>(null) }
        var isSelectingStart by remember { mutableStateOf(false) }
        var pathResult by remember { mutableStateOf<List<GridCell>>(emptyList()) }

        val hitRadiusPx = with(density) { 40.dp.toPx() * DISPLAY_SCALE }

        LaunchedEffect(markerMapPos, startMarkerMapPos) {
            if (markerMapPos != null && startMarkerMapPos != null) {
                val startRow = (startMarkerMapPos!!.y / SCALE_Y).toInt()
                val startCol = (startMarkerMapPos!!.x / SCALE_X).toInt()
                val endRow = (markerMapPos!!.y / SCALE_Y).toInt()
                val endCol = (markerMapPos!!.x / SCALE_X).toInt()

                val startCell = aStar.findNearestWalkable(startRow, startCol)
                val endCell = aStar.findNearestWalkable(endRow, endCol)

                if (startCell != null && endCell != null) {
                    try {
                        val (path, _) = aStar.findPath(startCell, endCell)
                        pathResult = path
                    } catch (e: Exception) { pathResult = emptyList() }
                }
            } else { pathResult = emptyList() }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount ->
                        offset += dragAmount
                    }
                }
                .pointerInput(isSelectingStart) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val mapX = ((tapOffset.x - offset.x) / DISPLAY_SCALE) + correctionX
                            val mapY = ((tapOffset.y - offset.y) / DISPLAY_SCALE) + correctionY

                            if (isSelectingStart) {
                                startMarkerMapPos = Offset(mapX, mapY)
                                isSelectingStart = false
                            } else {
                                markerMapPos = Offset(mapX, mapY)
                                startMarkerMapPos = null
                            }
                        },
                        onDoubleTap = { tapOffset ->
                            val mapX = ((tapOffset.x - offset.x) / DISPLAY_SCALE) + correctionX
                            val mapY = ((tapOffset.y - offset.y) / DISPLAY_SCALE) + correctionY
                            val clickPos = Offset(mapX, mapY)
                            val distToTarget = markerMapPos?.let { (clickPos - it).getDistance() } ?: Float.MAX_VALUE
                            val distToStart = startMarkerMapPos?.let { (clickPos - it).getDistance() } ?: Float.MAX_VALUE

                            if (distToTarget < hitRadiusPx || distToStart < hitRadiusPx) {
                                markerMapPos = null
                                startMarkerMapPos = null
                                pathResult = emptyList()
                            }
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                    .requiredSize(mapWidthDp, mapHeightDp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.map),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.FillBounds
                )

                Canvas(modifier = Modifier.fillMaxSize()) {
                    pathResult.forEach { cell ->
                        drawCircle(
                            color = Color(standardTSUColor.toColorInt()),
                            radius = 3f * DISPLAY_SCALE,
                            center = Offset(
                                cell.y * SCALE_X * DISPLAY_SCALE,
                                cell.x * SCALE_Y * DISPLAY_SCALE)
                        )
                    }
                }

                markerMapPos?.let { pos ->
                    Box(modifier = Modifier
                        .offset {
                            IntOffset(
                                (pos.x * DISPLAY_SCALE - 20.dp.toPx()).roundToInt(),
                                (pos.y * DISPLAY_SCALE - 40.dp.toPx()).roundToInt()
                            )
                        }
                    ) {
                        Image(painter = painterResource(id = R.drawable.marker_icon), null, modifier = Modifier.size(40.dp))
                    }
                    MarkerRouteButton(pos) { isSelectingStart = true }
                }

                startMarkerMapPos?.let { pos ->
                    Box(modifier = Modifier
                        .offset {
                            IntOffset(
                                (pos.x * DISPLAY_SCALE - 15.dp.toPx()).roundToInt(),
                                (pos.y * DISPLAY_SCALE - 15.dp.toPx()).roundToInt()
                            )
                        }
                    ) {
                        Image(painter = painterResource(id = R.drawable.start_point_icon), null, modifier = Modifier.size(30.dp))
                    }
                }
            }

            if (isSelectingStart) SelectStartHint()
            markerMapPos?.let { MapCoordinatesLabel(it) }
        }
    }

    @Composable
    private fun MarkerRouteButton(mapPos: Offset, onRouteClick: () -> Unit) {
        Box(modifier = Modifier.offset { IntOffset((
                mapPos.x * DISPLAY_SCALE + 15.dp.toPx()).roundToInt(),
            (mapPos.y * DISPLAY_SCALE - 70.dp.toPx()).roundToInt()
        ) }) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(modifier = Modifier.clickable(onClick = onRouteClick).padding(horizontal = 14.dp, vertical = 8.dp)) {
                    Text("Построить маршрут", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(standardTSUColor.toColorInt()))
                }
            }
        }
    }

    @Composable
    private fun SelectStartHint() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(top = 60.dp),
            contentAlignment = Alignment.TopCenter) {
            Card(
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = Color(standardTSUColor.toColorInt())
                )
            ) {
                Text(
                    "Выберите место,\nоткуда построить маршрут",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(20.dp),
                    fontFamily = standardTSUFont,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    private fun MapCoordinatesLabel(mapPos: Offset) {
        Card(
            modifier = Modifier
                .padding(start = 16.dp)
                .statusBarsPadding(),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Color(standardTSUColor.toColorInt())
            )
        ) {
            Text(
                "x: ${mapPos.x.roundToInt()}  y: ${mapPos.y.roundToInt()}",
                modifier = Modifier.padding(12.dp),
                fontFamily = standardTSUFont,
                fontWeight = FontWeight.Bold
            )
        }
    }
}