package com.example.tsuonspot

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt

enum class MapLayer {
    CLEAN,
    FOOD,
    ATTRACTIONS
}

fun MapLayer.next(): MapLayer = when (this) {
    MapLayer.CLEAN       -> MapLayer.FOOD
    MapLayer.FOOD        -> MapLayer.ATTRACTIONS
    MapLayer.ATTRACTIONS -> MapLayer.CLEAN
}

fun MapLayer.iconRes(): Int = when (this) {
    MapLayer.CLEAN       -> R.drawable.layer_icon_clean
    MapLayer.FOOD        -> R.drawable.layer_icon_food
    MapLayer.ATTRACTIONS -> R.drawable.layer_icon_attractions
}

@Composable
fun MapLayerToggleButton(
    currentLayer: MapLayer,
    onLayerChange: (MapLayer) -> Unit,
    modifier: Modifier = Modifier
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())

    val bgColor by animateColorAsState(
        targetValue = when (currentLayer) {
            MapLayer.CLEAN       -> Color.White
            MapLayer.FOOD        -> Color.White
            MapLayer.ATTRACTIONS -> Color.White
        },
        animationSpec = tween(250),
        label = "layerBg"
    )

    Box(
        modifier = modifier
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = tsuBlue)
            ) {
                onLayerChange(currentLayer.next())
            }
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = currentLayer.iconRes()),
            contentDescription = "Переключить слой карты",
            modifier = Modifier.size(28.dp)
        )
    }
}