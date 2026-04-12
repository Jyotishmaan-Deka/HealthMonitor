package com.healthmonitor.domain.usecase

import com.healthmonitor.domain.model.AISuggestion
import com.healthmonitor.domain.model.AlertLevel
import com.healthmonitor.domain.model.AlertType
import com.healthmonitor.domain.model.DailyStats
import com.healthmonitor.domain.model.HealthAlert
import com.healthmonitor.domain.model.HealthReading
import com.healthmonitor.domain.model.SuggestionCategory
import com.healthmonitor.domain.repository.HealthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

// ─── Get latest single reading ────────────────────────────────────────────────
class ObserveLatestReadingUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    operator fun invoke(): Flow<HealthReading?> = repository.observeLatestReading()
}

// ─── Get historical readings ──────────────────────────────────────────────────
class ObserveReadingsUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    operator fun invoke(limit: Int = 100): Flow<List<HealthReading>> =
        repository.observeReadings(limit)
}

// ─── Get daily stats for chart ────────────────────────────────────────────────
class ObserveDailyStatsUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    operator fun invoke(days: Int = 7): Flow<List<DailyStats>> =
        repository.observeDailyStats(days)
}

// ─── Save a reading & check for alerts ───────────────────────────────────────
class SaveReadingUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    suspend operator fun invoke(reading: HealthReading) {
        repository.saveReading(reading)
    }
}

// ─── Observe alerts ───────────────────────────────────────────────────────────
class ObserveAlertsUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    operator fun invoke(): Flow<List<HealthAlert>> = repository.observeAlerts()

    fun unreadCount(): Flow<Int> =
        repository.observeAlerts().map { list -> list.count { !it.isRead } }
}

// ─── Sync use case (called by WorkManager) ───────────────────────────────────
class SyncHealthDataUseCase @Inject constructor(
    private val repository: HealthRepository
) {
    suspend operator fun invoke(): Result<Int> = repository.syncPendingReadings()
}

// ─── AI suggestion engine (mock logic) ───────────────────────────────────────
class GetAISuggestionsUseCase @Inject constructor() {

    operator fun invoke(
        latestReading: HealthReading?,
        recentReadings: List<HealthReading>,
        dailyStats: List<DailyStats>
    ): List<AISuggestion> {
        val suggestions = mutableListOf<AISuggestion>()
        if (latestReading == null) return suggestions

        // Heart rate analysis
        val avgHr = recentReadings.takeIf { it.isNotEmpty() }
            ?.map { it.heartRate }?.average()?.toInt() ?: latestReading.heartRate

        when {
            avgHr > 100 ->
                suggestions += AISuggestion(
                    category = SuggestionCategory.STRESS,
                    title = "Elevated heart rate detected",
                    description = "Your average heart rate has been ${avgHr} bpm. " +
                        "Try 5 minutes of deep breathing or light stretching to lower it.",
                    priority = 1
                )
            avgHr < 55 ->
                suggestions += AISuggestion(
                    category = SuggestionCategory.ACTIVITY,
                    title = "Low resting heart rate",
                    description = "Heart rate of ${avgHr} bpm. If you're an athlete, this may be normal. " +
                        "Otherwise consider a gentle walk.",
                    priority = 2
                )
        }

        // Oxygen analysis
        if (latestReading.oxygenLevel in 90f..94f) {
            suggestions += AISuggestion(
                category = SuggestionCategory.BREATHING,
                title = "SpO₂ slightly below normal",
                description = "Your oxygen level is ${latestReading.oxygenLevel}%. " +
                    "Practice deep diaphragmatic breathing and ensure good ventilation.",
                priority = 1
            )
        }

        // Step count analysis
        val todaySteps = dailyStats.firstOrNull()?.totalSteps ?: 0
        when {
            todaySteps < 2000 ->
                suggestions += AISuggestion(
                    category = SuggestionCategory.ACTIVITY,
                    title = "Very low activity today",
                    description = "Only $todaySteps steps so far. A short 10-minute walk " +
                        "can significantly improve cardiovascular health.",
                    priority = 2
                )
            todaySteps > 10000 ->
                suggestions += AISuggestion(
                    category = SuggestionCategory.HYDRATION,
                    title = "Great activity! Stay hydrated",
                    description = "You've hit $todaySteps steps today. Make sure to " +
                        "drink at least 2-3 extra glasses of water.",
                    priority = 3
                )
        }

        // Night-time pattern (mock)
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        if (hour >= 22 && avgHr > 80) {
            suggestions += AISuggestion(
                category = SuggestionCategory.SLEEP,
                title = "Prepare for better sleep",
                description = "Your heart rate is still elevated at this hour. " +
                    "Avoid screens and try a 5-min wind-down routine.",
                priority = 2
            )
        }

        // Always give at least one suggestion
        if (suggestions.isEmpty()) {
            suggestions += AISuggestion(
                category = SuggestionCategory.ACTIVITY,
                title = "Looking good today!",
                description = "Your vitals are within healthy ranges. " +
                    "Keep up the good work and aim for 8000+ steps.",
                priority = 3
            )
        }

        return suggestions.sortedBy { it.priority }
    }
}
