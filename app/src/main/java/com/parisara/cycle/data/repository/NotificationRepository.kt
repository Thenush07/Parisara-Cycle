package com.parisara.cycle.data.repository

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.parisara.cycle.data.model.AppNotification
import com.parisara.cycle.util.toAppNotification
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotificationRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val notifications = firestore.collection("notifications")

    fun observeNotifications(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val reg = notifications.whereEqualTo("userId", userId)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.documents?.map { it.toAppNotification() } ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun sendNotification(
        userId: String,
        title: String,
        message: String,
        type: String
    ): Result<Unit> = runCatching {
        notifications.add(
            mapOf(
                "userId" to userId,
                "title" to title,
                "message" to message,
                "type" to type,
                "read" to false,
                "createdAt" to Timestamp.now()
            )
        ).await()
    }

    suspend fun markAsRead(notificationId: String): Result<Unit> = runCatching {
        notifications.document(notificationId).update("read", true).await()
    }
}
