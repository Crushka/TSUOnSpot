package com.example.alghoritms.pathFind

import com.example.alghoritms.Data.GridCell
import kotlin.math.sqrt

abstract class AlgorithmAStar(
    protected val edges: List<GridEdge>
) {
    data class GridEdge(
        val a: GridCell,
        val b: GridCell
    )

    open fun getNeighbors(cell: GridCell): List<GridCell> {
        return edges
            .asSequence()
            .filter { it.a == cell || it.b == cell }
            .map { if (it.a == cell) it.b else it.a }
            .distinct()
            .toList()
    }

    abstract fun costToMoveThrough(from: GridCell, to: GridCell): Double

    abstract fun createEdge(from: GridCell, to: GridCell): GridEdge

    protected open fun heuristic(from: GridCell, to: GridCell): Double {
        val dx = (from.x - to.x).toDouble()
        val dy = (from.y - to.y).toDouble()
        return sqrt(dx * dx + dy * dy)
    }

    private fun generatePath(currentPos: GridCell, cameFrom: Map<GridCell, GridCell>): List<GridCell> {
        val path = mutableListOf(currentPos)
        var current = currentPos
        while (cameFrom.containsKey(current)) {
            current = cameFrom.getValue(current)
            path.add(0, current)
        }
        return path
    }

    fun findPath(begin: GridCell, end: GridCell): Pair<List<GridCell>, Double> {
        val cameFrom = mutableMapOf<GridCell, GridCell>()
        val openVertices = mutableSetOf(begin)
        val closedVertices = mutableSetOf<GridCell>()
        val costFromStart = mutableMapOf(begin to 0.0)
        val totalCost = mutableMapOf(begin to heuristic(begin, end))

        while (openVertices.isNotEmpty()) {
            val currentPos = openVertices.minBy { totalCost.getOrDefault(it, Double.MAX_VALUE) }!!

            if (currentPos.x == end.x && currentPos.y == end.y) {
                val path = generatePath(currentPos, cameFrom)
                return Pair(path, costFromStart[currentPos] ?: 0.0)
            }

            openVertices.remove(currentPos)
            closedVertices.add(currentPos)

            for (neighbour in getNeighbors(currentPos)) {
                if (neighbour in closedVertices) continue

                val moveCost = costToMoveThrough(currentPos, neighbour)
                val tentativeCost = (costFromStart[currentPos] ?: 0.0) + moveCost

                if (tentativeCost < (costFromStart[neighbour] ?: Double.MAX_VALUE)) {
                    cameFrom[neighbour] = currentPos
                    costFromStart[neighbour] = tentativeCost
                    totalCost[neighbour] = tentativeCost + heuristic(neighbour, end)

                    if (neighbour !in openVertices) {
                        openVertices.add(neighbour)
                    }
                }
            }
        }
        throw IllegalArgumentException("Нет пути от $begin до $end")
    }
}