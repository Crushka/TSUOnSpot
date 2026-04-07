package com.example.alghoritms.clustering

import com.example.alghoritms.Data.Point

interface DistanceMetric {
    fun calculate(point1: Point, point2: Point):Double
}