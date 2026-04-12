package com.healthmonitor.data.repository

import com.healthmonitor.data.local.dao.HealthDao
import com.healthmonitor.data.local.entity.HealthAlertEntity
import com.healthmonitor.data.local.entity.toEntity
import com.healthmonitor.data.remote.firebase.FirebaseService
import com.healthmonitor.domain.model.AlertLevel
import com.healthmonitor.domain.model.AlertType
import com.healthmonitor.domain.model.DailyStats
import com.healthmonitor.domain.model.HealthAlert
import com.healthmonitor.domain.model.HealthReading
import com.healthmonitor.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthRepositoryImpl @Inject constructor(
    private val dao: HealthDao,
    private val firebase: FirebaseService
) : HealthRepository {

    override fun observeLatestReading(): Flow<HealthReading?> =
        dao.observeLatestReading().map { it?.toDomain() }

    override fun observeReadings(limit: Int): Flow<List<HealthReading>> =
        dao.observeReadings(limit).map { list -> list.map { it.toDomain() } }

    override fun observeDailyStats(days: Int): Flow<List<DailyStats>> {
        val since = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return dao.observeDailyStats(since, days).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun saveReading(reading: HealthReading) {
        dao.insertReading(reading.toEntity())
        checkAndCreateAlerts(reading)
    }

    override suspend fun syncPendingReadings(): Result<Int> {
        val unsynced = dao.getUnsyncedReadings()
        if (unsynced.isEmpty()) return Result.success(0)

        return firebase.uploadReadings(unsynced.map { it.toDomain() })
            .map {
                dao.markSynced(unsynced.map { e -> e.id })
                unsynced.size
            }
    }

    override fun observeAlerts(): Flow<List<HealthAlert>> =
        dao.observeAlerts().map { list -> list.map { it.toDomain() } }

    override suspend fun markAlertRead(alertId: String) = dao.markAlertRead(alertId)

    override suspend fun clearAllAlerts() = dao.clearAllAlerts()

    override suspend fun getCurrentUserId(): String? = firebase.getCurrentUserId()

    override fun isLoggedIn(): Boolean = firebase.isLoggedIn()

    // ── Alert logic ───────────────────────────────────────────────────────────

    private suspend fun checkAndCreateAlerts(reading: HealthReading) {
        when {
            reading.heartRate > 140 -> createAlert(
                type = AlertType.HIGH_HEART_RATE,
                value = "${reading.heartRate} bpm",
                message = "Heart rate critically high: ${reading.heartRate} bpm. Rest immediately."
            )
            reading.heartRate > 120 -> createAlert(
                type = AlertType.HIGH_HEART_RATE,
                value = "${reading.heartRate} bpm",
                message = "Elevated heart rate: ${reading.heartRate} bpm."
            )
            reading.heartRate < 45 -> createAlert(
                type = AlertType.LOW_HEART_RATE,
                value = "${reading.heartRate} bpm",
                message = "Heart rate very low: ${reading.heartRate} bpm."
            )
        }

        when {
            reading.oxygenLevel < 90f && reading.oxygenLevel > 0f -> createAlert(
                type = AlertType.CRITICAL_OXYGEN,
                value = "${reading.oxygenLevel}%",
                message = "Critical SpO₂: ${reading.oxygenLevel}%. Seek medical attention."
            )
            reading.oxygenLevel < 95f && reading.oxygenLevel > 0f -> createAlert(
                type = AlertType.LOW_OXYGEN,
                value = "${reading.oxygenLevel}%",
                message = "SpO₂ below normal: ${reading.oxygenLevel}%."
            )
        }
    }

    private suspend fun createAlert(type: AlertType, value: String, message: String) {
        dao.insertAlert(
            HealthAlertEntity(
                id = UUID.randomUUID().toString(),
                type = type.name,
                value = value,
                message = message,
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
        )
    }
}
