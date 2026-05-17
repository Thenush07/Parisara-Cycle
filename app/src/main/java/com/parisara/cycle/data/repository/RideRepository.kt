package com.parisara.cycle.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.parisara.cycle.data.model.EcoCalculator
import com.parisara.cycle.data.model.EcoStats
import com.parisara.cycle.data.model.RideRecord
import com.parisara.cycle.util.toMap
import com.parisara.cycle.util.toRideRecord
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class RideRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val rides = firestore.collection("rides")

    suspend fun saveRide(ride: RideRecord): Result<String> = runCatching {
        val ref = rides.document()
        ref.set(ride.copy(id = ref.id, completedAt = Timestamp.now()).toMap()).await()
        ref.id
    }

    fun observeUserRides(userId: String): Flow<List<RideRecord>> = callbackFlow {
        val reg = rides.whereEqualTo("userId", userId)
            .orderBy("completedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.map { it.toRideRecord() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun computeEcoStats(rideList: List<RideRecord>): EcoStats {
        val now = Calendar.getInstance()
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val startOfWeek = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis
        val startOfMonth = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0)
        }.timeInMillis

        var daily = 0.0; var weekly = 0.0; var monthly = 0.0
        rideList.forEach { ride ->
            val ts = ride.completedAt?.toDate()?.time ?: return@forEach
            when {
                ts >= startOfDay -> daily += ride.distanceKm
                else -> {}
            }
            if (ts >= startOfWeek) weekly += ride.distanceKm
            if (ts >= startOfMonth) monthly += ride.distanceKm
        }
        return EcoStats(
            dailyDistanceKm = daily,
            weeklyDistanceKm = weekly,
            monthlyDistanceKm = monthly,
            dailyCo2Grams = EcoCalculator.co2FromDistanceKm(daily),
            weeklyCo2Grams = EcoCalculator.co2FromDistanceKm(weekly),
            monthlyCo2Grams = EcoCalculator.co2FromDistanceKm(monthly)
        )
    }
}
