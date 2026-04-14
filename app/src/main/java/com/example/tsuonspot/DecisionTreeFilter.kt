package com.example.tsuonspot

import android.R
import android.renderscript.Allocation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
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
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import androidx.room.util.TableInfo
import com.example.alghoritms.Data.CSVData
import com.example.tsuonspot.AntAlghoritm.StarForACO
import com.example.tsuonspot.decisionTree.CSVParser
import com.example.tsuonspot.decisionTree.DecisionTree
import kotlinx.coroutines.selects.selectUnbiased
import java.util.logging.Filter

@Composable
fun FilterSection(
    label: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit
) {
    Column{
        Text(
            text = label,
            fontSize = 14.sp,
            fontFamily = standardTSUFont,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { (value, displayName) ->
                FilterChip(
                    selected = selected == value,
                    onClick = {onSelect(value)},
                    label = {Text(displayName, fontSize = 12.sp)},
                    modifier = Modifier.wrapContentWidth(),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(standardTSUColor.toColorInt()),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun ResultScreen(
    recommendation: String,
    decisionPath: List<String>,
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Рекомендуем:",
            fontSize = 18.sp,
            fontFamily = standardTSUFont,
            color = Color(standardTSUColor.toColorInt())
        )

        Card(
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(standardTSUColor.toColorInt()).copy(alpha = 0.1f))
        ) {
            Text(
                text = recommendation.replace("_", " ").replaceFirstChar { it.uppercase() },
                fontSize = 28.sp,
                fontFamily = standardTSUFont,
                fontWeight = Bold,
                color = Color(standardTSUColor.toColorInt()),
                modifier = Modifier.padding(24.dp)
            )
        }

        Text(
            text = "Путь решения:",
            fontSize = 16.sp,
            fontFamily = standardTSUFont,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(decisionPath) { step ->
                Card (
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(standardTSUColor.toColorInt()).copy(alpha = 0.1f))
                ) {
                    Text(
                        text = step,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp,
                        fontFamily = standardTSUFont
                    )
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color(standardTSUColor.toColorInt()))
            ) {
                Text("Построить маршрут", color = Color.White)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecisionTreeFilter(
    onDismiss: () -> Unit,
    onResult: (recommendation: String, path: List<String>) -> Unit
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())

    val (featureNames, dataRows) = remember {
        CSVParser.parse(CSVData.data)
    }

    val decisionTree = remember {
        DecisionTree().apply {
            fit(dataRows, featureNames)
        }
    }

    var selectedLocation by remember { mutableStateOf("") }
    var selectedBudget by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var selectedCuisine by remember { mutableStateOf("") }
    var selectedQueue by remember { mutableStateOf("") }
    var selectedWeather by remember {mutableStateOf("")}

    val locations = listOf("campus_center" to "Центр Культуры", "main_building" to "Главный корпус",
        "second_building" to "Второй Корпус")
    val budgets = listOf("low" to "Низкий", "medium" to "Средний", "high" to "Высокий")
    val times = listOf("short" to "От 10 до 20 минут", "medium" to "От 20 до 40 минут",
        "high" to "Никуда не тороплюсь")
    val foodTypes = listOf("pancakes" to "Блины", "full_meal" to "Полноценный обед", "coffee" to "Кофе",
        "fast_food" to "Фаст-Фуд", "shaurma" to "Шаурма", "snacks" to "Снэки", "gastromarket" to "Гастромаркет",
        "bakery" to "Пекарня", "street_food" to "Уличная еда", "asian" to "Азиатская кухня",
        "cafe" to "Кафе", "restaurant" to "Ресторан", "bistro" to "Кафе Бистро", "grocery" to "Продуктовый магазин")
    val queueTolerances = listOf("low" to "Нет времени", "medium" to "Могу немножко постоять",
        "high" to "Да пусть хоть с улицы тянется")
    val weathers = listOf("good" to "Хорошая", "bad" to "Плохая")
    var result by remember { mutableStateOf(false) }
    var recommendation by remember { mutableStateOf("") }
    var decisionPath by remember { mutableStateOf<List<String>>(emptyList()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        if(!result) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item{
                    Text(
                        text = "Фильтр для выбора места обеда",
                        fontSize = 20.sp,
                        fontFamily = standardTSUFont,
                        color = tsuBlue
                    )
                }

                item{
                    FilterSection(
                        label = "Где вы находитесь?",
                        options = locations,
                        selected = selectedLocation,
                        onSelect = {selectedLocation = it}
                    )
                }

                item{
                    FilterSection(
                        label = "Ваш бюджет?",
                        options = budgets,
                        selected = selectedBudget,
                        onSelect = {selectedBudget = it}
                    )
                }

                item{
                    FilterSection(
                        label = "Сколько у вас есть времени?",
                        options = times,
                        selected = selectedTime,
                        onSelect = {selectedTime = it}
                    )
                }

                item{
                    FilterSection(
                        label = "Какой тип кухни вас интерисует?",
                        options = foodTypes,
                        selected = selectedCuisine,
                        onSelect = {selectedCuisine = it}
                    )
                }

                item{
                    FilterSection(
                        label = "Сколько вы готовы ждать в очереди?",
                        options = queueTolerances,
                        selected = selectedQueue,
                        onSelect = {selectedQueue = it}
                    )
                }

                item{
                    FilterSection(
                        label = "Какая погода на улице?",
                        options = weathers,
                        selected = selectedWeather,
                        onSelect = {selectedWeather = it}
                    )
                }

                item {
                    Button(
                        onClick = {
                            val features = listOf(
                                selectedLocation,
                                selectedBudget,
                                selectedTime,
                                selectedCuisine,
                                selectedQueue,
                                selectedWeather
                            )
                            val (chances, path) = decisionTree.predict(features)
                            recommendation = chances.maxByOrNull { it.value }?.key ?: "Не найдено"
                            decisionPath = path
                            result = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
                        enabled = selectedLocation.isNotBlank() && selectedBudget.isNotBlank()
                                && selectedTime.isNotBlank() && selectedCuisine.isNotBlank()
                                && selectedQueue.isNotBlank() && selectedWeather.isNotBlank()
                    ) {
                        Text("Рекомендовать место", color = Color.White)
                    }
                }
            }
        }
        else {
            ResultScreen(
                recommendation = recommendation,
                decisionPath = decisionPath,
                onBack = {result = false},
                onConfirm = {
                    onResult(recommendation, decisionPath)
                    onDismiss()
                }
            )
        }
    }
}
