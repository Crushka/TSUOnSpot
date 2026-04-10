package com.example.tsuonspot.AntAlghoritm

import com.example.alghoritms.Data.GridCell
import com.example.alghoritms.pathFind.GridAStar

class StarForACO(private val gridAStar: GridAStar) {

    fun computeMatrix(points: List<GridCell>): Array<DoubleArray> {
        val nodes = points.size
        return Array(nodes) { i ->
            DoubleArray(nodes) { j ->
                if (i == j) {
                    0.0
                } else {
                    calculateAStarDistance(points[i], points[j])
                }
            }
        }
    }

    fun calculateAStarDistance(from: GridCell, to: GridCell): Double {
        val start = if (from.isWalkable) from else gridAStar.findNearestWalkable(from.x, from.y) ?: from
        val end = if (to.isWalkable) to else gridAStar.findNearestWalkable(to.x, to.y) ?: to

        val (temp, cost) = gridAStar.findPath(start, end)
        return cost
    }
}