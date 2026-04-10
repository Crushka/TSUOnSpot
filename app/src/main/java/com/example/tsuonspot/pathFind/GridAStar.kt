package com.example.alghoritms.pathFind

import android.content.Context
import com.example.alghoritms.Data.GridCell
import kotlin.math.abs

class GridAStar(
    private val grid: Array<Array<Int>>
) : AlgorithmAStar(emptyList()) {

    private val rows = grid.size
    private val cols = if (rows > 0) grid[0].size else 0

    override fun getNeighbors(cell: GridCell): List<GridCell> {
        val neighbors = mutableListOf<GridCell>()
        val directions = listOf(
            Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
        )

        for ((dx, dy) in directions) {
            val nx = cell.x + dx
            val ny = cell.y + dy

            if (ny in 0 until rows && nx in 0 until grid[ny].size && grid[ny][nx] == 1) {
                neighbors.add(GridCell(nx, ny, true))
            }
        }
        return neighbors
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

    fun findNearestWalkable(x: Int, y: Int): GridCell? {
        if (y in 0 until rows && x in 0 until grid[y].size && grid[y][x] == 1) {
            return GridCell(x, y, true)
        }

        for (r in 1..10) {
            for (dx in -r..r) {
                for (dy in -r..r) {
                    val nx = x + dx
                    val ny = y + dy
                    if (ny in 0 until rows && nx in 0 until grid[ny].size && grid[ny][nx] == 1) {
                        return GridCell(nx, ny, true)
                    }
                }
            }
        }
        return null
    }

    companion object {
        fun loadFromAssets(context: Context, fileName: String): GridAStar {
            val matrix = mutableListOf<Array<Int>>()
            context.assets.open(fileName).bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val row = line.trim().split(" ")
                        .filter { it.isNotEmpty() }
                        .map { it.toInt() }
                        .toTypedArray()
                    if (row.isNotEmpty()) matrix.add(row)
                }
            }
            return GridAStar(matrix.toTypedArray())
        }
    }
}