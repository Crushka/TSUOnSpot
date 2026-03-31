package com.example.alghoritms.Data

import kotlin.math.abs
import kotlin.math.sqrt
data class Point(val x: Double, val y: Double, var clusterId: Int = -1) {
    fun distanceTo(other: Point) : Double {
        val dx =  this.x - other.x
        val dy = this.y - other.y
        return sqrt(dx*dx + dy*dy)
    }
}

data class Cluster(val id: Int, var centroid: Point? = null,
                       val points: MutableList<Point> = mutableListOf()) {
    fun addPoint(point: Point) {
        points.add(point)
    }

    fun clear() {
        points.clear()
    }
    fun recalculateCentroid() {
        if(points.isEmpty()) return
        val sumX = points.sumOf { it.x }
        val sumY = points.sumOf { it.y }
        centroid = Point(sumX / points.size, sumY/points.size)
    }
}

data class GridCell(val x: Int, val y: Int, val isWalkable: Boolean)