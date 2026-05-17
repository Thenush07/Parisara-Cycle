package com.parisara.cycle.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import com.parisara.cycle.data.model.BuddyLocation
import com.parisara.cycle.data.model.BuddyRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class BuddyRepository(
    private val rtdb: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val buddyLocations = rtdb.getReference("buddyLocations")
    private val requests = firestore.collection("buddyRequests")

    fun observeNearbyBuddies(excludeUserId: String): Flow<List<BuddyLocation>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.children.mapNotNull { child ->
                    val loc = child.getValue(BuddyLocation::class.java) ?: return@mapNotNull null
                    if (loc.userId != excludeUserId && loc.isSharing) loc else null
                }
                trySend(list)
            }

            override fun onCancelled(error: DatabaseError) {
                trySend(emptyList())
            }
        }
        buddyLocations.addValueEventListener(listener)
        awaitClose { buddyLocations.removeEventListener(listener) }
    }

    suspend fun updateLocation(location: BuddyLocation): Result<Unit> = runCatching {
        buddyLocations.child(location.userId).setValue(
            location.copy(updatedAt = System.currentTimeMillis())
        ).await()
    }

    suspend fun stopSharing(userId: String): Result<Unit> = runCatching {
        buddyLocations.child(userId).child("isSharing").setValue(false).await()
    }

    suspend fun sendBuddyRequest(fromUserId: String, fromUserName: String, toUserId: String): Result<Unit> =
        runCatching {
            val doc = requests.document()
            doc.set(
                mapOf(
                    "fromUserId" to fromUserId,
                    "fromUserName" to fromUserName,
                    "toUserId" to toUserId,
                    "status" to "pending",
                    "createdAt" to Timestamp.now()
                )
            ).await()
        }

    fun observeIncomingRequests(userId: String): Flow<List<BuddyRequest>> = callbackFlow {
        val reg = requests.whereEqualTo("toUserId", userId)
            .whereEqualTo("status", "pending")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.map { doc ->
                    BuddyRequest(
                        id = doc.id,
                        fromUserId = doc.getString("fromUserId") ?: "",
                        fromUserName = doc.getString("fromUserName") ?: "",
                        toUserId = doc.getString("toUserId") ?: "",
                        status = doc.getString("status") ?: "pending",
                        createdAt = doc.getTimestamp("createdAt")
                    )
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { reg.remove() }
    }

    suspend fun respondToRequest(requestId: String, accept: Boolean): Result<Unit> = runCatching {
        requests.document(requestId).update("status", if (accept) "accepted" else "rejected").await()
    }
}
