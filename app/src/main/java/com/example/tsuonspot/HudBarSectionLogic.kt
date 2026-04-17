package com.example.tsuonspot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt


class HudBarSectionLogic {}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Section(
    activeSection: String?,
    onDismiss: () -> Unit,
    onShowZones: () -> Unit = {},
    onBuildAttractionRoute: (List<Attraction>) -> Unit = {},
    onEatBuildRoute: (PointOfInterest) -> Unit = {}
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    val topGap = screenHeight * 0.1f
    val attractionsMenu = AttractionsMenu()
    var showFilterSheet by remember { mutableStateOf(false) }
    var filterState by remember { mutableStateOf(FilterStates())}
    var showResultScreen by remember { mutableStateOf(false) }

    if (activeSection != null) {
        ModalBottomSheet(
            onDismissRequest = { onDismiss() },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = Color.Transparent,
            contentColor = Color(standardTSUColor.toColorInt()),
            dragHandle = { BottomSheetDefaults.DragHandle(color = Color.Transparent) },
            contentWindowInsets = {
                WindowInsets(top = topGap, bottom = 0.dp)
            }
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color(standardTSUColor.toColorInt())
                    ),
                    shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .size(50.dp, 5.dp)
                                .background(
                                    color = Color(standardTSUColor.toColorInt()).copy(alpha = 0.5f),
                                    shape = CircleShape
                                )
                        )
                        when (activeSection) {
                            "walk" -> attractionsMenu.DrawMenu(
                                onAttractionClick = {},
                                onBuildRoute = { selected ->
                                    onBuildAttractionRoute(selected)
                                    onDismiss()
                                }
                            )
                            "route"   -> Text("Секция Маршрут", Modifier.padding(10.dp))
                            "eat"     -> DrawEatMenu(
                                onBuildRoute = { poi ->
                                    onEatBuildRoute(poi)
                                    onDismiss()
                                },
                                onShowZones = {
                                    onShowZones()
                                    onDismiss()
                                },
                                onFilterClick = {showFilterSheet = true}
                            )
                            "account" -> Text("Личный кабинет", Modifier.padding(10.dp))
                        }
                    }
                }
            }
        }
    }
    if (showFilterSheet) {
        DecisionTreeFilter(
            onDismiss = { showFilterSheet = false },
            filterState = filterState,
            onFilterStateChange = { newState -> filterState = newState },
            onBackToFilter = { showResultScreen = false },
            onResult = { recommendation, path ->
                val foundPoi = findPoiByCsvRecommendation(recommendation)

                if (foundPoi != null) {
                    showFilterSheet = false
                    onDismiss()
                    onEatBuildRoute(foundPoi)
                } else {
                    showFilterSheet = false
                }
            }
        )
    }
}