package com.example.tsuonspot

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import com.example.tsuonspot.neuralNetwork.usingNN
import com.example.tsuonspot.neuralNetwork.loadWeights
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import androidx.core.graphics.createBitmap

fun starResForSlot(rating: Int, slot: Int): Int {
    val fullStars  = (rating + 1) / 2
    val halfStar   = rating % 2 == 0
    return when {
        slot < fullStars            -> R.drawable.yellow_star
        slot == fullStars && halfStar -> R.drawable.half_star
        else                         -> R.drawable.grey_star
    }
}

@Composable
fun RatingStars(rating: Int, modifier: Modifier = Modifier, starSize: Int = 20) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (slot in 0 until 5) {
            Image(
                painter = painterResource(id = starResForSlot(rating, slot)),
                contentDescription = null,
                modifier = Modifier.size(starSize.dp)
            )
        }
    }
}

@Composable
fun DrawingCanvas(
    lines: List<List<Offset>>,
    onNewLine: (List<Offset>) -> Unit,
    modifier: Modifier = Modifier
) {
    val tsuBlue = Color(standardTSUColor.toColorInt())
    var currentLine by remember { mutableStateOf<List<Offset>>(emptyList()) }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(2.dp, tsuBlue, RoundedCornerShape(12.dp))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        currentLine = listOf(
                            Offset(
                                offset.x.coerceIn(0f, size.width.toFloat()),
                                offset.y.coerceIn(0f, size.height.toFloat())
                            )
                        )
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val clampedPos = Offset(
                            x = change.position.x.coerceIn(0f, size.width.toFloat()),
                            y = change.position.y.coerceIn(0f, size.height.toFloat())
                        )
                        currentLine = currentLine + clampedPos
                    },
                    onDragEnd  = {
                        if (currentLine.isNotEmpty()) onNewLine(currentLine)
                        currentLine = emptyList()
                    },
                    onDragCancel = { currentLine = emptyList() }
                )
            }
    ) {
        for (line in lines) {
            if (line.size < 2) continue
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(line.first().x, line.first().y)
                line.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, color = Color.Black,
                style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
        if (currentLine.size >= 2) {
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(currentLine.first().x, currentLine.first().y)
                currentLine.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(path, color = Color.Black,
                style = Stroke(width = 14f, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}

fun strokesToBitmapFile(
    lines: List<List<Offset>>,
    context: Context
): Pair<File, Bitmap> {
    val allPoints = lines.flatten()
    if (allPoints.isEmpty()) {
        val empty = createBitmap(50, 50)
        return Pair(File(context.cacheDir, "digit_input.jpg"), empty)
    }

    val minX = allPoints.minOf { it.x }
    val maxX = allPoints.maxOf { it.x }
    val minY = allPoints.minOf { it.y }
    val maxY = allPoints.maxOf { it.y }

    val digitWidth = maxX - minX
    val digitHeight = maxY - minY
    val centerX = (minX + maxX) / 2f
    val centerY = (minY + maxY) / 2f

    val maxSide = maxOf(digitWidth, digitHeight, 1f)
    val paddingFactor = 0.8f
    val viewPortSize = maxSide / paddingFactor

    val left = centerX - viewPortSize / 2f
    val top = centerY - viewPortSize / 2f

    val targetSize = 50
    val bitmap = createBitmap(targetSize, targetSize)
    val canvas = android.graphics.Canvas(bitmap)
    canvas.drawColor(android.graphics.Color.WHITE)

    val paint = Paint().apply {
        color = android.graphics.Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 3.5f
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    val scale = targetSize.toFloat() / viewPortSize

    for (line in lines) {
        if (line.size < 2) continue
        val path = AndroidPath()
        path.moveTo((line.first().x - left) * scale, (line.first().y - top) * scale)
        line.drop(1).forEach {
            path.lineTo((it.x - left) * scale, (it.y - top) * scale)
        }
        canvas.drawPath(path, paint)
    }

    val file = File(context.cacheDir, "digit_input.jpg")
    file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }

    return Pair(file, bitmap)
}

enum class RatingDialogStep { DRAW, CONFIRM }

@Composable
fun RatingDialog(
    visible: Boolean,
    weightsFile: String?,
    onDismiss: () -> Unit,
    onRatingConfirmed: (Int) -> Unit
) {
    AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {

        val tsuBlue  = Color(standardTSUColor.toColorInt())
        val context  = LocalContext.current
        val scope    = rememberCoroutineScope()

        var lines     by remember { mutableStateOf<List<List<Offset>>>(emptyList()) }
        var canvasSize by remember { mutableStateOf(IntSize.Zero) }
        var step      by remember { mutableStateOf(RatingDialogStep.DRAW) }
        var nnResult  by remember { mutableStateOf<Int?>(null) }
        var isRunning by remember { mutableStateOf(false) }

        // var debugBitmap by remember { mutableStateOf<Bitmap?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.55f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 360.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Оцените место",
                            fontSize = 17.sp,
                            fontFamily = standardTSUFont,
                            fontWeight = FontWeight.Bold,
                            color = tsuBlue
                        )
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .background(tsuBlue.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                .clickable { onDismiss() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("✕", fontSize = 14.sp, color = tsuBlue,
                                fontFamily = standardTSUFont, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text = "Нарисуйте цифру для оценки (0–9)",
                        fontSize = 13.sp,
                        fontFamily = standardTSUFont,
                        fontWeight = FontWeight.Bold,
                        color = tsuBlue,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(12.dp))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val side = maxWidth
                        DrawingCanvas(
                            lines = lines,
                            onNewLine = { newLine ->
                                lines = lines + listOf(newLine)
                                if (step == RatingDialogStep.CONFIRM) {
                                    step = RatingDialogStep.DRAW
                                    nnResult = null
                                }
                            },
                            modifier = Modifier
                                .size(side)
                                .onSizeChanged { canvasSize = it }
                                .background(Color(0xFFF8F8F8), RoundedCornerShape(12.dp))
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    if (step == RatingDialogStep.CONFIRM && nnResult != null) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Результат: $nnResult",
                                fontFamily = standardTSUFont,
                                fontWeight = FontWeight.Bold,
                                color = tsuBlue
                            )

                            Spacer(Modifier.height(8.dp))
                            RatingStars(rating = nnResult!!, starSize = 28)
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    when (step) {
                        RatingDialogStep.DRAW -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { lines = emptyList() },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, tsuBlue)
                                ) {
                                    Text("Очистить", fontSize = 13.sp,
                                        fontFamily = standardTSUFont,
                                        fontWeight = FontWeight.Bold, color = tsuBlue)
                                }
                                Button(
                                    onClick = {
                                        if (lines.isEmpty() || canvasSize == IntSize.Zero) return@Button
                                        isRunning = true
                                        scope.launch {
                                            val (file, bmp) = withContext(Dispatchers.Default) {
                                                strokesToBitmapFile(lines, context)
                                            }

                                            val result = withContext(Dispatchers.Default) {
                                                try {
                                                    if (weightsFile != null && File(weightsFile).exists()) {
                                                        val weights = loadWeights(weightsFile)
                                                        usingNN(weights, file.absolutePath)
                                                    } else {
                                                        (0..9).random()
                                                    }
                                                } catch (e: Exception) {
                                                    -1
                                                }
                                            }
                                            nnResult = result
                                            step = RatingDialogStep.CONFIRM
                                            isRunning = false
                                        }
                                    },
                                    enabled = !isRunning && lines.isNotEmpty(),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = tsuBlue)
                                ) {
                                    if (isRunning) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = Color.White, strokeWidth = 2.dp)
                                    } else {
                                        Text(
                                            text = "Подтвердить",
                                            fontSize = 13.sp,
                                            fontFamily = standardTSUFont,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        RatingDialogStep.CONFIRM -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        lines    = emptyList()
                                        nnResult = null
                                        step     = RatingDialogStep.DRAW
                                    },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.5.dp, tsuBlue)
                                ) {
                                    Text("Перерисовать", fontSize = 13.sp,
                                        fontFamily = standardTSUFont,
                                        fontWeight = FontWeight.Bold, color = tsuBlue)
                                }
                                Button(
                                    onClick = { nnResult?.let { onRatingConfirmed(it) } },
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = tsuBlue)
                                ) {
                                    Text("Принять", fontSize = 13.sp,
                                        fontFamily = standardTSUFont,
                                        fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}