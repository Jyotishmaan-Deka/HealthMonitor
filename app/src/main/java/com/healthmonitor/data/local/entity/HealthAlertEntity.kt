package com.healthmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.healthmonitor.domain.model.AlertType
import com.healthmonitor.domain.model.HealthAlert

@Entity(tableName = "health_alerts")
data class HealthAlertEntity(
    @PrimaryKey val id: String,
    val type: String,
    val value: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean
) {
    fun toDomain() = HealthAlert(
        id = id,
        type = AlertType.valueOf(type),
        value = value,
        message = message,
        timestamp = timestamp,
        isRead = isRead
    )
}

fun HealthAlert.toEntity() = HealthAlertEntity(
    id = id,
    type = type.name,
    value = value,
    message = message,
    timestamp = timestamp,
    isRead = isRead
)
