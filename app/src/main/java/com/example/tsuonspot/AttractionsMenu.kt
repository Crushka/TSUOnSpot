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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt

data class Attraction(
    val id: Int,
    val title: String,
    val icon: Int,
    val description: String,
    val type: String,
    val gridX: Int = 0,
    val gridY: Int = 0
)

class AttractionsMenu {
    @Composable
    fun DrawMenu(
        onAttractionClick: (Attraction) -> Unit = {},
        onBuildRoute: (List<Attraction>) -> Unit = {}
    ) {
        val attractionsList = remember { attractions }

        var selectedIds by remember { mutableStateOf(setOf<Int>()) }

        val currentList = attractionsList
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
                        .weight(1f)
                        .clickable {
                            val selected = currentList.filter { it.id in selectedIds }
                            if (selected.isNotEmpty()) onBuildRoute(selected)
                        },
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

@Composable
fun WaitingForStartHint() {
    val tsuBlue = Color(standardTSUColor.toColorInt())
    Box(
        modifier = Modifier.fillMaxSize().statusBarsPadding(),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .width(200.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = tsuBlue)
        ) {
            Text(
                text = "Нажмите на карту, чтобы выбрать точку старта",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = Color.White,
                fontFamily = standardTSUFont,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}