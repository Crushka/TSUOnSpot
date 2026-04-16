package com.example.tsuonspot

import android.content.Context
import androidx.core.content.edit

object RatingStore {
    private const val PREFS_NAME = "tsu_ratings"
    private const val NOT_RATED  = -1

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getPoiRating(context: Context, poiId: Int): Int =
        prefs(context).getInt("poi_$poiId", NOT_RATED)

    fun setPoiRating(context: Context, poiId: Int, rating: Int) {
        prefs(context).edit { putInt("poi_$poiId", rating) }
    }

    fun getAttractionRating(context: Context, attractionId: Int): Int =
        prefs(context).getInt("attraction_$attractionId", NOT_RATED)

    fun setAttractionRating(context: Context, attractionId: Int, rating: Int) {
        prefs(context).edit { putInt("attraction_$attractionId", rating) }
    }
}