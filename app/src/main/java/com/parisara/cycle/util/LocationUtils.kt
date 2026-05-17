package com.parisara.cycle.util

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)

fun LatLng.toGeoPoint(): GeoPoint = GeoPoint(latitude, longitude)

object LocationUtils {
    fun distanceKm(from: LatLng, to: LatLng): Double {
        val r = 6371.0
        val dLat = Math.toRadians(to.latitude - from.latitude)
        val dLng = Math.toRadians(to.longitude - from.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
            cos(Math.toRadians(from.latitude)) * cos(Math.toRadians(to.latitude)) *
            sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun estimateCyclingMinutes(distanceKm: Double): Int =
        ((distanceKm / 15.0) * 60).toInt().coerceAtLeast(1)

    fun safetyScoreFromDistance(distanceKm: Double, dangerCount: Int): Int {
        var score = 95
        if (distanceKm > 10) score -= 5
        score -= dangerCount * 8
        return score.coerceIn(40, 100)
    }
}
