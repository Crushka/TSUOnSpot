package com.example.tsuonspot.decisionTree

import com.example.alghoritms.Data.DataRow

object CSVParser {

    fun parse(csvContent: String,) : Pair<List<String>, List<DataRow>> {
        val lines = csvContent.split("\n").map { it.trim()}.filter { !it.isEmpty() }
        if(lines.isEmpty())
            return Pair(emptyList(), emptyList())
        val headers = lines[0].split(",").map { it.trim() }
        val featureNames = headers.dropLast(1)

        val dataRows = lines.drop(1).map { line ->
            val parts = line.split(",").map { it.trim() }
            val features = parts.dropLast(1).map { it.trim().lowercase() }
            val label = parts.last().lowercase()
            DataRow(features, label)
        }
        return Pair(featureNames, dataRows)
    }

}