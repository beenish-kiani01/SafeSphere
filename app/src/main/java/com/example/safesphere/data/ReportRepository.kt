package com.example.safesphere.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReportRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val reportsCollection = firestore.collection("reports")

    suspend fun submitReport(report: Report): Boolean {
        return try {
            val doc = reportsCollection.document()
            reportsCollection.document(doc.id).set(report.copy(
                id = doc.id,
                userId = auth.currentUser?.uid ?: ""
            )).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getAllReports(): Flow<List<Report>> = callbackFlow {
        val listener = reportsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                val reports = snapshot?.toObjects(Report::class.java) ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }
}
