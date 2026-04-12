package com.healthmonitor.domain.repository

import com.healthmonitor.domain.model.DailyStats
import com.healthmonitor.domain.model.HealthAlert
import com.healthmonitor.domain.model.HealthReading
import kotlinx.coroutines.flow.Flow

interface HealthRepository {

    // Real-time stream of latest reading (from BLE or Room)
    fun observeLatestReading(): Flow<HealthReading?>

    // Historical readings, newest first
    fun observeReadings(limit: Int = 100): Flow<List<HealthReading>>

    // Daily aggregated stats (last N days)
    fun observeDailyStats(days: Int = 7): Flow<List<DailyStats>>

    // Save a new reading locally
    suspend fun saveReading(reading: HealthReading)

    // Sync unsynced readings to Firestore
    suspend fun syncPendingReadings(): Result<Int>

    // Alerts
    fun observeAlerts(): Flow<List<HealthAlert>>
    suspend fun markAlertRead(alertId: String)
    suspend fun clearAllAlerts()

    // User management
    suspend fun getCurrentUserId(): String?
    fun isLoggedIn(): Boolean
}
