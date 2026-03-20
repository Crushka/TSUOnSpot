package com.example.tsuonspot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tsuonspot.ui.theme.TSUOnSpotTheme
import androidx.core.graphics.toColorInt

private const val standartTSUColor: String = "#0072BC"
private const val iconSize = 55
private val standartTSUFont = FontFamily(
    Font(R.font.calibri, FontWeight.Normal),
    Font(R.font.calibri_bold, FontWeight.Bold)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TSUOnSpotTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Image(painter = painterResource(id = R.drawable.map),
                            contentDescription = "Map",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(900.dp)
                        )
                    }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        GeoIcon()
                        HudBar()
                    }
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
        fontFamily = standartTSUFont,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun HudBar() {
    Card(
        modifier = Modifier.fillMaxWidth()
            .padding(20.dp)
            .size(90.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(standartTSUColor.toColorInt())
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(5.dp)
        ) {
            val iconPadding = 10
            Row(modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically) {

                Column(modifier = Modifier.padding(
                    iconPadding.dp,
                    0.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.attractions_icon),
                        contentDescription = "Attractions",
                        modifier = Modifier.size(iconSize.dp)
                    )
                    HudBarIconText("Прогулка")
                }

                Column(modifier = Modifier.padding(
                    iconPadding.dp,
                    0.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.goto_icon),
                        contentDescription = "Goto",
                        modifier = Modifier.size(iconSize.dp)
                    )
                    HudBarIconText("Маршрут")
                }

                Column(modifier = Modifier.padding(
                    iconPadding.dp,
                    0.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.eat_icon),
                        contentDescription = "Eat",
                        modifier = Modifier.size(iconSize.dp)
                    )
                    HudBarIconText("Где поесть")
                }

                Column(modifier = Modifier.padding(
                    iconPadding.dp,
                    0.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.account_icon),
                        contentDescription = "Account",
                        modifier = Modifier.size(iconSize.dp)
                    )
                    HudBarIconText("Аккаунт")
                }

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
                .padding(20.dp, 0.dp)
                .size(70.dp),
            shape = RoundedCornerShape(60.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 7.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White,
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
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