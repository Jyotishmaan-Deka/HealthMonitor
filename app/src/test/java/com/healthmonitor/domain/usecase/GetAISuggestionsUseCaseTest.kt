package com.healthmonitor.domain.usecase

import com.healthmonitor.domain.model.AlertLevel
import com.healthmonitor.domain.model.HealthReading
import com.healthmonitor.domain.model.SuggestionCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetAISuggestionsUseCaseTest {

    private lateinit var useCase: GetAISuggestionsUseCase

    @Before
    fun setUp() {
        useCase = GetAISuggestionsUseCase()
    }

    private fun reading(
        heartRate: Int = 72,
        steps: Int = 5000,
        oxygen: Float = 98f
    ) = HealthReading(
        id = "test",
        heartRate = heartRate,
        steps = steps,
        oxygenLevel = oxygen,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun `returns at least one suggestion for healthy readings`() {
        val suggestions = useCase(reading(), emptyList(), emptyList())
        assertTrue("Should produce at least one suggestion", suggestions.isNotEmpty())
    }

    @Test
    fun `elevated average HR triggers stress suggestion`() {
        val readings = List(10) { reading(heartRate = 115) }
        val suggestions = useCase(reading(heartRate = 115), readings, emptyList())
        assertTrue(
            "Should contain STRESS suggestion",
            suggestions.any { it.category == SuggestionCategory.STRESS }
        )
    }

    @Test
    fun `low oxygen triggers breathing suggestion`() {
        val suggestions = useCase(reading(oxygen = 92f), emptyList(), emptyList())
        assertTrue(
            "Should contain BREATHING suggestion",
            suggestions.any { it.category == SuggestionCategory.BREATHING }
        )
    }

    @Test
    fun `suggestions are sorted by priority ascending`() {
        val readings = List(10) { reading(heartRate = 115) }
        val suggestions = useCase(reading(heartRate = 115, oxygen = 92f), readings, emptyList())
        val priorities = suggestions.map { it.priority }
        assertEquals(priorities.sorted(), priorities)
    }

    @Test
    fun `normal oxygen level does not trigger breathing suggestion`() {
        val suggestions = useCase(reading(oxygen = 98f), emptyList(), emptyList())
        assertTrue(
            "Should NOT contain BREATHING for normal SpO2",
            suggestions.none { it.category == SuggestionCategory.BREATHING }
        )
    }
}

class HealthReadingAlertLevelTest {

    private fun reading(heartRate: Int = 72, oxygen: Float = 98f) = HealthReading(
        id = "x", heartRate = heartRate, oxygenLevel = oxygen,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun `normal reading has NORMAL alert level`() {
        assertEquals(AlertLevel.NORMAL, reading(heartRate = 72, oxygen = 98f).alertLevel)
    }

    @Test
    fun `heart rate over 120 is WARNING`() {
        assertEquals(AlertLevel.WARNING, reading(heartRate = 125).alertLevel)
    }

    @Test
    fun `heart rate over 140 is CRITICAL`() {
        assertEquals(AlertLevel.CRITICAL, reading(heartRate = 145).alertLevel)
    }

    @Test
    fun `oxygen below 90 is CRITICAL`() {
        assertEquals(AlertLevel.CRITICAL, reading(oxygen = 88f).alertLevel)
    }

    @Test
    fun `oxygen 90 to 94 is WARNING`() {
        assertEquals(AlertLevel.WARNING, reading(oxygen = 92f).alertLevel)
    }

    @Test
    fun `isHeartRateAbnormal true for hr above 120`() {
        assertTrue(reading(heartRate = 125).isHeartRateAbnormal)
    }

    @Test
    fun `isHeartRateAbnormal true for hr below 50`() {
        assertTrue(reading(heartRate = 45).isHeartRateAbnormal)
    }

    @Test
    fun `isOxygenLow true for oxygen below 95`() {
        assertTrue(reading(oxygen = 93f).isOxygenLow)
    }

    @Test
    fun `isOxygenLow false for oxygen 0 (no device)`() {
        val r = reading(oxygen = 0f)
        assertTrue("0f means no reading — should not be flagged", !r.isOxygenLow)
    }
}
