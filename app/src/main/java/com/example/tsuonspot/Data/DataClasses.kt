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
    val splitAttributeIndex: Int? = null,
    val threshold: String? = null,
    val children: Map<String, DecisionNode> = emptyMap()
)


data class GridCell(val x: Int, val y: Int, val isWalkable: Boolean)

data object CSVData {
    val data: String = """
        location,budget,time_available,food_type,queue_tolerance,weather,recommended_place
        campus_center,low,short,pancakes,medium,good,siberian_pancakes
        campus_center,low,short,pancakes,low,bad,siberian_pancakes
        campus_center,low,medium,soup,medium,good,stolovaya_100
        campus_center,low,short,soup,low,bad,stolovaya_100
        campus_center,medium,short,full_meal,medium,good,cafe_minutka
        campus_center,medium,medium,full_meal,low,good,cafe_minutka
        campus_center,medium,medium,coffee,medium,good,starbooks
        campus_center,medium,short,coffee,low,good,starbooks
        campus_center,low,short,fast_food,medium,good,bezumno_shaurma
        campus_center,low,short,fast_food,low,bad,bezumno_shaurma
        campus_center,low,short,shaurma,low,bad,batina_shaurma
        campus_center,low,short,shaurma,medium,good,batina_shaurma
        campus_center,low,medium,full_meal,medium,good,nauchka
        campus_center,low,short,snacks,low,good,kolobok
        campus_center,low,short,snacks,medium,bad,kolobok
        campus_center,medium,medium,gastromarket,medium,good,lampochka
        campus_center,medium,short,gastromarket,low,good,lampochka
        campus_center,low,short,coffee,low,good,belka_coffee
        campus_center,low,medium,coffee,medium,bad,belka_coffee
        campus_center,medium,short,fast_food,medium,good,rostiks
        campus_center,medium,medium,full_meal,low,good,rostiks
        campus_center,low,short,bakery,low,good,xo_bakery
        campus_center,low,short,bakery,medium,bad,xo_bakery
        campus_center,low,medium,soup,medium,good,second_corps_canteen
        campus_center,low,short,soup,low,bad,second_corps_canteen
        campus_center,low,short,street_food,low,good,peshkom_postoyu
        campus_center,medium,short,asian,medium,good,panda_juice
        campus_center,medium,medium,asian,low,good,panda_juice
        campus_center,low,short,bakery,medium,good,testo_pastry
        campus_center,medium,medium,bakery,low,good,testo_pastry
        campus_center,medium,medium,bakery,high,good,peki_lola
        campus_center,medium,short,cafe,medium,good,peki_lola
        campus_center,medium,medium,restaurant,low,good,vechniy_zov
        campus_center,medium,medium,restaurant,medium,bad,vechniy_zov
        campus_center,medium,short,restaurant,low,good,rebro_grill
        campus_center,high,medium,restaurant,low,good,poly_bistro
        campus_center,medium,short,bistro,low,good,poly_bistro
        campus_center,medium,medium,cafe,medium,good,herbarium
        campus_center,medium,short,cafe,low,good,herbarium
        campus_center,medium,medium,restaurant,medium,good,blizhe
        campus_center,low,short,grocery,low,good,podkova
        campus_center,low,medium,grocery,medium,bad,podkova
        campus_center,low,short,grocery,low,good,yarche_2
        campus_center,low,medium,grocery,low,bad,mariya_ra
        campus_center,low,short,grocery,low,good,abrikos
        campus_center,low,medium,grocery,medium,good,abrikos
        main_building,low,short,pancakes,medium,good,siberian_pancakes
        main_building,low,short,fast_food,low,good,bezumno_shaurma
        main_building,low,medium,soup,medium,good,second_corps_canteen
        main_building,low,short,coffee,low,good,belka_coffee
        main_building,low,short,grocery,low,good,podkova
    """.trimIndent()
}