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

    private val GridCell.neighbors: List<GridCell>
        get() = edges
            .asSequence()
            .filter { it.a == this || it.b == this }
            .map { listOf(it.a, it.b) }
            .flatten()
            .filterNot { it == this }
            .distinct()
            .toList()

    private val GridEdge.cost: Double
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

        val estimatedRoute = routeCheck(from = begin, to = end)
        val totalCost = mutableMapOf(begin to estimatedRoute.cost)

        while (openVertices.isNotEmpty()) {
            val currentPos = openVertices.minBy { totalCost.getValue(it) }!!

            if (currentPos == end) {
                val path = generatePath(currentPos, cameFrom)
                return Pair(path, totalCost.getValue(end))
            }

            openVertices.remove(currentPos)
            closedVertices.add(currentPos)

            (currentPos.neighbors - closedVertices).forEach { neighbour ->
                val routeCost = routeCheck(from = currentPos, to = neighbour).cost
                val cost: Double = costFromStart.getValue(currentPos) + routeCost

                if (cost < costFromStart.getOrDefault(neighbour, Double.MAX_VALUE)) {
                    if (!openVertices.contains(neighbour)) {
                        openVertices.add(neighbour)
                    }

                    cameFrom[neighbour] = currentPos
                    costFromStart[neighbour] = cost

                    val remainingRouteCost = routeCheck(from = neighbour, to = end).cost
                    totalCost[neighbour] = cost + remainingRouteCost
                }
            }
        }

        throw IllegalArgumentException("нет пути от начала $begin до конца $end")
    }
}