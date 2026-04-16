package com.example.tsuonspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tsuonspot.ui.theme.TSUOnSpotTheme

public const val standardTSUColor: String = "#0072BC"
private const val iconSize = 55
public val standardTSUFont = FontFamily(
    Font(R.font.calibri, FontWeight.Normal),
    Font(R.font.calibri_bold, FontWeight.Bold)
)

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TSUOnSpotTheme {
                var activeSection by remember { mutableStateOf<String?>(null) }
                val mapViewModel: MapViewModel = viewModel()
                val mapState by mapViewModel.mapState.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    DrawBackground()
                    Column(modifier = Modifier.fillMaxSize()) {
                        MapScreen(vm = mapViewModel)
                    }

                    if (!mapState.showClusters) {
                        Box(
                            modifier = Modifier
                                .statusBarsPadding()
                                .fillMaxSize()
                                .padding(end = 16.dp),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            MapLayerToggleButton(
                                currentLayer = mapState.mapLayer,
                                onLayerChange = { mapViewModel.setMapLayer(it) }
                            )
                        }
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Bottom
                        ) {
                            GeoIcon()
                            HudBar(onIconClick = { section -> activeSection = section })
                        }
                    }

                    Section(
                        activeSection = activeSection,
                        onDismiss = { activeSection = null },
                        onEatBuildRoute = { poi ->
                            mapViewModel.onPoiClick(poi)
                            mapViewModel.onBuildRouteToPoiRequested()
                            activeSection = null
                        },
                        onShowZones = {
                            activeSection = null
                            mapViewModel.enterClusterMode()
                        },
                        onBuildAttractionRoute = { selected ->
                            mapViewModel.onBuildAttractionRoute(selected)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun HudBarIconText(text: String) {
    Text(
        text = text,
        fontSize = 13.sp,
        fontFamily = standardTSUFont,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun DrawIcon(
    description: String,
    iconText: String,
    iconSource: Int,
    onClick: () -> Unit
) {
    val iconPadding = 10
    Column(
        modifier = Modifier
            .padding(iconPadding.dp, 0.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(
                    radius = 100.dp,
                    color = Color(standardTSUColor.toColorInt())
                )
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(iconSource),
            contentDescription = description,
            modifier = Modifier.size(iconSize.dp)
        )
        HudBarIconText(iconText)
    }
}

@Composable
private fun DrawBackground() {
    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = "Background",
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun HudBar(onIconClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 5.dp)
            .size(90.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(standardTSUColor.toColorInt())
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                DrawIcon("Attractions", "Прогулка",   R.drawable.attractions_icon) { onIconClick("walk") }
                DrawIcon("Goto",        "Маршрут",    R.drawable.goto_icon)         { onIconClick("route") }
                DrawIcon("Eat",         "Где поесть", R.drawable.eat_icon)          { onIconClick("eat") }
                DrawIcon("Account",     "Аккаунт",    R.drawable.account_icon)      { onIconClick("account") }
            }
        }
    }
}

@Composable
private fun GeoIcon() {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterEnd
    ) {
        Card(
            modifier = Modifier
                .padding(20.dp, 10.dp)
                .size(70.dp),
            shape = RoundedCornerShape(60.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(
                            radius = 100.dp,
                            color = Color(standardTSUColor.toColorInt())
                        )
                    ) { },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.geo_icon),
                    contentDescription = "Geo",
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 2.dp, y = 2.dp)
                )
            }
        }
    }
}