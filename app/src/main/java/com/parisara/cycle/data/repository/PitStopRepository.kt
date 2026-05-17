package com.parisara.cycle.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.parisara.cycle.data.model.PitStop
import com.parisara.cycle.util.LocationUtils.distanceKm
import com.parisara.cycle.util.toLatLng
import com.parisara.cycle.util.toPitStop
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.android.gms.maps.model.LatLng

class PitStopRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val pitStops = firestore.collection("pitStops")

    fun observePitStops(): Flow<List<PitStop>> = callbackFlow {
        val reg = pitStops.addSnapshotListener { snap, _ ->
            val list = snap?.documents?.map { it.toPitStop() }
            if (list.isNullOrEmpty()) {
                trySend(SampleData.pitStops)
            } else {
                trySend(list)
            }
        }
        awaitClose { reg.remove() }
    }

    suspend fun seedSampleData(): Result<Unit> = runCatching {
        SampleData.pitStops.forEach { stop ->
            pitStops.document(stop.id).set(
                mapOf(
                    "name" to stop.name,
                    "category" to stop.category,
                    "address" to stop.address,
                    "location" to stop.location,
                    "operatingHours" to stop.operatingHours,
                    "phone" to stop.phone,
                    "rating" to stop.rating
                )
            ).await()
        }
    }

    fun filterByCategory(stops: List<PitStop>, category: String?): List<PitStop> =
        if (category.isNullOrBlank() || category == "All") stops
        else stops.filter { it.category.equals(category, ignoreCase = true) }

    fun sortByDistance(stops: List<PitStop>, userLocation: LatLng): List<PitStop> =
        stops.sortedBy { stop ->
            stop.location?.toLatLng()?.let { distanceKm(userLocation, it) } ?: Double.MAX_VALUE
        }
}

object SampleData {
    // Bengaluru sample coordinates for demo
    val center = LatLng(12.9716, 77.5946)

    val pitStops = listOf(
        PitStop("ps1", "Green Wheel Repairs", "Repair Shop", "MG Road", GeoPoint(12.9750, 77.6063), "8 AM - 8 PM", "+91 9876500001", 4.5f),
        PitStop("ps2", "Cycle Hydration Point", "Water Station", "Cubbon Park Gate", GeoPoint(12.9763, 77.5929), "6 AM - 9 PM", "", 4.2f),
        PitStop("ps3", "Eco Rest Pavilion", "Rest Area", "Indiranagar", GeoPoint(12.9784, 77.6408), "24 Hours", "", 4.0f),
        PitStop("ps4", "SafeRide First Aid", "First Aid", "Koramangala", GeoPoint(12.9352, 77.6245), "7 AM - 10 PM", "+91 9876500002", 4.7f),
        PitStop("ps5", "Public Bike Stand", "Utility", "Whitefield", GeoPoint(12.9698, 77.7499), "24 Hours", "", 3.9f)
    )

    val dangerZones = listOf(
        com.parisara.cycle.data.model.DangerZone("dz1", "Pothole Cluster", "Multiple potholes near junction", "high", GeoPoint(12.9680, 77.5900)),
        com.parisara.cycle.data.model.DangerZone("dz2", "Poor Lighting", "Dim street lights after 7 PM", "medium", GeoPoint(12.9800, 77.6100))
    )

    val safetyTips = listOf(
        com.parisara.cycle.data.model.SafetyTip("t1", "Always Wear a Helmet", "A certified helmet reduces head injury risk by up to 70%. Ensure proper fit and strap fastening.", "Helmet", "🪖"),
        com.parisara.cycle.data.model.SafetyTip("t2", "Follow Traffic Rules", "Obey signals, use hand signals for turns, and ride in the designated lane.", "Road Rules", "🚦"),
        com.parisara.cycle.data.model.SafetyTip("t3", "Use Reflective Gear", "Wear reflective vests and use front/rear lights when cycling at dawn, dusk, or night.", "Visibility", "🔦"),
        com.parisara.cycle.data.model.SafetyTip("t4", "Maintain Safe Speed", "Slow down near intersections, schools, and pedestrian crossings.", "Riding", "⚡"),
        com.parisara.cycle.data.model.SafetyTip("t5", "Check Your Bicycle", "Inspect brakes, tires, and chain before every ride.", "Maintenance", "🔧")
    )
}
