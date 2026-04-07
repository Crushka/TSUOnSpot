package com.example.tsuonspot.AntAlghoritm

import com.example.alghoritms.Data.ACOdata
import kotlin.math.pow
import kotlin.random.Random

class ACO(private val nodes: Int, private val dist: Array<DoubleArray>, private val data: ACOdata = ACOdata()) {
    private val pheromones = Array(nodes) { DoubleArray(nodes) { 1.0 } }
    private val heuristic = Array(nodes) {i ->
        DoubleArray(nodes) {j -> if(dist[i][j] > 0) 1.0 / dist[i][j] else 0.0}
    }
    private var bestPath: List<Int>? = null
    private var bestCost = Double.MAX_VALUE
    private val random = Random(data.seed)

    private fun constructPath() : List<Int> {
        val visited = BooleanArray(nodes)
        val path = mutableListOf<Int>()
        var current = 0
        visited[current] = true
        path.add(current)

        while(path.size < nodes) {
            val chances = DoubleArray(nodes)
            var total = 0.0

            for(next in 0 until nodes) {
                if (!visited[next]) {
                    chances[next] = pheromones[current][next].pow(data.pheromone) *
                            heuristic[current][next].pow(data.heuristic)
                    total += chances[next]
                }
            }
            val rand = random.nextDouble() * total
            var cumSum = 0.0
            for(next in 0 until nodes) {
                if(visited[next]) {
                    cumSum += chances[next]
                    if(cumSum >= rand) {
                        visited[next] = true
                        path.add(next)
                        current = next
                        break
                    }
                }
            }
        }
        return path
    }

    private fun calculatePath(path: List<Int>):Double {
        var cost = 0.0
        for(i in 0 until path.size) {
            cost += dist[path[i]][path[i+1]]
        }

        return cost
    }

    private fun updatePheromones(paths: List<List<Int>>, costs: List<Double>) {
        for(i in 0 until nodes) {
            for(j in 0 until nodes) {
                pheromones[i][j] *= (1 - data.evaporation)
            }

            for(k in paths.indices) {
                val path = paths[k]
                val cost = costs[k]
                for(i in 0 until path.lastIndex) {
                    val from = path[i]
                    val to = path[i + 1]
                    pheromones[from][to] += data.pheromoneConst / cost
                    pheromones[to][from] += data.pheromoneConst / cost
                }
            }
        }
    }

    fun solve() : List<Int> {
        repeat(data.iterations) {
            val paths = MutableList(data.ants) {
                constructPath()
            }
            val costs = paths.map { calculatePath(it) }
            val minIndex = costs.indexOf(costs.min())
            if(costs[minIndex] < bestCost) {
                bestCost = costs[minIndex]
                bestPath = paths[minIndex]
            }

            updatePheromones(paths, costs)
        }
        return bestPath ?: (0 until nodes).toList()
    }
}