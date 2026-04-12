package com.healthmonitor.data.repository

import com.healthmonitor.data.local.dao.HealthAlertEntity
import com.healthmonitor.data.local.dao.HealthDao
import com.healthmonitor.data.remote.firebase.FirebaseService
import com.healthmonitor.domain.model.AlertType
import com.healthmonitor.domain.model.HealthReading
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HealthRepositoryImplTest {

    private lateinit var dao: HealthDao
    private lateinit var firebase: FirebaseService
    private lateinit var repository: HealthRepositoryImpl

    @Before
    fun setUp() {
        dao = mockk()
        firebase = mockk()
        repository = HealthRepositoryImpl(dao, firebase)

        // Default stubs
        coEvery { dao.insertReading(any()) } just Runs
        coEvery { dao.insertAlert(any()) } just Runs
    }

    private fun reading(
        heartRate: Int = 72,
        oxygen: Float = 98f
    ) = HealthReading(
        id = "test-id",
        heartRate = heartRate,
        oxygenLevel = oxygen,
        steps = 1000,
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun `saving a normal reading does not create an alert`() = runTest {
        repository.saveReading(reading(heartRate = 72, oxygen = 98f))

        coVerify(exactly = 0) { dao.insertAlert(any()) }
    }

    @Test
    fun `high heart rate above 120 triggers HIGH_HEART_RATE alert`() = runTest {
        val alertSlot = slot<HealthAlertEntity>()
        coEvery { dao.insertAlert(capture(alertSlot)) } just Runs

        repository.saveReading(reading(heartRate = 125))

        coVerify(exactly = 1) { dao.insertAlert(any()) }
        assertEquals(AlertType.HIGH_HEART_RATE.name, alertSlot.captured.type)
        assertTrue(alertSlot.captured.message.contains("125"))
    }

    @Test
    fun `critical heart rate above 140 still uses HIGH_HEART_RATE type`() = runTest {
        val alertSlot = slot<HealthAlertEntity>()
        coEvery { dao.insertAlert(capture(alertSlot)) } just Runs

        repository.saveReading(reading(heartRate = 155))

        assertEquals(AlertType.HIGH_HEART_RATE.name, alertSlot.captured.type)
        assertTrue(alertSlot.captured.message.contains("critically high"))
    }

    @Test
    fun `low heart rate below 45 triggers LOW_HEART_RATE alert`() = runTest {
        val alertSlot = slot<HealthAlertEntity>()
        coEvery { dao.insertAlert(capture(alertSlot)) } just Runs

        repository.saveReading(reading(heartRate = 40))

        coVerify(exactly = 1) { dao.insertAlert(any()) }
        assertEquals(AlertType.LOW_HEART_RATE.name, alertSlot.captured.type)
    }

    @Test
    fun `oxygen below 95 but above 90 triggers LOW_OXYGEN alert`() = runTest {
        val alertSlot = slot<HealthAlertEntity>()
        coEvery { dao.insertAlert(capture(alertSlot)) } just Runs

        repository.saveReading(reading(oxygen = 92f))

        assertEquals(AlertType.LOW_OXYGEN.name, alertSlot.captured.type)
    }

    @Test
    fun `oxygen below 90 triggers CRITICAL_OXYGEN alert`() = runTest {
        val alertSlot = slot<HealthAlertEntity>()
        coEvery { dao.insertAlert(capture(alertSlot)) } just Runs

        repository.saveReading(reading(oxygen = 87f))

        assertEquals(AlertType.CRITICAL_OXYGEN.name, alertSlot.captured.type)
        assertTrue(alertSlot.captured.message.contains("Seek medical"))
    }

    @Test
    fun `oxygen of 0 (no device) does not trigger oxygen alert`() = runTest {
        repository.saveReading(reading(oxygen = 0f))

        // Only possible alert is HR (72 is normal) — no oxygen alert
        coVerify(exactly = 0) { dao.insertAlert(any()) }
    }

    @Test
    fun `syncPendingReadings returns 0 when nothing to sync`() = runTest {
        coEvery { dao.getUnsyncedReadings() } returns emptyList()

        val result = repository.syncPendingReadings()

        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull())
    }
}
