//package com.example.tsuonspot.AntAlghoritm
//
//import com.example.alghoritms.Data.GridCell
//import com.example.alghoritms.pathFind.GridAStar
//
//class StarForACO(private val gridAStar: GridAStar) {
//
//    private fun calculateAStarDistance(from: GridCell, to: GridCell): Double {
//        val start = if (from.isWalkable) from else gridAStar.findNearestWalkable(from.x, from.y) ?: from
//        val end = if (to.isWalkable) to else gridAStar.findNearestWalkable(to.x, to.y) ?: to
//
//        val (_, cost) = gridAStar.findPath(start, end)
//        return cost
//    }
//}