package com.example.alghoritms.clustering

import com.example.alghoritms.Data.Point

class EuclidianDistance: DistanceMetric {
    override fun calculate(point1: Point, point2: Point): Double {
        return point1.distanceTo(point2)
    }
}