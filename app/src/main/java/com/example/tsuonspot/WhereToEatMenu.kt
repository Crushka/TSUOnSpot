package com.example.tsuonspot

import androidx.compose.foundation.BorderStroke
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

@Composable
fun DrawEatMenu(
    onEatPlaceClick: (PointOfInterest) -> Unit = {},
    onBuildRoute: (PointOfInterest) -> Unit = {},
    onShowZones: () -> Unit = {}
    onFilterClick: () -> Unit = {}
) {
    val eatList = pointsOfInterest

    var selectedId by remember { mutableStateOf<Int?>(null) }
    var showFilter by remember { mutableStateOf(false) }

    val tsuBlue = Color(standardTSUColor.toColorInt())
    val hasSelection = selectedId != null

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 10.dp, bottom = 10.dp, top = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Card(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .height(40.dp)
                    .width(120.dp)
                    .clickable { onShowZones() },
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    contentColor = tsuBlue
                ),
                border = BorderStroke(1.5.dp, tsuBlue)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.zone_icon),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            modifier = Modifier.padding(start = 7.dp),
                            text = "Зоны еды",
                            fontSize = 15.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Card(
                onClick = { showFilter = true },
                modifier = Modifier
                    .padding(end = 20.dp)
                    .height(40.dp)
                    .width(110.dp)
                    .clickable {onFilterClick()},
                shape = RoundedCornerShape(14.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 13.dp),
                colors = CardDefaults.cardColors(
                    containerColor = tsuBlue,
                    contentColor = Color.White
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(R.drawable.filter_icon),
                            contentDescription = null,
                            modifier = Modifier.size(25.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text(
                            modifier = Modifier.padding(start = 7.dp),
                            text = "Фильтр",
                            fontSize = 15.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            if (showFilter) {
                DecisionTreeFilter(
                    onDismiss = { showFilter = false },
                    onResult = { res, path ->
                        println("Получено: $res")
                    }
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(eatList) { item ->
                    val isSelected = item.id == selectedId
                    CardTemplate(
                        attraction = item,
                        isSelected = isSelected,
                        onCardClick = { onEatPlaceClick(item) },
                        onAddClick = {
                            selectedId = if (isSelected) null else item.id
                        }
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 10.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(start = 20.dp, end = 20.dp)
                        .height(60.dp)
                        .width(400.dp)
                        .clickable(enabled = hasSelection) {
                            val poi = eatList.firstOrNull { it.id == selectedId }
                            if (poi != null) onBuildRoute(poi)
                        },
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (hasSelection) 13.dp else 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasSelection) tsuBlue else Color(standardTSUColor.toColorInt()),
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
}

@Composable
private fun CardTemplate(
    attraction: PointOfInterest,
    isSelected: Boolean,
    onCardClick: () -> Unit,
    onAddClick: () -> Unit
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())

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
            contentColor = tsuBlue
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(attraction.imageRes),
                contentDescription = null,
                modifier = Modifier
                    .size(75.dp)
                    .clip(shape = RoundedCornerShape(15.dp)),
                contentScale = ContentScale.Crop
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 7.dp, top = 10.dp),
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
                        .size(width = 100.dp, height = 30.dp)
                        .clickable { onAddClick() },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = tsuBlue,
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
                                    tint = tsuBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            modifier = Modifier.padding(start = 5.dp),
                            text = if (isSelected) "Выбрано" else "Выбрать",
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