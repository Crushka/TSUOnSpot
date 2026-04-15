package com.example.tsuonspot

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

class AttractionsMenu {
    data class Attraction(
        val id: Int,
        val title: String,
        val icon: Int,
        val description: String,
        val type: String
    )

    @Composable
    fun DrawMenu(
        onAttractionClick: (Attraction) -> Unit = {}
    ) {
        val attractionsList = remember {
            listOf(
                Attraction(1, "Камень - символ геофизического центра Евразии", R.drawable.stone, "Stone", "attraction"),
                Attraction(2, "Профессорам В.М. Флоринскому и Д.И. Менделееву", R.drawable.flor_mend, "FlorMend", "attraction"),
                Attraction(3, "Каменные бабы", R.drawable.stone_babas, "StoneBabas", "attraction"),
                Attraction(4, "Профессор Белкин", R.drawable.prof_belkin, "ProfBelk", "attraction"),
                Attraction(5, "Г.Н. Потанин", R.drawable.potanin, "Potanin", "attraction"),
                Attraction(6, "Павшим за родину", R.drawable.rodina, "Rodina", "attraction"),
                Attraction(7, "Ботанический сад ТГУ", R.drawable.bot_sad, "BotSad", "attraction")
            )
        }

        val coworkingsList = remember {
            listOf(
                Attraction(101, "Научка: Инфоцентр", R.drawable.bot_sad, "Info", "coworking"),
                Attraction(102, "Коворкинг в 24 корпусе", R.drawable.stone, "C24", "coworking"),
                Attraction(103, "IT-пространство", R.drawable.prof_belkin, "IT", "coworking")
            )
        }

        var selectedTabIndex by remember { mutableIntStateOf(0) }
        val tabs = listOf("Достопримечательности", "Коворкинги")

        var selectedIds by remember { mutableStateOf(setOf<Int>()) }

        val currentList = if (selectedTabIndex == 0) attractionsList else coworkingsList
        val tsuBlue = Color(standardTSUColor.toColorInt())

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = tsuBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = tsuBlue
                        )
                    },
                    divider = {}
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 14.sp,
                                    fontFamily = standardTSUFont,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 100.dp)
                ) {
                    items(currentList, key = { it.id }) { item ->
                        val isSelected = item.id in selectedIds
                        CardTemplate(
                            attraction = item,
                            isSelected = isSelected,
                            onCardClick = { onAttractionClick(item) },
                            onAddClick = {
                                selectedIds = if (isSelected) {
                                    selectedIds - item.id
                                } else {
                                    selectedIds + item.id
                                }
                            }
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(bottom = 10.dp)
                    .fillMaxSize()
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .padding(start = 20.dp, end = 10.dp)
                        .size(60.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = selectedIds.size.toString(),
                            fontSize = 32.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            color = tsuBlue
                        )
                    }
                }

                Card(
                    modifier = Modifier
                        .padding(end = 20.dp)
                        .height(60.dp)
                        .weight(1f),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                    colors = CardDefaults.cardColors(containerColor = tsuBlue)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Построить маршрут",
                            fontSize = 18.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }

    @Composable
    private fun CardTemplate(
        attraction: Attraction,
        isSelected: Boolean,
        onCardClick: () -> Unit,
        onAddClick: () -> Unit
    ) {
        val tsuBlue = Color(standardTSUColor.toColorInt())
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .height(95.dp)
                .clickable { onCardClick() },
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(10.dp),
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
                        .padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = attraction.title,
                        fontSize = 14.sp,
                        lineHeight = 1.1.em,
                        fontFamily = standardTSUFont,
                        fontWeight = FontWeight.Bold,
                        color = tsuBlue,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        modifier = Modifier
                            .size(width = 170.dp, height = 28.dp)
                            .clickable { onAddClick() },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = tsuBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(Color.White, RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        tint = tsuBlue,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            Text(
                                modifier = Modifier.padding(start = 6.dp),
                                text = if (isSelected) "Добавлено в маршрут" else "Добавить в маршрут",
                                fontSize = 11.sp,
                                fontFamily = standardTSUFont,
                                fontWeight = FontWeight.Bold,
                                style = LocalTextStyle.current.copy(
                                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}