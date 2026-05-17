package com.parisara.cycle.data.repository

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import com.parisara.cycle.data.model.ACHIEVEMENTS
import com.parisara.cycle.data.model.UserProfile
import com.parisara.cycle.util.toMap
import com.parisara.cycle.util.toUserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class UserRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val users = firestore.collection("users")

    suspend fun createUser(profile: UserProfile): Result<Unit> = runCatching {
        users.document(profile.uid).set(profile.toMap(), SetOptions.merge()).await()
    }

    suspend fun uploadProfileImage(uid: String, imageUri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("profiles/$uid.jpg")
        ref.putFile(imageUri).await()
        ref.downloadUrl.await().toString()
    }

    suspend fun getUser(uid: String): Result<UserProfile> = runCatching {
        users.document(uid).get().await().toUserProfile()
    }

    fun observeUser(uid: String): Flow<UserProfile?> = callbackFlow {
        val reg = users.document(uid).addSnapshotListener { snap, _ ->
            trySend(snap?.toUserProfile())
        }
        awaitClose { reg.remove() }
    }

    suspend fun updateProfile(profile: UserProfile): Result<Unit> = runCatching {
        users.document(profile.uid).set(profile.toMap(), SetOptions.merge()).await()
    }

    suspend fun addRideStats(uid: String, distanceKm: Double, co2Grams: Double): Result<Unit> =
        runCatching {
            firestore.runTransaction { tx ->
                val ref = users.document(uid)
                val snap = tx.get(ref)
                val current = snap.toUserProfile()
                val newDistance = current.totalDistanceKm + distanceKm
                val newCo2 = current.totalCo2SavedGrams + co2Grams
                val unlocked = ACHIEVEMENTS.filter { ach ->
                    when (ach.id) {
                        "eco_warrior" -> newCo2 >= 1000.0
                        else -> newDistance >= ach.requiredDistanceKm
                    }
                }.map { it.id }
                val mergedAchievements = (current.achievements + unlocked).distinct()
                tx.update(
                    ref,
                    mapOf(
                        "totalDistanceKm" to newDistance,
                        "totalCo2SavedGrams" to newCo2,
                        "totalRides" to current.totalRides + 1,
                        "achievements" to mergedAchievements
                    )
                )
            }.await()
        }
}
