package com.example.tsuonspot.decisionTree

import com.example.alghoritms.Data.DataRow
import com.example.alghoritms.Data.DecisionNode
import com.example.alghoritms.Data.PredictionOutput
import com.example.alghoritms.Data.PredictionResult
import kotlin.math.log2

class DecisionTree {
    var root: DecisionNode? = null
        private set
    var featureNames: List<String> = emptyList()
        private set
    private var maxDepth: Int = 0
    private var minSamplesSplit: Int = 0
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

    fun fit(data: List<DataRow> , features: List<String>, maxDepth: Int, minSamplesSplit:Int) {
        this.maxDepth = maxDepth
        this.minSamplesSplit = minSamplesSplit
        this.featureNames = features

        val normalizedData = data.map { row ->
            DataRow(
                row.features.map { it.trim().lowercase() },
                row.label.trim().lowercase()
            )
        }
        root = buildTree(normalizedData, features.indices.toList(), 0)
    }

    fun buildTree(data: List<DataRow>, attributeIndices: List<Int>, currentDepth: Int ) : DecisionNode {
        val labels = data.map { it.label}.distinct()
        val distribute = data.groupingBy { it.label }.eachCount()
        val majority = distribute.maxByOrNull { it.value }?.key ?: "Unknown"

        if(labels.size == 1) {
            return DecisionNode(isLeaf = true, result = labels.first(), classDistribution = distribute)
        }

        if(attributeIndices.isEmpty() || data.isEmpty()) {
            return DecisionNode(isLeaf = true, result = majority, classDistribution = distribute)
        }

        if(currentDepth >= maxDepth) {
            return DecisionNode(isLeaf = true, result = majority, classDistribution = distribute)
        }

        if(data.size < minSamplesSplit) {
            return DecisionNode(isLeaf = true, result = majority, classDistribution = distribute)
        }

        val bestAttrIndex = selectBestAttr(data, attributeIndices)?: attributeIndices.first()
        val values = data.map {it.features[bestAttrIndex]}.distinct()
        val children = mutableMapOf<String, DecisionNode>()

        for (value in values) {
            val subset = data.filter { it.features[bestAttrIndex] == value }
            val remainingAttr = attributeIndices.filter { it != bestAttrIndex }

            children[value] = if (subset.isEmpty()) {
                DecisionNode(isLeaf = true, result = majority)
            } else {
                buildTree(subset, remainingAttr, currentDepth + 1)
            }
        }

        return DecisionNode(
            isLeaf = false,
            splitAttributeIndex = bestAttrIndex,
            children = children,
            classDistribution = distribute
        )
    }

    fun predict(features: List<String>, maxAlternatives: Int = 2) : PredictionOutput {
        val path = mutableListOf<DecisionNode>()
        var currentNode = root
        val normalizedFeatures = features.map { it.trim().lowercase() }
        val pathSteps = mutableListOf<String>()

        while (currentNode != null && !currentNode.isLeaf) {
            path.add(currentNode)
            val index = currentNode.splitAttributeIndex!!
            val value = normalizedFeatures.getOrNull(index)?: " "
            pathSteps.add("Проверка: ${featureNames[index]} == $value")
            currentNode = currentNode.children[value]
            if(currentNode == null) break
        }
        val mainLeaf = currentNode
        val mainDistance = normalizeDistribution(mainLeaf?.classDistribution ?: emptyMap())
        val mainRec = mainDistance.maxByOrNull {it.value}?.key ?: "Не найдено"
        pathSteps.add("Итоговое распределение: ${formatDistance(mainDistance)}")
        val mainResult = PredictionResult(mainRec, mainDistance[mainRec]?: 0.0, pathSteps)

        val alternatives = mutableListOf<PredictionResult>()
        for((depth, node) in path.withIndex()) {
            if(node.isLeaf || node.children.isEmpty()) {
                continue
            }

            val index = node.splitAttributeIndex!!
            val actualValue = normalizedFeatures.getOrNull(index)?: " "

            for((siblingValue, siblingNode) in node.children) {
                if(siblingValue == actualValue) {
                    continue
                }

                val allPath = mutableListOf<String>()

                for(i in 0 until depth) {
                    val previousNode = path[i]
                    val previousIndex = previousNode.splitAttributeIndex ?: -1
                    val previousValue = normalizedFeatures.getOrNull(previousIndex)
                    allPath.add("Проверка: ${featureNames[previousIndex]} == $previousValue")
                }
                allPath.add("Альтернатива: ${featureNames[index]} == $siblingValue")

                var tempNode = siblingNode
                while(tempNode != null && tempNode.isLeaf) {
                    if(tempNode.children.isEmpty()) break
                    val firstChild = tempNode.children.entries.firstOrNull()?: break
                    val nextIndex = tempNode.splitAttributeIndex?: -1
                    allPath.add("Проверка: ${featureNames.getOrNull(nextIndex)?: "unknown"} == ${firstChild.key}")
                    tempNode = firstChild.value
                }

                if(tempNode.isLeaf == true) {
                    val leafDistation = normalizeDistribution(tempNode.classDistribution)
                    val recommendation = leafDistation.maxByOrNull { it.value }?.key ?: continue
                    val chance = leafDistation[recommendation] ?: 0.0
                    if(recommendation != mainRec && alternatives.none {it.recommendation == recommendation}) {
                        allPath.add("Итог: $recommendation (${(chance*100).toInt()}%)")
                        alternatives.add(PredictionResult(recommendation, chance, allPath))
                    }
                }
            }

        }
        return PredictionOutput(main = mainResult,
            alternatives = alternatives.sortedByDescending { it.chance }.take(maxAlternatives))
    }

    private fun normalizeDistribution(dist: Map<String, Int>): Map<String, Double> {
        val total = dist.values.sum().toDouble()
        return if(total == 0.0) emptyMap() else dist.mapValues { it.value / total }
    }

    private fun formatDistance(distance: Map<String, Double>):String {
        return distance.map { (name, value) -> "$name: ${(value*100).toInt()}%" }.joinToString(", ")
    }

}