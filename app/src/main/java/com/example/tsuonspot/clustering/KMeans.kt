package com.example.alghoritms.clustering

import com.example.alghoritms.Data.*
import kotlin.math.acos
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class KMeans(private val clustersQuantity:Int,
             private val distanceMetric: DistanceMetric = EuclidianDistance(),
             private val maxIterations: Int = 100,
             private val threshold: Double = 0.0001,
             private val normalize: Boolean = true)
{

    fun findClusters(points: List<Point>): List<Cluster> {
        val (processedPoints, originalPoints) = if(normalize) {
            val normalized = normalizePoints(points)
            val mapping = normalized.zip(points).toMap()
            Pair(normalized, mapping)
        }
        else {
            Pair(points, points.associateWith { it })
        }

        if(processedPoints.size < clustersQuantity) {
            throw IllegalArgumentException("Точек меньше, чем кластеров")
        }

        val centroids = KMeansPlusPlus(processedPoints)

        val clusters = mutableListOf<Cluster>()
        for(i in 0 until clustersQuantity) {
            clusters.add(Cluster(id = i, centroid = centroids[i]))
        }

        var previousCentriods: List<Point>
        var iteration = 0

        do {
            previousCentriods = clusters.map { it.centroid ?: Point(0.0, 0.0) }

            clusters.forEach { it.clear() }

            processedPoints.forEach { point ->
                val originalPoint = originalPoints[point] ?: point
                val closestCluster = clusters.minByOrNull { cluster ->
                    distanceMetric.calculate(point,cluster.centroid?: Point(0.0, 0.0))
                }?: clusters[0]
                closestCluster.addPoint(originalPoint)
            }
            clusters.forEach { it.recalculateCentroid() }

            val currentCentroids = clusters.map {it.centroid?: Point(0.0, 0.0) }
            val movement = previousCentriods.zip(currentCentroids) { previous, current ->
                previous.distanceTo(current)
            }.sum()

            iteration++

            if(movement < threshold) {
                break
            }
        } while (iteration < maxIterations)
        return clusters.filter { it.points.isNotEmpty() }
    }

    private fun KMeansPlusPlus(points: List<Point>): List<Point> {
        val centroids = mutableListOf<Point>()

        centroids.add(points[Random.nextInt(points.size)])

        while (centroids.size < clustersQuantity) {
            val distances = points.map {point ->
                val minDistance = centroids.minOf { centroid ->
                    distanceMetric.calculate(point, centroid)
                }
                minDistance * minDistance
            }
            val totalDistance = distances.sum()

            val randomValue = Random.nextDouble() * totalDistance
            var cumulative = 0.0
            var selectedIndex = 0

            for(i in distances.indices) {
                cumulative += distances[i]
                if(cumulative >= randomValue) {
                    selectedIndex = i
                    break
                }
            }
            centroids.add(points[selectedIndex])
        }
        return centroids
    }
    fun calculatePoints(points: List<Point>, maxClusters: Int = 10):List<Double> {
        val distortions = mutableListOf<Double>()
        val maxValidClusters = min(maxClusters, points.size)

        for(clusterValue in 1..maxValidClusters) {
            val kMeans = KMeans(clusterValue, distanceMetric = distanceMetric, normalize=normalize)
            val clusters = kMeans.findClusters(points)
            val distortion = clusters.sumOf{cluster ->
                cluster.points.sumOf{ point ->
                    point.distanceTo(cluster.centroid?: point)
                }
            }
            distortions.add(distortion)
        }
        return distortions
    }

    fun findOptimalClusters(points: List<Point>, maxClusters: Int): Int {
        val maxValidClusters = min(maxClusters, points.size - 1)
        if(maxValidClusters  < 2) {
            return 1
        }
        val distortions = calculatePoints(points, maxValidClusters)
        return findElbowPoint(distortions)
    }

    private fun findElbowPoint(distortions: List<Double>): Int {
        if (distortions.size < 3) return 2

        var elbowIndex = 1
        var maxAngle = Double.NEGATIVE_INFINITY

        for (i in 1 until distortions.size - 1) {
            val prev = distortions[i - 1]
            val curr = distortions[i]
            val next = distortions[i + 1]

            val v1 = 1.0 to (curr - prev)
            val v2 = 1.0 to (next - curr)

            val dot = v1.first * v2.first + v1.second * v2.second
            val mag1 = sqrt(v1.first * v1.first + v1.second * v1.second)
            val mag2 = sqrt(v2.first * v2.first + v2.second * v2.second)

            val cos = (dot / (mag1 * mag2)).coerceIn(-1.0, 1.0)
            val angle = acos(cos)

            if (angle > maxAngle) {
                maxAngle = angle
                elbowIndex = i
            }
        }

        return elbowIndex + 1
    }

    private fun normalizePoints(points: List<Point>): List<Point> {
        val minX = points.minOf { it.x }
        val minY = points.minOf { it.y }
        val maxX = points.maxOf { it.x }
        val maxY = points.maxOf { it.y }

        val rangeX = maxX - minX
        val rangeY = maxY - minY

        return points.map{ point ->
            val normalizedX = if(rangeX > 0) (point.x - minX) / rangeX else 0.5
            val normalizedY = if(rangeY > 0) (point.y - minY) / rangeY else 0.5
            Point(normalizedX, normalizedY, clusterId = point.clusterId)
        }
    }
}