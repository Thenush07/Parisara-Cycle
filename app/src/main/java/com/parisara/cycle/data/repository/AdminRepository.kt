package com.parisara.cycle.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.parisara.cycle.data.model.DangerZone
import com.parisara.cycle.data.model.PitStop
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    suspend fun addPitStop(stop: PitStop): Result<Unit> = runCatching {
        firestore.collection("pitStops").document().set(
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

    suspend fun addDangerZone(title: String, description: String, severity: String, location: GeoPoint): Result<Unit> =
        runCatching {
            firestore.collection("dangerZones").add(
                mapOf(
                    "title" to title,
                    "description" to description,
                    "severity" to severity,
                    "location" to location
                )
            ).await()
        }
}
