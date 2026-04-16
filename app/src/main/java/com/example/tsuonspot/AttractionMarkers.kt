package com.example.tsuonspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun DrawAttractionMarkers(
    attractions: List<Attraction>,
    onAttractionClick: (Attraction) -> Unit
) {
    val camera = LocalMapCamera.current
    attractions.forEach { attraction ->
        AttractionMarker(
            attraction = attraction,
            camera = camera,
            onClick = { onAttractionClick(attraction) }
        )
    }
}

@Composable
private fun AttractionMarker(
    attraction: Attraction,
    camera: MapCamera,
    onClick: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = camera.toScreenX(attraction.gridX) - size.width / 2,
                    y = camera.toScreenY(attraction.gridY) - size.height / 2
                )
            }
            .onSizeChanged { size = it }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Image(
            painter = painterResource(id = R.drawable.point_of_attraction),
            contentDescription = attraction.title,
            modifier = Modifier.size(20.dp)
        )
    }
}