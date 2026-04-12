package com.healthmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.healthmonitor.data.local.entity.HealthAlertEntity
import com.healthmonitor.data.local.entity.HealthReadingEntity
import com.healthmonitor.domain.model.DailyStats
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthDao {

    // ── Readings ──────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: HealthReadingEntity)

    @Query("SELECT * FROM health_readings ORDER BY timestamp DESC LIMIT :limit")
    fun observeReadings(limit: Int): Flow<List<HealthReadingEntity>>

    @Query("SELECT * FROM health_readings ORDER BY timestamp DESC LIMIT 1")
    fun observeLatestReading(): Flow<HealthReadingEntity?>

    @Query("SELECT * FROM health_readings WHERE isSynced = 0")
    suspend fun getUnsyncedReadings(): List<HealthReadingEntity>

    @Query("UPDATE health_readings SET isSynced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)

    @Query("DELETE FROM health_readings WHERE timestamp < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)

    // ── Daily stats aggregation ───────────────────────────────────────────────
    @Query("""
        SELECT
            (timestamp / 86400000) * 86400000 AS date,
            CAST(AVG(heartRate) AS INTEGER) AS avgHeartRate,
            MAX(heartRate) AS maxHeartRate,
            MIN(heartRate) AS minHeartRate,
            MAX(steps) AS totalSteps,
            AVG(oxygenLevel) AS avgOxygenLevel,
            COUNT(*) AS readingCount
        FROM health_readings
        WHERE timestamp >= :since
        GROUP BY (timestamp / 86400000)
        ORDER BY date DESC
        LIMIT :days
    """)
    fun observeDailyStats(since: Long, days: Int): Flow<List<DailyStatsDto>>

    // ── Alerts ────────────────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: HealthAlertEntity)

    @Query("SELECT * FROM health_alerts ORDER BY timestamp DESC LIMIT 50")
    fun observeAlerts(): Flow<List<HealthAlertEntity>>

    @Query("UPDATE health_alerts SET isRead = 1 WHERE id = :alertId")
    suspend fun markAlertRead(alertId: String)

    @Query("DELETE FROM health_alerts")
    suspend fun clearAllAlerts()
}

// Room projection data class for aggregated query
data class DailyStatsDto(
    val date: Long,
    val avgHeartRate: Int,
    val maxHeartRate: Int,
    val minHeartRate: Int,
    val totalSteps: Int,
    val avgOxygenLevel: Float,
    val readingCount: Int
) {
    fun toDomain() = com.healthmonitor.domain.model.DailyStats(
        date = date,
        avgHeartRate = avgHeartRate,
        maxHeartRate = maxHeartRate,
        minHeartRate = minHeartRate,
        totalSteps = totalSteps,
        avgOxygenLevel = avgOxygenLevel,
        readingCount = readingCount
    )
}
