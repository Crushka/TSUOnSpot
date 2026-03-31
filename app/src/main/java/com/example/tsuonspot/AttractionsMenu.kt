package com.example.tsuonspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

class AttractionsMenu {
    data class Attraction(
        val id: Int,
        val title: String,
        val icon: Int,
        val description: String
    )
    @Composable
    fun DrawMenu() {
        val attractions = remember {
            listOf(
                Attraction(1, "Камень - символ геофизического центра Евразии", R.drawable.stone, "Stone"),
                Attraction(2, "Профессорам В.М. Флоринскому и Д.И. Менделееву", R.drawable.flor_mend, "FlorMend"),
                Attraction(3, "Каменные бабы", R.drawable.stone_babas, "StoneBabas"),
                Attraction(4, "Профессор Белкин", R.drawable.prof_belkin, "ProfBelk"),
                Attraction(5, "Г.Н. Потанин", R.drawable.potanin, "Potanin"),
                Attraction(6, "Павшим за родину", R.drawable.rodina, "Rodina"),
                Attraction(7, "Ботанический сад ТГУ", R.drawable.bot_sad, "BotSad")
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
            ) {
                items(attractions) { item ->
                    CardTemplate(
                        attraction = item,
                        onCardClick = {

                        },
                        onAddClick = {

                        }
                    )

                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(20.dp, 0.dp)
                        .size(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color(standardTSUColor.toColorInt())
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "0",
                            fontSize = 46.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 0.dp, end = 20.dp)
                        .height(60.dp)
                        .width(400.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(standardTSUColor.toColorInt()),
                        contentColor = Color.White
                    )
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Построить маршрут",
                            fontSize = 24.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

    }

    @Composable
    private fun CardTemplate(
        attraction: Attraction,
        onCardClick: () -> Unit,
        onAddClick: () -> Unit
        ) {
        Card( // ГЛАВНАЯ КАРТОЧКА
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp, 10.dp)
                .size(95.dp)
                .clickable { onCardClick() },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
                contentColor = Color(standardTSUColor.toColorInt())
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(attraction.icon),
                    contentDescription = attraction.description,
                    modifier = Modifier
                        .size(75.dp)
                        .clip(shape = RoundedCornerShape(15.dp)),
                    contentScale = ContentScale.Crop
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 7.dp),
                ) {
                    Text(
                        text = attraction.title,
                        fontSize = 14.sp,
                        lineHeight = 1.2.em,
                        fontFamily = standardTSUFont,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                    Card( // МАЛАЯ КАРТОЧКА СИНЯЯ
                        modifier = Modifier
                            .padding(top = 5.dp)
                            .size(width = 170.dp, height = 30.dp)
                            .clickable { onAddClick() },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(standardTSUColor.toColorInt()),
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(5.dp)
                                    )
                            )
                            Text(
                                modifier = Modifier
                                    .padding(start = 5.dp),
                                text = "Добавить в маршрут",
                                fontSize = 12.sp,
                                fontFamily = standardTSUFont,
                                fontWeight = FontWeight.Bold,
                                style = LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(
                                        includeFontPadding = false
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}