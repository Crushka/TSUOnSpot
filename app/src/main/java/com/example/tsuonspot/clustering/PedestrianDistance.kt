package com.example.alghoritms.clustering

import com.example.alghoritms.Data.Point
import kotlin.math.sqrt

class PedestrianDistance(
    private val cache: Map<Pair<Int, Int>, Double>,
    private val points: List<Point>
) : DistanceMetric {

    override fun calculate(point1: Point, point2: Point): Double {
        val i = points.indexOf(point1)
        val j = points.indexOf(point2)
        if (i == -1 || j == -1) return euclidean(point1, point2)
        val key = if (i <= j) i to j else j to i
        return cache[key] ?: euclidean(point1, point2)
    }

    private fun euclidean(p1: Point, p2: Point): Double {
        val dx = p1.x - p2.x;
        val dy = p1.y - p2.y
        return sqrt(dx * dx + dy * dy)
    }
}