package com.example.tsuonspot.decisionTree

import com.example.alghoritms.Data.DataRow
import com.example.alghoritms.Data.DecisionNode
import kotlin.math.log2

class DecisionTree {
    private var root: DecisionNode? = null
    private var featureNames: List<String> = emptyList();

    private fun calculateEntropy(data: List<DataRow>): Double {
        if(data.isEmpty()) return 0.0;

        val labelCounts = data.groupingBy { it.label }.eachCount()
        val size = data.size.toDouble()

        return -labelCounts.values.sumOf { count ->
            val chance = count/size
            chance*log2(chance)
        }
    }

    private fun calculateGain(data: List<DataRow>, attrributeIndex: Int):Double {
        val totalEntropy = calculateEntropy(data)

        val groups = data.groupBy { it.features[attrributeIndex] }

        val entropy = groups.values.sumOf { subset ->
            val weight = subset.size.toDouble() / data.size
            weight * calculateEntropy(subset)
        }

        return totalEntropy - entropy
    }

    private fun selectBestAttr(data: List<DataRow>, attributeIndices: List<Int>): Int? {
        var bestGain = Double.NEGATIVE_INFINITY
        var bestIndex: Int? = null

        for(index in attributeIndices) {
            val gain = calculateGain(data, index)
            if(gain > bestGain) {
                bestGain = gain
                bestIndex = index
            }
        }
        return bestIndex
    }

    fun fit(data: List<DataRow> , features: List<String>) {
        featureNames = features
        val normalizedData = data.map { row ->
            DataRow(
                row.features.map { it.trim().lowercase() },
                row.label.trim().lowercase()
            )
        }
        root = buildTree(normalizedData, features.indices.toList())
    }

    fun buildTree(data: List<DataRow>, attributeIndices: List<Int> ) : DecisionNode {
        val labels = data.map { it.label}.distinct()

        if(labels.size == 1) {
            val distribute = data.groupingBy { it.label }.eachCount()
            return DecisionNode(isLeaf = true, result = labels.first(), classDistribution = distribute)
        }

        if(attributeIndices.isEmpty() || data.isEmpty()) {
            val distribute = data.groupingBy { it.label }.eachCount()
            val majority = distribute.maxByOrNull { it.value }?.key ?: "Unknown"
            return DecisionNode(isLeaf = true, result = majority, classDistribution = distribute)

        }

        val bestAttrIndex = selectBestAttr(data, attributeIndices)?: attributeIndices.first()
        val values = data.map {it.features[bestAttrIndex]}.distinct()
        val children = mutableMapOf<String, DecisionNode>()

        val majorityResult = data.groupingBy { it.label }.eachCount().maxByOrNull { it.value }?.key ?: "Unknown"

        for(value in values) {
            val subset = data.filter { it.features[bestAttrIndex] == value }
            val remainingAttr = attributeIndices.filter { it != bestAttrIndex }
            children[value] = if(subset.isEmpty()) {
                DecisionNode(isLeaf = true, result = majorityResult)
            }
            else {
                buildTree(subset, remainingAttr)
            }
        }

        return DecisionNode(isLeaf = false,
                            splitAttributeIndex = bestAttrIndex,
                            children = children
        )
    }

    fun predict(features: List<String>) : Pair<Map<String, Double>, List<String>> {
        val path = mutableListOf<String>()
        var currentNode = root
        val normalizedFeatures = features.map { it.trim().lowercase() }

        while (currentNode != null) {
            if(currentNode.isLeaf) {
                val total = currentNode.classDistribution.values.sum().toDouble()
                val chances = currentNode.classDistribution.mapValues { (temp, count) ->
                    count / total
                }
                path.add("Итоговое распределение: ${chances.map {(name, value) -> "$name: ${(value*100).toInt()}%"}}")
                return Pair(chances, path)
            }
            else {
                val attrIndex = currentNode.splitAttributeIndex!!
                val inputValue = if (attrIndex < normalizedFeatures.size) {
                    normalizedFeatures[attrIndex]
                } else ""
                path.add("Проверка: ${featureNames[attrIndex]} == $inputValue")

                currentNode = currentNode.children[inputValue]
                if (currentNode == null) {
                    path.add("Тупик")
                    return Pair(emptyMap(), path)
                }
            }
        }
        return Pair(emptyMap(), path)
    }
}