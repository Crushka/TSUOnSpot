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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

class WhereToEatMenu {
    data class EatPlace(
        val id: Int,
        val title: String,
        val icon: Int,
        val places: String,
        val description: String
    )

    @Composable
    fun DrawMenu(
        onEatPlaceClick: (EatPlace) -> Unit = {}
    ) {
        val eatList = remember {
            listOf(
                EatPlace(1, "Блины", R.drawable.rodina, "Сибирские блины", "Blini"),
                EatPlace(2, "Кофе", R.drawable.rodina, "StarBooks, Научка, Ярче, Абрикос", "Coffee"),
                EatPlace(3, "Снеки", R.drawable.rodina, "Вендинговые аппараты, Ярче, Абрикос, Подкова", "Snacks"),
                EatPlace(4, "Одноразовая посуда", R.drawable.rodina, "Ярче, Абрикос", "DispTableware")
            )
        }

        var selectedPlaces by remember { mutableStateOf(setOf<Int>()) }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 10.dp, bottom = 90.dp)
            ) {
                items(eatList) { item ->
                    val isSelected = item.id in selectedPlaces
                    CardTemplate(
                        attraction = item,
                        isSelected = isSelected,
                        onCardClick = {
                            onEatPlaceClick(item)
                        },
                        onAddClick = {
                            selectedPlaces = if (isSelected) {
                                selectedPlaces - item.id
                            } else {
                                selectedPlaces + item.id
                            }
                        }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxSize(),
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
                            text = selectedPlaces.size.toString(),
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
        attraction: EatPlace,
        isSelected: Boolean,
        onCardClick: () -> Unit,
        onAddClick: () -> Unit
    ) {
        Card(
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
                    Card(
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
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Выбрано",
                                        tint = Color(standardTSUColor.toColorInt()),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                modifier = Modifier.padding(start = 5.dp),
                                text = if (isSelected) "Добавлено" else "Добавить",
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