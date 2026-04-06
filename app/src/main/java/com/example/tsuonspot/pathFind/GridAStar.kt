package com.example.alghoritms.pathFind

import com.example.alghoritms.Data.GridCell
import com.example.alghoritms.Data.GridEdge
import kotlin.math.abs

class GridAStar(
    private val grid: Array<Array<Int>>
) : AlgorithmAStar(edges = buildEdges(grid)) {

    companion object {
        private fun buildEdges(grid: Array<Array<Int>>): List<GridEdge> {
            val edges = mutableListOf<GridEdge>()
            val rows = grid.size
            val cols = grid[0].size

            for (x in 0 until rows) {
                for (y in 0 until cols) {
                    if (grid[x][y] == 0) continue

                    val current = GridCell(x, y, true)

                    val neighbors = listOf(
                        Pair(x + 1, y),
                        Pair(x - 1, y),
                        Pair(x, y + 1),
                        Pair(x, y - 1)
                    )

                    for ((nx, ny) in neighbors) {
                        if (nx in 0 until rows && ny in 0 until cols && grid[nx][ny] == 1) {
                            val neighbor = GridCell(nx, ny, true)
                            edges.add(GridEdge(current, neighbor))
                        }
                    }
                }
            }
            return edges
        }
    }

    private fun findNearestWalkable(start: GridCell): GridCell? {
        if (start.x in grid.indices && start.y in grid[0].indices && grid[start.x][start.y] == 1) {
            return start.copy(isWalkable = true)
        }

        val rows = grid.size
        val cols = grid[0].size
        val visited = mutableSetOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()

        queue.add(start.x to start.y)
        visited.add(start.x to start.y)

        val directions = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        var distance = 0

        while (queue.isNotEmpty()) {
            val levelSize = queue.size
            repeat(levelSize) {
                val (x, y) = queue.removeFirst()

                if (grid[x][y] == 1) return GridCell(x, y, true)

                for ((dx, dy) in directions) {
                    val nx = x + dx
                    val ny = y + dy
                    if (nx in 0 until rows && ny in 0 until cols && (nx to ny) !in visited) {
                        visited.add(nx to ny)
                        queue.add(nx to ny)
                    }
                }
            }
            distance++
        }
        return null
    }

    fun findShortestPath(begin: GridCell, end: GridCell): Pair<List<GridCell>, Double>? {
        val actualBegin = findNearestWalkable(begin) ?: return null
        val actualEnd = findNearestWalkable(end) ?: return null

        if (actualBegin == actualEnd) return Pair(listOf(actualBegin), 0.0)

        return try {
            findPath(actualBegin, actualEnd)
        } catch (temp: IllegalArgumentException) {
            null
        }
    }

    override fun costToMoveThrough(edge: GridEdge): Double {
        return 1.0
    }

    override fun createEdge(from: GridCell, to: GridCell): GridEdge {
        return GridEdge(from, to)
    }

    override fun heuristic(from: GridCell, to: GridCell): Double {
        return (abs(from.x - to.x) + abs(from.y - to.y)).toDouble()
    }
}