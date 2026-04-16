package com.example.tsuonspot.AntAlghoritm

import com.example.alghoritms.Data.ACOdata
import com.example.alghoritms.Data.GridCell
import com.example.alghoritms.Data.RouteResult
import com.example.alghoritms.pathFind.GridAStar

class RouteFinder(
    private val grid: Array<Array<Int>>,
    private val gridAStar: GridAStar,
    private val acoData: ACOdata = ACOdata()
    ) {
    private val acoStar = StarForACO(gridAStar)

    private fun buildFullPath(orderedPoints: List<GridCell>): List<GridCell> {
        val result = mutableListOf<GridCell>()

        for(i in 0 until orderedPoints.size - 1) {
            val from = orderedPoints[i]
            val to = orderedPoints[i + 1]

            try {
                val start = gridAStar.findNearestWalkable(from.x, from.y)
                    ?: GridCell(from.x, from.y, true)
                val end = gridAStar.findNearestWalkable(to.x, to.y)
                    ?: GridCell(to.x, to.y, true)

                val (path, temp) = gridAStar.findPath(start, end)

                if(result.isEmpty()) {
                    result.addAll(path)
                }
                else  {
                    result.addAll(path.drop(1))
                }
            } catch (error: Exception) {
                result.add(GridCell(to.x, to.y, true))
            }
        }
        return result
    }

    private fun calculatePathLength(path: List<Int>, distMatrix: Array<DoubleArray>): Double {
        var result = 0.0
        for(i in 0 until path.size - 1) {
            result += distMatrix[path[i]][path[i+1]]
        }
        return result
    }

    fun fundOptimalRoute(userPos: GridCell, attractions: List<GridCell>): RouteResult {
        val walkableUserPos = gridAStar.findNearestWalkable(userPos.x, userPos.y)
            ?: userPos

        val allPoints = listOf(walkableUserPos) + attractions
        val nodes_ = allPoints.size
        val distMatrix = acoStar.computeMatrix(allPoints)

        val aco = ACO(nodes = nodes_, dist = distMatrix, data = acoData)
        val optimizedIndices = aco.solve()
        val orderedPoints = optimizedIndices.map { allPoints[it] }

        val fullPath = buildFullPath(orderedPoints)

        return RouteResult(
            orderedPoints = orderedPoints,
            fullPath = fullPath,
            totalDistance = calculatePathLength(optimizedIndices, distMatrix)
        )

    }

}