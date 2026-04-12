package com.healthmonitor.data.ble

import android.content.Context
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BleManagerTest {

    private lateinit var context: Context
    private lateinit var bleManager: BleManager

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        bleManager = BleManager(context)
    }

    @Test
    fun `mock data emits BleHealthData with valid heart rate range`() = runTest {
        bleManager.observeHealthData(useMock = true).test {
            val first = awaitItem()
            assertTrue(
                "Heart rate should be in [40, 180], got ${first.heartRate}",
                first.heartRate in 40..180
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mock data emits BleHealthData with valid SpO2 range`() = runTest {
        bleManager.observeHealthData(useMock = true).test {
            val first = awaitItem()
            assertTrue(
                "SpO2 should be in [88, 100], got ${first.oxygenLevel}",
                first.oxygenLevel in 88f..100f
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mock data device address is MOCK`() = runTest {
        bleManager.observeHealthData(useMock = true).test {
            val first = awaitItem()
            assertEquals("MOCK", first.deviceAddress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `mock data steps are non-negative`() = runTest {
        bleManager.observeHealthData(useMock = true).test {
            val first = awaitItem()
            assertTrue("Steps should be >= 0", first.steps >= 0)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `multiple emissions have non-decreasing steps`() = runTest {
        bleManager.observeHealthData(useMock = true).test {
            val first = awaitItem()
            val second = awaitItem()
            assertTrue(
                "Steps should never decrease: ${first.steps} -> ${second.steps}",
                second.steps >= first.steps
            )
            cancelAndIgnoreRemainingEvents()
        }
    }
}
