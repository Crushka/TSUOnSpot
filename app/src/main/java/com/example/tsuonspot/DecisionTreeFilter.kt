package com.example.tsuonspot

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontWeight.Companion.Bold
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.graphics.toColorInt
import com.example.alghoritms.Data.CSVData
import com.example.alghoritms.Data.DecisionNode
import com.example.alghoritms.Data.PredictionOutput
import com.example.tsuonspot.decisionTree.CSVParser
import com.example.tsuonspot.decisionTree.DecisionTree


data class FilterStates(
    val location: String = "",
    val budget: String = "",
    val time: String = "",
    val cuisine: String = "",
    val queue: String = "",
    val weather: String = ""
)
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
    output: PredictionOutput,
    onBack: () -> Unit,
    onConfirm: (String) -> Unit,
    onShowTree: () -> Unit
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
                text = output.main.recommendation.replace("_", " ").replaceFirstChar { it.uppercase() },
                fontSize = 28.sp,
                fontFamily = standardTSUFont,
                fontWeight = Bold,
                color = Color(standardTSUColor.toColorInt()),
                modifier = Modifier.padding(24.dp)
            )
        }

        Button(
            onClick = onShowTree,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0072BC))
        ) {
            Text("Показать дерево решений", color = Color.White, fontSize = 16.sp)
        }

        if(output.alternatives.isNotEmpty()) {
            Text(
                text = "Другие возможные варианты",
                fontSize = 16.sp,
                fontFamily = standardTSUFont,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            output.alternatives.forEach { alternative ->
                Card(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.Gray.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alternative.recommendation.replace("_", " ").replaceFirstChar { it.uppercase() },
                            fontWeight = Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f),
                            fontFamily = standardTSUFont
                        )

                        Button(
                            onClick = {onConfirm(alternative.recommendation)},
                            modifier = Modifier.height(35.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(standardTSUColor.toColorInt())),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "Маршрут", fontSize = 12.sp, color = Color.White
                            )
                        }
                    }
                }
            }
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
            items(output.main.path) { step ->
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Назад")
            }

            Button(
                onClick = { onConfirm(output.main.recommendation) },
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
    filterState: FilterStates,
    onFilterStateChange: (FilterStates) -> Unit,
    onResult: (recommendation: String, path: List<String>) -> Unit,
    onBackToFilter: () -> Unit
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())

    val (featureNames, dataRows) = remember {
        CSVParser.parse(CSVData.data)
    }

    val decisionTree = remember {
        DecisionTree().apply {
            fit(dataRows, featureNames, 4, 5)
        }
    }

    val locations = listOf(
        "main_building" to "Главный корпус",
        "second_building" to "Второй корпус",
        "campus_center" to "Центр Культуры",
        "sports_building" to "Спорт-корпус"
    )
    val budgets = listOf(
        "low" to "Низкий",
        "medium" to "Средний",
        "high" to "Высокий"
    )
    val times = listOf(
        "short" to "Мало времени",
        "medium" to "Средне",
        "long" to "Много времени"
    )
    val foodTypes = listOf(
        "full_meal" to "Полноценный обед",
        "bakery" to "Пекарня",
        "pancakes" to "Блины",
        "shaurma" to "Шаурма",
        "coffee" to "Кофе",
        "grocery" to "Продуктовый магазин",
        "asian" to "Китайская/Азиатская кухня",
        "fast_food" to "Фаст-фуд",
        "restaurant" to "Ресторан",
        "pizza" to "Пиццерия",
        "grill" to "Гриль-бар",
        "cafe" to "Кафе"
    )
    val queueTolerances = listOf(
        "low" to "Малая",
        "medium" to "Средняя"
    )
    val weathers = listOf(
        "good" to "Хорошая",
        "bad" to "Плохая"
    )

    var result by remember { mutableStateOf(false) }
    var predictionOutput by remember { mutableStateOf<PredictionOutput?>(null) }
    var showTreeWindow by remember { mutableStateOf(false) }

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
                        selected = filterState.location,
                        onSelect = { onFilterStateChange(filterState.copy(location = it)) }
                    )
                }

                item{
                    FilterSection(
                        label = "Ваш бюджет?",
                        options = budgets,
                        selected = filterState.budget,
                        onSelect = { onFilterStateChange(filterState.copy(budget = it))}
                    )
                }

                item{
                    FilterSection(
                        label = "Сколько у вас есть времени?",
                        options = times,
                        selected = filterState.time,
                        onSelect = { onFilterStateChange(filterState.copy(time = it))}
                    )
                }

                item{
                    FilterSection(
                        label = "Какой тип кухни вас интерисует?",
                        options = foodTypes,
                        selected = filterState.cuisine,
                        onSelect = { onFilterStateChange(filterState.copy(cuisine = it))}
                    )
                }

                item{
                    FilterSection(
                        label = "Сколько вы готовы ждать в очереди?",
                        options = queueTolerances,
                        selected = filterState.queue,
                        onSelect = { onFilterStateChange(filterState.copy(queue = it))}
                    )
                }

                item{
                    FilterSection(
                        label = "Какая погода на улице?",
                        options = weathers,
                        selected = filterState.weather,
                        onSelect = { onFilterStateChange(filterState.copy(weather = it))}
                    )
                }

                item {
                    Button(
                        onClick = {
                            val features = listOf(
                                filterState.location, filterState.budget, filterState.time,
                                filterState.cuisine, filterState.queue, filterState.weather
                            )
                            predictionOutput = decisionTree.predict(features, maxAlternatives = 2)
                            result = true
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = tsuBlue),
                        enabled = filterState.location.isNotBlank() && filterState.budget.isNotBlank()
                                && filterState.time.isNotBlank() && filterState.cuisine.isNotBlank()
                                && filterState.queue.isNotBlank() && filterState.weather.isNotBlank()
                    ) {
                        Text("Рекомендовать место", color = Color.White)
                    }
                }
            }
        }
        else {
            if (showTreeWindow && predictionOutput != null) {
                val currentFeatures = listOf(
                    filterState.location,
                    filterState.budget,
                    filterState.time,
                    filterState.cuisine,
                    filterState.queue,
                    filterState.weather
                )
                DecisionTreeVisualizer(
                    root = decisionTree.root,
                    featureNames = decisionTree.featureNames,
                    userInput = currentFeatures,
                    onDismiss = { showTreeWindow = false }
                )
            }

            predictionOutput?.let { output ->
                ResultScreen(
                    output = output,
                    onBack = {result = false},
                    onConfirm = { selectedRec ->
                        onResult(selectedRec, emptyList())
                    },
                    onShowTree = {showTreeWindow = true}
                )
            }
        }
    }
}

fun getReadableName(slug: String): String {
    val lower = slug.lowercase().trim()
    val poi = findPoiByCsvRecommendation(lower)
    if (poi != null) return poi.title

    return when (lower) {
        "location" -> "Локация"
        "budget" -> "Бюджет"
        "time_available" -> "Доступное время"
        "food_type" -> "Тип еды"
        "queue_tolerance" -> "Очередь"
        "weather" -> "Погода"
        "main_building" -> "Главный корпус"
        "second_building" -> "Второй корпус"
        "campus_center" -> "Центр Культуры"
        "sports_building" -> "Спорт-корпус"
        "low" -> "Низкий"
        "medium" -> "Средний"
        "high" -> "Высокий"
        "short" -> "Мало"
        "long" -> "Много"
        "full_meal" -> "Обед"
        "bakery" -> "Пекарня"
        "pancakes" -> "Блины"
        "shaurma" -> "Шаурма"
        "coffee" -> "Кофе"
        "grocery" -> "Магазин"
        "asian" -> "Азиатская"
        "fast_food" -> "Фаст-фуд"
        "restaurant" -> "Ресторан"
        "pizza" -> "Пицца"
        "grill" -> "Гриль"
        "cafe" -> "Кафе"
        "good" -> "Хорошая"
        "bad" -> "Плохая"

        else -> slug.replace("_", " ").replaceFirstChar { it.uppercase() }
    }
}

fun findActivePathNodes(root: DecisionNode?, userInput: List<String>): Set<DecisionNode> {
    val path = mutableSetOf<DecisionNode>()
    var current = root
    while (current != null) {
        path.add(current)
        if (current.isLeaf) break

        val index = current.splitAttributeIndex ?: break
        val value = userInput.getOrNull(index)?.lowercase()?.trim() ?: ""
        current = current.children[value]
    }
    return path
}

@Composable
fun DecisionTreeVisualizer(
    root: DecisionNode?,
    featureNames: List<String>,
    userInput: List<String>,
    onDismiss: () -> Unit
) {
    if (root == null) { onDismiss(); return }

    val nodeWidth = 260f
    val nodeHeight = 140f
    val headerHeight = 45f
    val tsuBlue = Color(0xFF0072BC)
    val highlightColor = Color(0xFF4CAF50)
    val inactiveColor = Color.LightGray
    val verticalGap = 240f
    val leafGap = 280f
    val padding = 100f

    val activePathNodes = remember(root, userInput) {
        findActivePathNodes(root, userInput)
    }

    val layoutNodes = remember(root) {
        calculateTreeLayoutUpdated(root, nodeWidth, leafGap, verticalGap)
    }

    val maxX = layoutNodes.maxOfOrNull { it.x } ?: 0f
    val maxY = layoutNodes.maxOfOrNull { it.y } ?: 0f
    val canvasWidth = (maxX + nodeWidth + padding * 2).dp
    val canvasHeight = (maxY + nodeHeight + padding * 2).dp
    val textMeasurer = rememberTextMeasurer()

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Box(modifier = Modifier
            .fillMaxSize()
            .background(Color.White)) {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("ЗАКРЫТЬ", color = tsuBlue, fontWeight = Bold)
            }

            Box(modifier = Modifier
                .fillMaxSize()
                .padding(top = 60.dp)
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState())
                .width(canvasWidth)
                .height(canvasHeight)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    layoutNodes.forEach { pos ->
                        pos.parent?.let { parentPos ->
                            val isActivePath = activePathNodes.contains(pos.node) &&
                                    activePathNodes.contains(parentPos.node)

                            drawLine(
                                color = if (isActivePath) highlightColor else inactiveColor,
                                start = Offset(parentPos.x + padding + nodeWidth / 2,
                                                parentPos.y + padding + nodeHeight),
                                end = Offset(pos.x + padding + nodeWidth / 2, pos.y + padding),
                                strokeWidth = if (isActivePath) 8f else 3f
                            )
                        }
                    }

                    layoutNodes.forEach { pos ->
                        val centerX = pos.x + padding
                        val centerY = pos.y + padding
                        val isLeaf = pos.node.isLeaf
                        val isActive = activePathNodes.contains(pos.node)

                        val headerText = if (isLeaf) "Рекомендуем:"
                        else getReadableName(featureNames.getOrNull(pos.node.splitAttributeIndex ?: 0) ?: "")
                        val bodyText = getReadableName(
                            if (isLeaf) pos.node.result ?: ""
                            else userInput.getOrNull(pos.node.splitAttributeIndex ?: 0) ?: ""
                        )

                        drawRoundRect(
                            color = Color.White,
                            topLeft = Offset(centerX, centerY),
                            size = Size(nodeWidth, nodeHeight),
                            cornerRadius = CornerRadius(15f, 15f)
                        )

                        drawRoundRect(
                            color = if (isActive) highlightColor else inactiveColor,
                            topLeft = Offset(centerX, centerY),
                            size = Size(nodeWidth, nodeHeight),
                            cornerRadius = CornerRadius(15f, 15f),
                            style = Stroke(width = if (isActive) 6f else 2f)
                        )

                        val measuredHeader = textMeasurer.measure(
                            text = headerText,
                            style = TextStyle(fontSize = 11.sp, color = Color.Gray)
                        )
                        drawText(
                            textLayoutResult = measuredHeader,
                            topLeft = Offset(centerX + 15f, centerY + 10f)
                        )

                        val fontSize = if (bodyText.length > 20) 12.sp else 15.sp

                        val measuredBody = textMeasurer.measure(
                            text = bodyText,
                            style = TextStyle(
                                fontSize = fontSize,
                                fontWeight = Bold,
                                textAlign = TextAlign.Center,
                                color = if (isActive) (if (isLeaf) tsuBlue else Color.Black) else inactiveColor
                            ),
                            constraints = androidx.compose.ui.unit.Constraints(maxWidth = (nodeWidth - 30).toInt())
                        )

                        val bodyX = centerX + (nodeWidth - measuredBody.size.width) / 2
                        val bodyY = centerY + headerHeight + (nodeHeight - headerHeight - measuredBody.size.height) / 2

                        drawText(
                            textLayoutResult = measuredBody,
                            topLeft = Offset(bodyX, bodyY)
                        )
                    }
                }
            }
        }
    }
}
data class TreeNodePos(
    val node: DecisionNode,
    var x: Float,
    val y: Float,
    val parent: TreeNodePos? = null
)
private fun calculateTreeLayoutUpdated(
    root: DecisionNode,
    nodeWidth: Float,
    leafGap: Float,
    depthGap: Float
): List<TreeNodePos> {
    val result = mutableListOf<TreeNodePos>()
    var leafCounter = 0

    fun traverse(node: DecisionNode, depth: Int, parent: TreeNodePos?): TreeNodePos {
        val y = depth * depthGap
        val currentPos = TreeNodePos(node, 0f, y, parent)

        if (node.isLeaf || node.children.isEmpty()) {
            currentPos.x = leafCounter * leafGap
            leafCounter++
        } else {
            val childrenPosList = mutableListOf<TreeNodePos>()
            node.children.forEach { (_, childNode) ->
                val childPos = traverse(childNode, depth + 1, currentPos)
                childrenPosList.add(childPos)
                result.add(childPos)
            }
            val firstX = childrenPosList.first().x
            val lastX = childrenPosList.last().x
            currentPos.x = (firstX + lastX) / 2
        }

        if (node === root) result.add(currentPos)

        return currentPos
    }

    val finalRoot = traverse(root, 0, null)
    if (!result.contains(finalRoot)) result.add(finalRoot)

    return result
}