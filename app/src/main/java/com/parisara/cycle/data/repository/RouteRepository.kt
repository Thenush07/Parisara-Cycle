package com.parisara.cycle.data.repository

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.parisara.cycle.data.model.DangerZone
import com.parisara.cycle.data.model.RouteInfo
import com.parisara.cycle.data.remote.DirectionsApi
import com.parisara.cycle.data.repository.SampleData.dangerZones
import com.parisara.cycle.util.LocationUtils
import com.parisara.cycle.util.PolylineDecoder
import com.parisara.cycle.util.toDangerZone
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class RouteRepository(
    private val directionsApi: DirectionsApi,
    private val apiKey: String,
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun getCyclingRoute(origin: LatLng, destination: LatLng): Result<RouteInfo> =
        runCatching {
            if (apiKey.isBlank()) {
                return@runCatching buildFallbackRoute(origin, destination)
            }
            val originStr = "${origin.latitude},${origin.longitude}"
            val destStr = "${destination.latitude},${destination.longitude}"
            val response = directionsApi.getDirections(
                origin = originStr,
                destination = destStr,
                apiKey = apiKey
            )
            val route = response.routes.firstOrNull()
                ?: return@runCatching buildFallbackRoute(origin, destination)
            val leg = route.legs.firstOrNull()
            val distanceKm = (leg?.distance?.value ?: 0L) / 1000.0
            val durationMin = ((leg?.duration?.value ?: 0L) / 60).toInt().coerceAtLeast(1)
            val encoded = route.overviewPolyline?.points ?: ""
            val points = if (encoded.isNotBlank()) PolylineDecoder.decode(encoded)
            else listOf(origin, destination)
            val dangers = getNearbyDangerCount(origin, destination)
            RouteInfo(
                distanceKm = distanceKm.coerceAtLeast(0.1),
                durationMinutes = durationMin,
                safetyScore = LocationUtils.safetyScoreFromDistance(distanceKm, dangers),
                polylinePoints = points,
                encodedPolyline = encoded
            )
        }.recoverCatching {
            buildFallbackRoute(origin, destination)
        }

    private fun buildFallbackRoute(origin: LatLng, destination: LatLng): RouteInfo {
        val distanceKm = LocationUtils.distanceKm(origin, destination)
        val duration = LocationUtils.estimateCyclingMinutes(distanceKm)
        val mid = LatLng(
            (origin.latitude + destination.latitude) / 2,
            (origin.longitude + destination.longitude) / 2
        )
        return RouteInfo(
            distanceKm = distanceKm,
            durationMinutes = duration,
            safetyScore = LocationUtils.safetyScoreFromDistance(distanceKm, 1),
            polylinePoints = listOf(origin, mid, destination),
            encodedPolyline = ""
        )
    }

    private fun getNearbyDangerCount(origin: LatLng, dest: LatLng): Int {
        val zones = dangerZones
        return zones.count { zone ->
            zone.location?.let { gp ->
                val p = LatLng(gp.latitude, gp.longitude)
                LocationUtils.distanceKm(origin, p) < 2 || LocationUtils.distanceKm(dest, p) < 2
            } ?: false
        }
    }

    fun observeDangerZones(): Flow<List<DangerZone>> = callbackFlow {
        val reg = firestore.collection("dangerZones").addSnapshotListener { snap, _ ->
            val list = snap?.documents?.map { it.toDangerZone() }
            trySend(if (list.isNullOrEmpty()) dangerZones else list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun addDangerZone(zone: DangerZone): Result<Unit> = runCatching {
        firestore.collection("dangerZones").document(zone.id.ifBlank { null } ?: firestore.collection("dangerZones").document().id)
            .set(
                mapOf(
                    "title" to zone.title,
                    "description" to zone.description,
                    "severity" to zone.severity,
                    "location" to zone.location
                )
            ).await()
    }
}
