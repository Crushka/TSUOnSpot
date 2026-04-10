package com.example.tsuonspot

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.alghoritms.Data.GridCell
import kotlin.math.roundToInt
import kotlin.math.sqrt

const val METERS_PER_PIXEL: Float = 1.05f
fun cartesianDistanceMeters(
    start: Pair<Int, Int>,
    end: Pair<Int, Int>,
    cellSize: Float
): Float {
    val dx = (end.first  - start.first ).toFloat() * cellSize
    val dy = (end.second - start.second).toFloat() * cellSize
    return sqrt(dx * dx + dy * dy) * METERS_PER_PIXEL
}

fun pathDistanceMeters(
    pathPoints: List<GridCell>,
    cellSize: Float
): Float {
    if (pathPoints.size < 2) return 0f
    var total = 0f
    for (i in 1 until pathPoints.size) {
        val dx = (pathPoints[i].x - pathPoints[i - 1].x).toFloat() * cellSize
        val dy = (pathPoints[i].y - pathPoints[i - 1].y).toFloat() * cellSize
        total += sqrt(dx * dx + dy * dy)
    }
    return total * METERS_PER_PIXEL
}

private fun formatDistance(meters: Float): String {
    return if (meters >= 1000f) {
        val km = meters / 1000f
        "%.2f км".format(km)
    } else {
        "${meters.roundToInt()} м"
    }
}

@Composable
fun DistanceOverlay(
    startPoint: Pair<Int, Int>?,
    endPoint: Pair<Int, Int>?,
    pathPoints: List<GridCell>?,
    cellSize: Float
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())

    val bothSet = startPoint != null && endPoint != null
    val hasPath = pathPoints != null && pathPoints.size >= 2

    val cartesian = if (bothSet)
        cartesianDistanceMeters(startPoint!!, endPoint!!, cellSize) else null
    val pathDist  = if (hasPath)
        pathDistanceMeters(pathPoints!!, cellSize) else null

    AnimatedVisibility(
        visible = bothSet || hasPath,
        enter = fadeIn(),
        exit  = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 16.dp)
                .width(200.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor   = tsuBlue
            )
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text       = "Расстояние",
                    fontSize   = 13.sp,
                    fontFamily = standardTSUFont,
                    fontWeight = FontWeight.Bold,
                    color      = tsuBlue
                )

                if (pathDist != null) {
                    Spacer(Modifier.height(2.dp))
                    DistanceRow(
                        label = "По маршруту",
                        value = formatDistance(pathDist),
                        tsuBlue = tsuBlue
                    )
                }

                if (pathDist != null && cartesian != null) {
                    HorizontalDivider(
                        color     = tsuBlue.copy(alpha = 0.15f),
                        thickness = 1.dp
                    )
                }

                if (cartesian != null) {
                    DistanceRow(
                        label = "По прямой",
                        value = formatDistance(cartesian),
                        tsuBlue = tsuBlue
                    )
                }
            }
        }
    }
}

@Composable
private fun DistanceRow(label: String, value: String, tsuBlue: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.width(172.dp)
    ) {
        Text(
            text       = label,
            fontSize   = 12.sp,
            fontFamily = standardTSUFont,
            fontWeight = FontWeight.Normal,
            color      = tsuBlue.copy(alpha = 0.7f)
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text       = value,
            fontSize   = 14.sp,
            fontFamily = standardTSUFont,
            fontWeight = FontWeight.Bold,
            color      = tsuBlue
        )
    }
}