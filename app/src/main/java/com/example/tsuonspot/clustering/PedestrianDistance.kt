package com.example.tsuonspot.clustering

import com.example.alghoritms.Data.Point
import com.example.alghoritms.clustering.DistanceMetric
import com.example.alghoritms.pathFind.GridAStar
import kotlin.math.roundToInt

class PedestrianDistance(
    private val gridAStar: GridAStar,
    private val scaleX: Double = 1.0,
    private val scaleY: Double = 1.0,
    private val offsetX: Int = 0,
    private val offsetY: Int = 0): DistanceMetric {
    override fun calculate(point1: Point, point2: Point): Double {
        val x1 = (point1.x * scaleX).roundToInt() + offsetX
        val y1 = (point1.y * scaleY).roundToInt() + offsetY
        val x2 = (point2.x * scaleX).roundToInt() + offsetX
        val y2 = (point2.y * scaleY).roundToInt() + offsetY

        val start = gridAStar.findNearestWalkable(x1, y1)
        val end = gridAStar.findNearestWalkable(x2, y2)

        if(start == null || end == null) {
            return point1.distanceTo(point2) * 1.5
        }

        return try {
            val (temp, cost) = gridAStar.findPath(start, end)
            cost
        } catch (error: IllegalArgumentException) {
            point1.distanceTo(point2) * 2.0
        }
    }
}