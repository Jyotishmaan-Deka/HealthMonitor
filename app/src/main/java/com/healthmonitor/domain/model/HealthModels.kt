package com.healthmonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class HealthReading(
    val id: String = "",
    val userId: String = "",
    val heartRate: Int = 0,          // bpm
    val steps: Int = 0,
    val oxygenLevel: Float = 0f,     // SpO2 %
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
) {
    val isHeartRateAbnormal: Boolean
        get() = heartRate < 50 || heartRate > 120

    val isOxygenLow: Boolean
        get() = oxygenLevel < 95f && oxygenLevel > 0f

    val alertLevel: AlertLevel
        get() = when {
            heartRate > 140 || heartRate < 45 || oxygenLevel < 90f -> AlertLevel.CRITICAL
            isHeartRateAbnormal || isOxygenLow                     -> AlertLevel.WARNING
            else                                                    -> AlertLevel.NORMAL
        }
}

enum class AlertLevel { NORMAL, WARNING, CRITICAL }

@Serializable
data class DailyStats(
    val date: Long,
    val avgHeartRate: Int,
    val maxHeartRate: Int,
    val minHeartRate: Int,
    val totalSteps: Int,
    val avgOxygenLevel: Float,
    val readingCount: Int
)

data class HealthAlert(
    val id: String,
    val type: AlertType,
    val value: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean = false
)

enum class AlertType {
    HIGH_HEART_RATE,
    LOW_HEART_RATE,
    LOW_OXYGEN,
    CRITICAL_OXYGEN,
    INACTIVITY
}

data class AISuggestion(
    val category: SuggestionCategory,
    val title: String,
    val description: String,
    val priority: Int   // 1 = high, 3 = low
)

enum class SuggestionCategory {
    ACTIVITY, BREATHING, SLEEP, HYDRATION, STRESS
}
