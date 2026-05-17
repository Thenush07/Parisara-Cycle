package com.parisara.cycle.util

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.parisara.cycle.data.model.*

fun DocumentSnapshot.toUserProfile(): UserProfile = UserProfile(
    uid = id,
    name = getString("name") ?: "",
    email = getString("email") ?: "",
    phone = getString("phone") ?: "",
    city = getString("city") ?: "",
    profileImageUrl = getString("profileImageUrl") ?: "",
    totalDistanceKm = getDouble("totalDistanceKm") ?: 0.0,
    totalCo2SavedGrams = getDouble("totalCo2SavedGrams") ?: 0.0,
    totalRides = (getLong("totalRides") ?: 0).toInt(),
    isAdmin = getBoolean("isAdmin") ?: false,
    achievements = (get("achievements") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
    createdAt = getTimestamp("createdAt")
)

fun UserProfile.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "email" to email,
    "phone" to phone,
    "city" to city,
    "profileImageUrl" to profileImageUrl,
    "totalDistanceKm" to totalDistanceKm,
    "totalCo2SavedGrams" to totalCo2SavedGrams,
    "totalRides" to totalRides,
    "isAdmin" to isAdmin,
    "achievements" to achievements,
    "createdAt" to (createdAt ?: Timestamp.now())
)

fun DocumentSnapshot.toRideRecord(): RideRecord = RideRecord(
    id = id,
    userId = getString("userId") ?: "",
    sourceName = getString("sourceName") ?: "",
    destinationName = getString("destinationName") ?: "",
    sourceLat = getDouble("sourceLat") ?: 0.0,
    sourceLng = getDouble("sourceLng") ?: 0.0,
    destLat = getDouble("destLat") ?: 0.0,
    destLng = getDouble("destLng") ?: 0.0,
    distanceKm = getDouble("distanceKm") ?: 0.0,
    durationMinutes = (getLong("durationMinutes") ?: 0).toInt(),
    co2SavedGrams = getDouble("co2SavedGrams") ?: 0.0,
    safetyScore = (getLong("safetyScore") ?: 85).toInt(),
    polylineEncoded = getString("polylineEncoded") ?: "",
    completedAt = getTimestamp("completedAt")
)

fun RideRecord.toMap(): Map<String, Any?> = mapOf(
    "userId" to userId,
    "sourceName" to sourceName,
    "destinationName" to destinationName,
    "sourceLat" to sourceLat,
    "sourceLng" to sourceLng,
    "destLat" to destLat,
    "destLng" to destLng,
    "distanceKm" to distanceKm,
    "durationMinutes" to durationMinutes,
    "co2SavedGrams" to co2SavedGrams,
    "safetyScore" to safetyScore,
    "polylineEncoded" to polylineEncoded,
    "completedAt" to (completedAt ?: Timestamp.now())
)

fun DocumentSnapshot.toRouteReport(): RouteReport = RouteReport(
    id = id,
    userId = getString("userId") ?: "",
    userName = getString("userName") ?: "",
    issueType = getString("issueType") ?: "",
    description = getString("description") ?: "",
    imageUrl = getString("imageUrl") ?: "",
    location = getGeoPoint("location"),
    status = getString("status") ?: ReportStatus.PENDING,
    createdAt = getTimestamp("createdAt")
)

fun DocumentSnapshot.toPitStop(): PitStop = PitStop(
    id = id,
    name = getString("name") ?: "",
    category = getString("category") ?: "",
    address = getString("address") ?: "",
    location = getGeoPoint("location"),
    operatingHours = getString("operatingHours") ?: "9 AM - 6 PM",
    phone = getString("phone") ?: "",
    rating = (getDouble("rating") ?: 4.0).toFloat()
)

fun DocumentSnapshot.toDangerZone(): DangerZone = DangerZone(
    id = id,
    title = getString("title") ?: "",
    description = getString("description") ?: "",
    severity = getString("severity") ?: "medium",
    location = getGeoPoint("location")
)

fun DocumentSnapshot.toAppNotification(): AppNotification = AppNotification(
    id = id,
    userId = getString("userId") ?: "",
    title = getString("title") ?: "",
    message = getString("message") ?: "",
    type = getString("type") ?: "",
    read = getBoolean("read") ?: false,
    createdAt = getTimestamp("createdAt")
)
