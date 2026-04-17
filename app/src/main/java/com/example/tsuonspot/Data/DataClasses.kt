package com.example.alghoritms.Data

import kotlin.math.sqrt
data class Point(val x: Double, val y: Double, var clusterId: Int = -1) {
    fun distanceTo(other: Point) : Double {
        val dx =  this.x - other.x
        val dy = this.y - other.y
        return sqrt(dx*dx + dy*dy)
    }
}

data class Cluster(val id: Int, var centroid: Point? = null,
                       val points: MutableList<Point> = mutableListOf()) {
    fun addPoint(point: Point) {
        points.add(point)
    }

    fun clear() {
        points.clear()
    }
    fun recalculateCentroid() {
        if(points.isEmpty()) return
        val sumX = points.sumOf { it.x }
        val sumY = points.sumOf { it.y }
        centroid = Point(sumX / points.size, sumY/points.size)
    }
}

data class DataRow(val features: List<String>, val label: String)

data class DecisionNode(
    val isLeaf: Boolean,
    val result: String? = null,
    val classDistribution : Map<String, Int> = emptyMap(),
    val splitAttributeIndex: Int? = null,
    val children: Map<String, DecisionNode> = emptyMap(),
)

data class PredictionResult(
    val recommendation: String,
    val chance: Double,
    val path: List<String>
)

data class PredictionOutput(
    val main: PredictionResult,
    val alternatives: List<PredictionResult>
)

data class ACOdata(
    val ants: Int = 30,
    val iterations: Int = 40,
    val pheromone: Double = 1.0,
    val heuristic: Double = 2.0,
    val evaporation: Double = 0.5,
    val pheromoneConst: Double = 1.0,
    val seed: Long = 42L
)

data class RouteResult(
    val orderedPoints: List<GridCell>,
    val fullPath: List<GridCell>,
    val totalDistance: Double
)

data class GridCell(val x: Int, val y: Int, val isWalkable: Boolean)

data class GridEdge(
    val a: GridCell,
    val b: GridCell
)

data object CSVData {
    val data: String = """
        location,budget,time_available,food_type,queue_tolerance,weather,recommended_place
        second_building,low,short,full_meal,medium,good,syr_bor
        second_building,low,medium,full_meal,medium,bad,syr_bor
        second_building,medium,short,bakery,low,bad,second_corps_canteen
        campus_center,low,short,pancakes,medium,good,siberian_pancakes
        campus_center,low,short,pancakes,medium,bad,siberian_pancakes
        main_building,low,short,pancakes,medium,good,siberian_pancakes
        main_building,low,short,pancakes,medium,bad,siberian_pancakes
        second_building,low,short,pancakes,medium,good,siberian_pancakes
        sports_building,low,short,pancakes,medium,good,siberian_pancakes
        campus_center,low,short,full_meal,medium,good,stolovaya_100
        campus_center,low,short,full_meal,medium,bad,stolovaya_100
        main_building,low,medium,full_meal,medium,good,stolovaya_100
        main_building,low,medium,full_meal,medium,bad,stolovaya_100
        second_building,low,short,full_meal,medium,good,stolovaya_100
        sports_building,low,medium,full_meal,medium,good,stolovaya_100
        second_building,low,medium,shaurma,low,good,batina_shaurma
        second_building,low,medium,shaurma,medium,good,batina_shaurma
        campus_center,medium,short,coffee,medium,good,starbooks
        campus_center,medium,short,coffee,medium,bad,starbooks
        main_building,medium,medium,coffee,medium,good,starbooks
        main_building,medium,medium,coffee,medium,bad,starbooks
        second_building,medium,short,coffee,medium,good,starbooks
        sports_building,medium,medium,coffee,medium,good,starbooks
        second_building,low,medium,shaurma,low,good,bezumno_shaurma
        second_building,medium,medium,shaurma,medium,good,bezumno_shaurma
        second_building,medium,medium,grill,low,good,meat_boulevard
        second_building,medium,long,grill,medium,good,meat_boulevard
        second_building,low,medium,grocery,low,good,mariya_ra
        second_building,medium,medium,asian,low,good,chzisyan
        second_building,medium,long,asian,medium,good,chzisyan
        second_building,high,medium,full_meal,medium,good,sib_smoker
        second_building,high,long,full_meal,medium,good,sib_smoker
        campus_center,medium,short,fast_food,medium,good,rostics
        main_building,medium,medium,fast_food,medium,good,rostics
        sports_building,medium,short,fast_food,medium,good,rostics
        campus_center,high,medium,restaurant,medium,good,herbarium
        main_building,high,long,restaurant,medium,good,herbarium
        sports_building,high,medium,restaurant,medium,good,herbarium
        campus_center,high,medium,restaurant,low,good,blizhe
        main_building,high,long,restaurant,low,good,blizhe
        main_building,high,medium,restaurant,low,good,vechniy_zov
        main_building,high,long,restaurant,medium,good,vechniy_zov
        main_building,medium,long,fast_food,low,good,lampochka
        main_building,high,long,fast_food,medium,good,lampochka
        main_building,medium,long,pizza,medium,good,papa_johns
        main_building,high,long,pizza,medium,good,papa_johns
        main_building,high,medium,bakery,low,good,testo_pastry
        main_building,high,long,bakery,low,good,testo_pastry
        main_building,high,long,restaurant,low,good,poly_bistro
        main_building,medium,long,asian,low,good,panda_juice
        main_building,high,long,grill,medium,good,rebro_grill
        main_building,medium,medium,cafe,low,good,cafe_minutka
        main_building,medium,medium,cafe,low,bad,cafe_minutka
        campus_center,medium,medium,cafe,medium,good,cafe_minutka
        campus_center,medium,medium,cafe,medium,bad,cafe_minutka
        second_building,medium,medium,cafe,medium,good,cafe_minutka
        sports_building,medium,medium,cafe,low,good,cafe_minutka
        main_building,low,medium,cafe,low,good,nauchka
        campus_center,medium,medium,cafe,medium,good,nauchka
        second_building,low,medium,cafe,low,good,nauchka
        sports_building,medium,medium,cafe,medium,good,nauchka
        main_building,medium,long,full_meal,medium,good,kolobok
        main_building,medium,short,coffee,low,good,belka_coffee
        campus_center,medium,medium,coffee,medium,good,belka_coffee
        main_building,medium,medium,cafe,medium,good,peshkom_postoyu
        main_building,medium,long,cafe,medium,good,peshkom_postoyu
        main_building,high,medium,bakery,low,good,peki_lola
        main_building,high,medium,bakery,medium,good,peki_lola
        second_building,low,short,grocery,low,good,podkova
        second_building,low,medium,grocery,low,good,podkova
        main_building,low,long,grocery,medium,good,yarche_1
        main_building,low,medium,grocery,medium,good,yarche_2
        campus_center,low,medium,grocery,medium,good,yarche_2
    """.trimIndent()
}