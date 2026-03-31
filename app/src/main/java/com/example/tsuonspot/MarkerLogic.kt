package com.example.tsuonspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize

class MarkerLogic {
    @Composable
    public fun DrawMap() {
        var offset by remember { mutableStateOf(Offset.Zero) }
        var markerMapPos by remember { mutableStateOf<Offset?>(null) }
        var containerSizePx by remember { mutableStateOf(IntSize.Zero) }
        val markerHitRadiusDp = 30f

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coordinates ->
                    containerSizePx = coordinates.size
                }
                .pointerInput(Unit) {
                    detectDragGestures { _, dragAmount: Offset ->
                        offset = Offset(
                            x = offset.x + dragAmount.x,
                            y = offset.y + dragAmount.y
                        )
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { tapOffset ->
                            val mapX = tapOffset.x - offset.x
                            val mapY = tapOffset.y - offset.y
                            markerMapPos = Offset(mapX, mapY)
                        },
                        onDoubleTap = { tapOffset ->
                            markerMapPos?.let { mapPos ->
                                val markerScreenX = offset.x + mapPos.x
                                val markerScreenY = offset.y + mapPos.y
                                val hitRadiusPx = markerHitRadiusDp * density
                                val dx = tapOffset.x - markerScreenX
                                val dy = tapOffset.y - markerScreenY
                                if (dx * dx + dy * dy <= hitRadiusPx * hitRadiusPx) {
                                    markerMapPos = null
                                }
                            }
                        }
                    )
                }
        ) {
            Image(
                painter = painterResource(id = R.drawable.map),
                contentDescription = "Map",
                modifier = Modifier
                    .offset {
                        IntOffset(offset.x.roundToInt(),
                                  offset.y.roundToInt())
                    }
                    .graphicsLayer(
                        scaleX = 3f,
                        scaleY = 3f,
                    )
            )

            markerMapPos?.let { mapPos ->
                val screenX = offset.x + mapPos.x
                val screenY = offset.y + mapPos.y

                MarkerOverlay(
                    screenPos = Offset(screenX, screenY),
                    containerWidthPx = containerSizePx.width,
                    onRouteClick = { /* МАРШРУТ ТУТ */}
                )
            }

            markerMapPos?.let { mapPos ->
                MapCoordinatesLabel(mapPos)
            }
        }
    }

    @Composable
    private fun MarkerOverlay(
        screenPos: Offset,
        containerWidthPx: Int,
        onRouteClick: () -> Unit
    ) {
        Box(
            modifier = Modifier.offset {
                IntOffset(
                    x = (screenPos.x - 20.dp.toPx()).roundToInt(),
                    y = (screenPos.y - 40.dp.toPx()).roundToInt()
                )
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.marker_icon),
                contentDescription = "Маркер",
                modifier = Modifier.size(40.dp)
            )
        }

        val isOnLeftHalf = screenPos.x < containerWidthPx / 2f

        Box(
            modifier = Modifier.offset {
                if (isOnLeftHalf) {
                    IntOffset(
                        x = (screenPos.x + 15.dp.toPx()).roundToInt(),
                        y = (screenPos.y - 70.dp.toPx()).roundToInt()
                    )
                } else {
                    IntOffset(
                        x = (screenPos.x - 160.dp.toPx()).roundToInt(),
                        y = (screenPos.y - 70.dp.toPx()).roundToInt()
                    )
                }
            }
        ) {
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Box(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(color = Color(standardTSUColor.toColorInt())),
                            onClick = onRouteClick
                        )
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Построить маршрут",
                        fontFamily = standardTSUFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(standardTSUColor.toColorInt())
                    )
                }
            }
        }
    }

    @Composable
    private fun MapCoordinatesLabel(mapPos: Offset) {
        Card(
            modifier = Modifier
                .padding(16.dp, 0.dp)
                .statusBarsPadding(),
            shape = RoundedCornerShape(10.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Text(
                text = "x: ${mapPos.x.roundToInt()}  y: ${mapPos.y.roundToInt()}",
                fontFamily = standardTSUFont,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(standardTSUColor.toColorInt()),
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}