package com.parisara.cycle.data.model

import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val city: String = "",
    val profileImageUrl: String = "",
    val totalDistanceKm: Double = 0.0,
    val totalCo2SavedGrams: Double = 0.0,
    val totalRides: Int = 0,
    val isAdmin: Boolean = false,
    val achievements: List<String> = emptyList(),
    val createdAt: Timestamp? = null
)

data class RideRecord(
    val id: String = "",
    val userId: String = "",
    val sourceName: String = "",
    val destinationName: String = "",
    val sourceLat: Double = 0.0,
    val sourceLng: Double = 0.0,
    val destLat: Double = 0.0,
    val destLng: Double = 0.0,
    val distanceKm: Double = 0.0,
    val durationMinutes: Int = 0,
    val co2SavedGrams: Double = 0.0,
    val safetyScore: Int = 85,
    val polylineEncoded: String = "",
    val completedAt: Timestamp? = null
)

data class RouteReport(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val issueType: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val location: GeoPoint? = null,
    val status: String = ReportStatus.PENDING,
    val createdAt: Timestamp? = null
)

object ReportStatus {
    const val PENDING = "Pending"
    const val VERIFIED = "Verified"
    const val RESOLVED = "Resolved"
}

data class PitStop(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val address: String = "",
    val location: GeoPoint? = null,
    val operatingHours: String = "9 AM - 6 PM",
    val phone: String = "",
    val rating: Float = 4.0f
)

data class DangerZone(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val severity: String = "medium",
    val location: GeoPoint? = null
)

data class BuddyLocation(
    val userId: String = "",
    val userName: String = "",
    val profileImageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val isSharing: Boolean = false,
    val updatedAt: Long = 0L
)

data class BuddyRequest(
    val id: String = "",
    val fromUserId: String = "",
    val fromUserName: String = "",
    val toUserId: String = "",
    val status: String = "pending",
    val createdAt: Timestamp? = null
)

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val read: Boolean = false,
    val createdAt: Timestamp? = null
)

data class EcoStats(
    val dailyDistanceKm: Double = 0.0,
    val weeklyDistanceKm: Double = 0.0,
    val monthlyDistanceKm: Double = 0.0,
    val dailyCo2Grams: Double = 0.0,
    val weeklyCo2Grams: Double = 0.0,
    val monthlyCo2Grams: Double = 0.0
)

data class Achievement(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val requiredDistanceKm: Double
)

data class SafetyTip(
    val id: String,
    val title: String,
    val content: String,
    val category: String,
    val icon: String
)

data class RouteInfo(
    val distanceKm: Double,
    val durationMinutes: Int,
    val safetyScore: Int,
    val polylinePoints: List<LatLng>,
    val encodedPolyline: String
)

object EcoCalculator {
    const val CO2_GRAMS_PER_KM = 120.0

    fun co2FromDistanceKm(km: Double): Double = km * CO2_GRAMS_PER_KM

    fun formatCo2(grams: Double): String = when {
        grams >= 1000 -> String.format("%.1f kg", grams / 1000)
        else -> String.format("%.0f g", grams)
    }
}

val ACHIEVEMENTS = listOf(
    Achievement("green_beginner", "Green Beginner", "Complete your first 5 km ride", "🌱", 5.0),
    Achievement("eco_warrior", "Eco Warrior", "Save 1 kg of CO2", "🌍", 8.33),
    Achievement("cycle_champion", "Cycle Champion", "Ride 100 km total", "🏆", 100.0)
)
