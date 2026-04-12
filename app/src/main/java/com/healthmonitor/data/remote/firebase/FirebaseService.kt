package com.healthmonitor.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.healthmonitor.domain.model.HealthReading
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseService @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // ── Auth ──────────────────────────────────────────────────────────────────

    fun getCurrentUserId(): String? = auth.currentUser?.uid

    fun isLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("No user after sign-in")
    }

    suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        result.user?.uid ?: error("No user after sign-up")
    }

    fun signOut() = auth.signOut()

    // ── Firestore – health readings ───────────────────────────────────────────

    suspend fun uploadReadings(readings: List<HealthReading>): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not authenticated")
        val batch = firestore.batch()

        readings.forEach { reading ->
            val ref = firestore
                .collection("users").document(uid)
                .collection("readings").document(reading.id)
            batch.set(ref, reading.toFirestoreMap())
        }

        batch.commit().await()
    }

    // ── Firestore – user profile ──────────────────────────────────────────────

    suspend fun getUserProfile(): Result<Map<String, Any>> = runCatching {
        val uid = getCurrentUserId() ?: error("Not authenticated")
        val snapshot = firestore.collection("users").document(uid).get().await()
        snapshot.data ?: emptyMap()
    }

    suspend fun updateUserProfile(data: Map<String, Any>): Result<Unit> = runCatching {
        val uid = getCurrentUserId() ?: error("Not authenticated")
        firestore.collection("users").document(uid).update(data).await()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun HealthReading.toFirestoreMap() = mapOf(
        "userId"       to userId,
        "heartRate"    to heartRate,
        "steps"        to steps,
        "oxygenLevel"  to oxygenLevel,
        "timestamp"    to timestamp,
        "isSynced"     to true
    )
}
