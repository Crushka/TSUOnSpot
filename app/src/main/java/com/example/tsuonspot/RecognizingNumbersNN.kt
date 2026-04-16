package com.example.tsuonspot

import android.graphics.BitmapFactory
import android.graphics.Color
import org.json.JSONObject
import java.io.File
import kotlin.math.exp
import androidx.core.graphics.get

fun sigmoid(x: Double): Double = 1.0 / (1.0 + exp(-x))

fun loadWeights(fileName: String): Map<String, Array<DoubleArray>> {
    val data = JSONObject(File(fileName).readText())

    fun parseMatrix(key: String): Array<DoubleArray> {
        val outer = data.getJSONArray(key)
        return Array(outer.length()) { i ->
            val inner = outer.getJSONArray(i)
            DoubleArray(inner.length()) { j -> inner.getDouble(j) }
        }
    }

    return mapOf(
        "bias1" to parseMatrix("bias1"),
        "bias2" to parseMatrix("bias2"),
        "weights1" to parseMatrix("weights1"),
        "weights2" to parseMatrix("weights2")
    )
}

fun usingNN(weights: Map<String, Array<DoubleArray>>, imagePath: String): Int {
    val b1 = weights["bias1"]!!
    val b2 = weights["bias2"]!!
    val w1 = weights["weights1"]!!
    val w2 = weights["weights2"]!!


    val bitmap = BitmapFactory.decodeFile(imagePath)
    val image = DoubleArray(50 * 50)
    for (idx in 0 until 50 * 50) {
        val px = idx % 50
        val py = idx / 50
        val pixel = bitmap[px, py]
        val r = Color.red(pixel).toDouble()
        val g = Color.green(pixel).toDouble()
        val b = Color.blue(pixel).toDouble()
        image[idx] = 1.0 - (0.299 * r + 0.587 * g + 0.114 * b) / 255.0
    }


    val hidden = DoubleArray(w1.size) { i ->
        sigmoid(b1[i][0] + w1[i].indices.sumOf { k -> w1[i][k] * image[k] })
    }

    val output = DoubleArray(w2.size) { i ->
        sigmoid(b2[i][0] + w2[i].indices.sumOf { k -> w2[i][k] * hidden[k] })
    }

    return output.indices.maxByOrNull { output[it] } ?: -1
}

fun main() {
    val weights = loadWeights("data_for_nn.json")
    val result = usingNN(weights, "Test.jpg")
    println("NN suggests the CUSTOM number is: $result")
}