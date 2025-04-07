package com.example.attendancecollector

import kotlin.math.*

fun calculateDistance(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val earthRadius = 6371.0 // in kilometers

    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)

    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)

    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadius * c *1000 // returns distance in km
}
  var loca:String="location"

var dis :Double = 0.0;