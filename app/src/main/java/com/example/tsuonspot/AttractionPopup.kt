package com.example.tsuonspot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

@Composable
fun AttractionPopup(
    attraction: Attraction?,
    isWaitingForStartPoint: Boolean,
    onBuildRouteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(
        visible = attraction != null,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit  = fadeOut() + scaleOut(targetScale = 0.85f)
    ) {
        if (attraction == null) return@AnimatedVisibility

        val camera = LocalMapCamera.current
        val tsuBlue = Color(standardTSUColor.toColorInt())

        val screenX = camera.toScreenX(attraction.gridX)
        val screenY = camera.toScreenY(attraction.gridY)

        var cardSize by remember { mutableStateOf(IntSize.Zero) }

        val offsetX = screenX + 20
        val offsetY = screenY - cardSize.height - 10

        Box(
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = offsetX,
                        y = offsetY.coerceAtLeast(8)
                    )
                }
                .onSizeChanged { cardSize = it }
                .widthIn(max = 260.dp)
        ) {
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {

                    Image(
                        painter = painterResource(id = attraction.icon),
                        contentDescription = attraction.title,
                        modifier = Modifier
                            .width(240.dp)
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.width(240.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = attraction.title,
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    lineHeight = 14.sp,
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                ),
                                fontFamily = standardTSUFont,
                                fontWeight = FontWeight.Bold,
                                color = tsuBlue,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(5) {
                                    Image(
                                        painter = painterResource(id = R.drawable.grey_star),
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.width(8.dp))

                        Card(
                            modifier = Modifier
                                .height(35.dp)
                                .width(100.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.White)
                                ) { },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.5.dp, tsuBlue)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Оценить",
                                    fontSize = 15.sp,
                                    fontFamily = standardTSUFont,
                                    fontWeight = FontWeight.Bold,
                                    color = tsuBlue
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.width(240.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.White)
                                ) {
                                    if (!isWaitingForStartPoint) onBuildRouteClick()
                                },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isWaitingForStartPoint)
                                    tsuBlue.copy(alpha = 0.5f) else tsuBlue
                            )
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (isWaitingForStartPoint)
                                        "Укажите точку старта" else "Построить маршрут",
                                    fontSize = if (isWaitingForStartPoint) 13.sp else 15.sp,
                                    fontFamily = standardTSUFont,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(
                                    color = tsuBlue.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = tsuBlue)
                                ) { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✕",
                                fontSize = 14.sp,
                                color = tsuBlue,
                                fontFamily = standardTSUFont,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}