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

    protected open fun getNeighbors(cell: GridCell): List<GridCell> {
        return edges
            .asSequence()
            .filter { it.a == cell || it.b == cell }
            .map { listOf(it.a, it.b) }
            .flatten()
            .filterNot { it == cell }
            .distinct()
            .toList()
    }

    private val GridEdge.edgeCost: Double
        get() = costToMoveThrough(this)

    private fun findRoute(from: GridCell, to: GridCell): GridEdge? {
        return edges.find {
            (it.a == from && it.b == to) || (it.a == to && it.b == from)
        }
    }

    private fun routeCheck(from: GridCell, to: GridCell): GridEdge {
        return findRoute(from, to) ?: createEdge(from, to)
    }

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
        return path.toList()
    }

    abstract fun costToMoveThrough(edge: GridEdge): Double
    abstract fun createEdge(from: GridCell, to: GridCell): GridEdge

    fun findPath(begin: GridCell, end: GridCell): Pair<List<GridCell>, Double> {
        val cameFrom = mutableMapOf<GridCell, GridCell>()
        val openVertices = mutableSetOf(begin)
        val closedVertices = mutableSetOf<GridCell>()
        val costFromStart = mutableMapOf(begin to 0.0)

        val initialEdge = routeCheck(begin, end)
        val totalCost = mutableMapOf(begin to initialEdge.edgeCost)

        while (openVertices.isNotEmpty()) {
            val currentPos = openVertices.minByOrNull { totalCost.getOrDefault(it, Double.MAX_VALUE) }!!

            if (currentPos == end) {
                val path = generatePath(currentPos, cameFrom)
                return Pair(path, costFromStart.getValue(end))
            }

            openVertices.remove(currentPos)
            closedVertices.add(currentPos)

            (getNeighbors(currentPos) - closedVertices).forEach { neighbour ->
                val edge = routeCheck(currentPos, neighbour)
                val routeCost = costToMoveThrough(edge)
                val newScore = costFromStart.getValue(currentPos) + routeCost

                if (newScore < costFromStart.getOrDefault(neighbour, Double.MAX_VALUE)) {
                    cameFrom[neighbour] = currentPos
                    costFromStart[neighbour] = newScore
                    totalCost[neighbour] = newScore + heuristic(neighbour, end)

                    if (!openVertices.contains(neighbour)) {
                        openVertices.add(neighbour)
                    }
                }
            }
        }
        throw IllegalArgumentException("Путь не найден")
    }
}