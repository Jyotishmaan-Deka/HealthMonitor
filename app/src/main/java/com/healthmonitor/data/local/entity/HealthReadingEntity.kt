package com.healthmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.healthmonitor.domain.model.HealthReading

@Entity(
    tableName = "health_readings",
    indices = [Index("timestamp"), Index("isSynced")]
)
data class HealthReadingEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val heartRate: Int,
    val steps: Int,
    val oxygenLevel: Float,
    val timestamp: Long,
    val isSynced: Boolean
) {
    fun toDomain() = HealthReading(
        id = id,
        userId = userId,
        heartRate = heartRate,
        steps = steps,
        oxygenLevel = oxygenLevel,
        timestamp = timestamp,
        isSynced = isSynced
    )
}

fun HealthReading.toEntity() = HealthReadingEntity(
    id = id.ifEmpty { java.util.UUID.randomUUID().toString() },
    userId = userId,
    heartRate = heartRate,
    steps = steps,
    oxygenLevel = oxygenLevel,
    timestamp = timestamp,
    isSynced = isSynced
)
