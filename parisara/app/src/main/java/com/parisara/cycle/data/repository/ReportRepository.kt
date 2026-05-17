package com.parisara.cycle.data.repository

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.parisara.cycle.data.model.ReportStatus
import com.parisara.cycle.data.model.RouteReport
import com.parisara.cycle.util.toRouteReport
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val reports = firestore.collection("reports")

    suspend fun submitReport(
        userId: String,
        userName: String,
        issueType: String,
        description: String,
        location: GeoPoint,
        imageUri: Uri?
    ): Result<String> = runCatching {
        var imageUrl = ""
        if (imageUri != null) {
            val ref = storage.reference.child("reports/${System.currentTimeMillis()}.jpg")
            ref.putFile(imageUri).await()
            imageUrl = ref.downloadUrl.await().toString()
        }
        val doc = reports.document()
        doc.set(
            mapOf(
                "userId" to userId,
                "userName" to userName,
                "issueType" to issueType,
                "description" to description,
                "imageUrl" to imageUrl,
                "location" to location,
                "status" to ReportStatus.PENDING,
                "createdAt" to Timestamp.now()
            )
        ).await()
        doc.id
    }

    fun observeReports(): Flow<List<RouteReport>> = callbackFlow {
        val reg = reports.addSnapshotListener { snap, _ ->
            trySend(snap?.documents?.map { it.toRouteReport() } ?: emptyList())
        }
        awaitClose { reg.remove() }
    }

    suspend fun updateStatus(reportId: String, status: String): Result<Unit> = runCatching {
        reports.document(reportId).update("status", status).await()
    }
}
