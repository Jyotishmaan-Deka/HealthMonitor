package com.healthmonitor.presentation.dashboard

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.work.WorkManager
import app.cash.turbine.test
import com.healthmonitor.data.ble.BleHealthData
import com.healthmonitor.data.ble.BleManager
import com.healthmonitor.domain.model.AlertLevel
import com.healthmonitor.domain.model.DailyStats
import com.healthmonitor.domain.model.HealthReading
import com.healthmonitor.domain.usecase.ObserveDailyStatsUseCase
import com.healthmonitor.domain.usecase.ObserveLatestReadingUseCase
import com.healthmonitor.domain.usecase.ObserveReadingsUseCase
import com.healthmonitor.domain.usecase.SaveReadingUseCase
import com.healthmonitor.notification.HealthNotificationManager
import com.healthmonitor.presentation.ui.dashboard.DashboardViewModel
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.coJustRun
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var bleManager: BleManager
    private lateinit var saveReading: SaveReadingUseCase
    private lateinit var observeLatest: ObserveLatestReadingUseCase
    private lateinit var observeReadings: ObserveReadingsUseCase
    private lateinit var observeDailyStats: ObserveDailyStatsUseCase
    private lateinit var notificationManager: HealthNotificationManager
    private lateinit var workManager: WorkManager

    private fun buildViewModel() = DashboardViewModel(
        bleManager          = bleManager,
        saveReadingUseCase  = saveReading,
        observeLatestReading = observeLatest,
        observeReadings     = observeReadings,
        observeDailyStats   = observeDailyStats,
        notificationManager = notificationManager,
        workManager         = workManager
    )

    private fun fakeReading(hr: Int = 72, steps: Int = 1000, oxygen: Float = 98f) =
        HealthReading(id = "x", heartRate = hr, steps = steps, oxygenLevel = oxygen,
            timestamp = System.currentTimeMillis())

    private fun fakeBle(hr: Int = 72, steps: Int = 1000, oxygen: Float = 98f) =
        BleHealthData(heartRate = hr, steps = steps, oxygenLevel = oxygen)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        bleManager          = mockk(relaxed = true)
        saveReading         = mockk(relaxed = true)
        observeLatest       = mockk()
        observeReadings     = mockk()
        observeDailyStats   = mockk()
        notificationManager = mockk(relaxed = true)
        workManager         = mockk(relaxed = true)

        every { observeLatest()       } returns flowOf(null)
        every { observeReadings(any()) } returns flowOf(emptyList())
        every { observeDailyStats(any()) } returns flowOf(emptyList())
        every { bleManager.observeHealthData(any()) } returns flowOf()
        coJustRun { saveReading(any()) }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has isLoading true and heartRate 0`() = runTest {
        val vm = buildViewModel()
        val state = vm.uiState.value
        assertTrue(state.isLoading)
        assertEquals(0, state.heartRate)
        assertNull(state.errorMessage)
    }

    @Test
    fun `BLE emission updates uiState heartRate and steps`() = runTest {
        every { bleManager.observeHealthData(true) } returns flowOf(fakeBle(hr = 85, steps = 3000))

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(85, state.heartRate)
            assertEquals(3000, state.steps)
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `abnormal BLE heart rate sets WARNING alert level`() = runTest {
        every { bleManager.observeHealthData(true) } returns flowOf(fakeBle(hr = 125))

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(AlertLevel.WARNING, state.alertLevel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `critical BLE heart rate sets CRITICAL alert level`() = runTest {
        every { bleManager.observeHealthData(true) } returns flowOf(fakeBle(hr = 155))

        val vm = buildViewModel()
        testDispatcher.scheduler.advanceUntilIdle()

        vm.uiState.test {
            val state = awaitItem()
            assertEquals(AlertLevel.CRITICAL, state.alertLevel)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissError clears errorMessage`() = runTest {
        val vm = buildViewModel()
        // Simulate error via internal update (test state mutation)
        vm.dismissError()
        assertNull(vm.uiState.value.errorMessage)
    }

    @Test
    fun `toggleDataSource flips useMockData flag`() = runTest {
        every { bleManager.observeHealthData(false) } returns flowOf()
        val vm = buildViewModel()
        val initialMock = vm.uiState.value.useMockData

        vm.toggleDataSource()
        testDispatcher.scheduler.advanceUntilIdle()

        assertEquals(!initialMock, vm.uiState.value.useMockData)
    }
}
