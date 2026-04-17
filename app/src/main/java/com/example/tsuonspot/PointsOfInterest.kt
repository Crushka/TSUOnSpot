package com.example.tsuonspot

import android.content.Context
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import java.io.File
import kotlin.math.sqrt

data class PointOfInterest(
    val id: Int,
    val title: String,
    val gridX: Int,
    val gridY: Int,
    val imageRes: Int
)

val pointsOfInterest = listOf(
    PointOfInterest(1, "Сыр-Бор",                 gridX = 340, gridY = 95,  imageRes = R.drawable.cheese_bor),
    PointOfInterest(2, "XO Bakery",               gridX = 310, gridY = 220, imageRes = R.drawable.cafe_2nd_building),
    PointOfInterest(3, "Сибирские блины",         gridX = 355, gridY = 180, imageRes = R.drawable.sib_blini),
    PointOfInterest(4, "100ловая",                gridX = 365, gridY = 165, imageRes = R.drawable.cafeteria_tsu),
    PointOfInterest(5, "Батина шаурма",           gridX = 185, gridY = 70,  imageRes = R.drawable.batina_shaurma),
    PointOfInterest(6, "StarBooks",               gridX = 335, gridY = 165, imageRes = R.drawable.star_books),
    PointOfInterest(7, "Безумно. Крутая шаурма",  gridX = 180, gridY = 55,  imageRes = R.drawable.bezumno),
    PointOfInterest(8, "Мясной бульвар",          gridX = 145, gridY = 310, imageRes = R.drawable.meat_boulevar),
    PointOfInterest(9, "Мария Ра",                gridX = 150, gridY = 465, imageRes = R.drawable.mariy_ra),
    PointOfInterest(10, "Цзисян",                 gridX = 155, gridY = 200, imageRes = R.drawable.chzisyan),
    PointOfInterest(11, "Сибирский Smoker",       gridX = 120, gridY = 450, imageRes = R.drawable.sib_smoker),
    PointOfInterest(12, "Rostic's",               gridX = 505, gridY = 190, imageRes = R.drawable.rostics),
    PointOfInterest(13, "Гербарий",               gridX = 520, gridY = 135, imageRes = R.drawable.gerbariy),
    PointOfInterest(14, "Ближе",                  gridX = 540, gridY = 40,  imageRes = R.drawable.closer),
    PointOfInterest(15, "Вечный ZOV",             gridX = 560, gridY = 65,  imageRes = R.drawable.endless_zov),
    PointOfInterest(16, "Лампочка",               gridX = 695, gridY = 480, imageRes = R.drawable.lampochka),
    PointOfInterest(17, "Буланже/Папа Джонс",     gridX = 640, gridY = 365, imageRes = R.drawable.father_johnes),
    PointOfInterest(18, "Тесто",                  gridX = 595, gridY = 400, imageRes = R.drawable.testo),
    PointOfInterest(19, "Poly Bistro",            gridX = 595, gridY = 430, imageRes = R.drawable.poly_bistro),
    PointOfInterest(20, "Panda Jui",              gridX = 595, gridY = 445, imageRes = R.drawable.panda_jui),
    PointOfInterest(21, "Rebro",                  gridX = 595, gridY = 470, imageRes = R.drawable.rebro),
    PointOfInterest(22, "Минутка",                gridX = 365, gridY = 175, imageRes = R.drawable.minutka),
    PointOfInterest(23, "Научка",                 gridX = 440, gridY = 270, imageRes = R.drawable.nauchka),
    PointOfInterest(24, "Колобок",                gridX = 650, gridY = 480, imageRes = R.drawable.kolobok),
    PointOfInterest(25, "Белка кофе",             gridX = 475, gridY = 65, imageRes = R.drawable.belka_coffee),
    PointOfInterest(26, "Пешком постою",          gridX = 595, gridY = 460, imageRes = R.drawable.stand_on_foot),
    PointOfInterest(27, "Пеки, Лола!",            gridX = 625, gridY = 50, imageRes = R.drawable.bake_lola),
    PointOfInterest(28, "Подкова",                gridX = 105, gridY = 110, imageRes = R.drawable.podkova),
    PointOfInterest(29, "Ярче на Советской",      gridX = 585, gridY = 50, imageRes = R.drawable.yarche_sovetsk),
    PointOfInterest(30, "Ярче на Кирова",         gridX = 620, gridY = 365, imageRes = R.drawable.yarche_kirova)
)

fun findPoiByCsvRecommendation(recommendation: String): PointOfInterest? {
    val slugToId = mapOf(
        "syr_bor" to 1, "second_corps_canteen" to 2, "siberian_pancakes" to 3,
        "stolovaya_100" to 4, "batina_shaurma" to 5, "starbooks" to 6,
        "bezumno_shaurma" to 7, "meat_boulevard" to 8, "mariya_ra" to 9,
        "chzisyan" to 10, "sib_smoker" to 11, "rostics" to 12,
        "herbarium" to 13, "blizhe" to 14, "vechniy_zov" to 15,
        "lampochka" to 16, "papa_johns" to 17, "testo_pastry" to 18,
        "poly_bistro" to 19, "panda_juice" to 20, "rebro_grill" to 21,
        "cafe_minutka" to 22, "nauchka" to 23, "kolobok" to 24,
        "belka_coffee" to 25, "peshkom_postoyu" to 26, "peki_lola" to 27,
        "podkova" to 28, "yarche_1" to 29, "yarche_2" to 30
    )
    return pointsOfInterest.find { it.id == slugToId[recommendation.lowercase().trim()] }
}

fun findTappedPoi(
    tapX: Float,
    tapY: Float,
    pois: List<PointOfInterest>,
    camera: MapCamera,
    hitRadiusPx: Float = 48f
): PointOfInterest? {
    return pois.firstOrNull { poi ->
        val sx = (poi.gridX * camera.scale * camera.cellSize + camera.offsetX)
        val sy = (poi.gridY * camera.scale * camera.cellSize + camera.offsetY)
        val dx = tapX - sx
        val dy = tapY - sy
        sqrt(dx * dx + dy * dy) <= hitRadiusPx
    }
}

fun findTappedAttraction(
    tapX: Float,
    tapY: Float,
    attractions: List<Attraction>,
    camera: MapCamera,
    hitRadiusPx: Float = 48f
): Attraction? {
    return attractions.firstOrNull { attraction ->
        val sx = attraction.gridX * camera.scale * camera.cellSize + camera.offsetX
        val sy = attraction.gridY * camera.scale * camera.cellSize + camera.offsetY
        val dx = tapX - sx
        val dy = tapY - sy
        sqrt(dx * dx + dy * dy) <= hitRadiusPx
    }
}

@Composable
fun DrawPoiMarkers(
    pois: List<PointOfInterest>,
    selectedPoi: PointOfInterest?,
    onPoiClick: (PointOfInterest) -> Unit
) {
    val camera = LocalMapCamera.current

    pois.forEach { poi ->
        val isSelected = poi.id == selectedPoi?.id
        PoiMarker(
            poi = poi,
            camera = camera,
            isSelected = isSelected,
            onClick = { onPoiClick(poi) }
        )
    }
}

@Composable
private fun PoiMarker(
    poi: PointOfInterest,
    camera: MapCamera,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var size by remember { mutableStateOf(IntSize.Zero) }
    val iconSizeDp = if (isSelected) 22.dp else 20.dp

    Box(
        modifier = Modifier
            .offset {
                IntOffset(
                    x = camera.toScreenX(poi.gridX) - size.width / 2,
                    y = camera.toScreenY(poi.gridY) - size.height / 2
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
            painter = painterResource(id = R.drawable.point_of_interest),
            contentDescription = poi.title,
            modifier = Modifier.size(iconSizeDp)
        )
    }
}

@Composable
fun PoiPopup(
    poi: PointOfInterest?,
    isWaitingForStartPoint: Boolean,
    onBuildRouteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val weightsPath = remember { getWeightsFilePath(context) }

    AnimatedVisibility(
        visible = poi != null,
        enter = fadeIn() + scaleIn(initialScale = 0.85f),
        exit  = fadeOut() + scaleOut(targetScale = 0.85f)
    ) {
        if (poi == null) return@AnimatedVisibility

        val context = LocalContext.current
        val camera  = LocalMapCamera.current
        val tsuBlue = Color(standardTSUColor.toColorInt())

        var currentRating by remember(poi.id) {
            mutableIntStateOf(RatingStore.getPoiRating(context, poi.id))
        }
        var showRatingDialog by remember(poi.id) { mutableStateOf(false) }

        val screenX = camera.toScreenX(poi.gridX)
        val screenY = camera.toScreenY(poi.gridY)

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
                        painter = painterResource(id = poi.imageRes),
                        contentDescription = poi.title,
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
                                text = poi.title,
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

                            if (currentRating >= 0) {
                                RatingStars(rating = currentRating, starSize = 12)
                            } else {
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
                        }

                        Spacer(Modifier.width(8.dp))

                        Card(
                            modifier = Modifier
                                .height(35.dp)
                                .width(100.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(color = Color.White)
                                ) { showRatingDialog = true },
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

        RatingDialog(
            visible = showRatingDialog,
            weightsFile = weightsPath,
            onDismiss = { showRatingDialog = false },
            onRatingConfirmed = { rating ->
                RatingStore.setPoiRating(context, poi.id, rating)
                currentRating    = rating
                showRatingDialog = false
            }
        )
    }
}

fun getWeightsFilePath(context: Context): String? {
    val fileName = "data_for_nn.json"
    val file = File(context.filesDir, fileName)

    if (!file.exists()) {
        try {
            context.assets.open(fileName).use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    return file.absolutePath
}