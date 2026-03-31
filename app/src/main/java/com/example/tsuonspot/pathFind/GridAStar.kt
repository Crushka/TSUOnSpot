package com.example.alghoritms.pathFind

import com.example.alghoritms.Data.GridCell
import kotlin.math.abs

class GridAStar(
    private val grid: Array<Array<Int>> // 1 - проходимо, 0 - непроходимо
) : AlgorithmAStar(edges = buildEdges(grid)) {

    companion object {
        private fun buildEdges(grid: Array<Array<Int>>): List<GridEdge> {
            val edges = mutableListOf<GridEdge>()
            val rows = grid.size
            val cols = grid[0].size

            for (x in 0 until rows) {
                for (y in 0 until cols) {
                    if (grid[x][y] == 0) continue // непроходимые ячейки пропускаем

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