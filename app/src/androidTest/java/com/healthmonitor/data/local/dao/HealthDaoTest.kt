package com.healthmonitor.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.healthmonitor.data.local.database.HealthDatabase
import com.healthmonitor.data.local.entity.HealthAlertEntity
import com.healthmonitor.data.local.entity.HealthReadingEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HealthDaoTest {

    private lateinit var database: HealthDatabase
    private lateinit var dao: HealthDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, HealthDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.healthDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    // ── Readings ──────────────────────────────────────────────────────────────

    @Test
    fun insertReading_andObserveLatest_returnsInserted() = runTest {
        val reading = testReading(id = "r1", heartRate = 75)
        dao.insertReading(reading)

        val latest = dao.observeLatestReading().first()
        assertEquals("r1", latest?.id)
        assertEquals(75, latest?.heartRate)
    }

    @Test
    fun observeLatestReading_withNoData_returnsNull() = runTest {
        val latest = dao.observeLatestReading().first()
        assertNull(latest)
    }

    @Test
    fun observeReadings_returnsNewestFirst() = runTest {
        dao.insertReading(testReading("r1", timestamp = 1000L))
        dao.insertReading(testReading("r2", timestamp = 3000L))
        dao.insertReading(testReading("r3", timestamp = 2000L))

        val list = dao.observeReadings(10).first()
        assertEquals(listOf("r2", "r3", "r1"), list.map { it.id })
    }

    @Test
    fun observeReadings_respectsLimit() = runTest {
        repeat(10) { i -> dao.insertReading(testReading("r$i", timestamp = i.toLong())) }

        val list = dao.observeReadings(5).first()
        assertEquals(5, list.size)
    }

    @Test
    fun getUnsyncedReadings_returnsOnlyUnsynced() = runTest {
        dao.insertReading(testReading("r1", isSynced = false))
        dao.insertReading(testReading("r2", isSynced = true))
        dao.insertReading(testReading("r3", isSynced = false))

        val unsynced = dao.getUnsyncedReadings()
        assertEquals(2, unsynced.size)
        assertTrue(unsynced.all { !it.isSynced })
    }

    @Test
    fun markSynced_updatesCorrectRows() = runTest {
        dao.insertReading(testReading("r1", isSynced = false))
        dao.insertReading(testReading("r2", isSynced = false))
        dao.insertReading(testReading("r3", isSynced = false))

        dao.markSynced(listOf("r1", "r3"))

        val unsynced = dao.getUnsyncedReadings()
        assertEquals(1, unsynced.size)
        assertEquals("r2", unsynced.first().id)
    }

    @Test
    fun insertReading_withDuplicateId_replaces() = runTest {
        dao.insertReading(testReading("r1", heartRate = 70))
        dao.insertReading(testReading("r1", heartRate = 90))   // same ID

        val readings = dao.observeReadings(10).first()
        assertEquals(1, readings.size)
        assertEquals(90, readings.first().heartRate)
    }

    @Test
    fun deleteOlderThan_removesOldReadings() = runTest {
        dao.insertReading(testReading("r1", timestamp = 100L))
        dao.insertReading(testReading("r2", timestamp = 500L))
        dao.insertReading(testReading("r3", timestamp = 900L))

        dao.deleteOlderThan(cutoff = 400L)

        val remaining = dao.observeReadings(10).first()
        assertEquals(2, remaining.size)
        assertTrue(remaining.none { it.id == "r1" })
    }

    // ── Alerts ────────────────────────────────────────────────────────────────

    @Test
    fun insertAlert_andObserveAlerts_returnsInserted() = runTest {
        val alert = testAlert("a1", message = "Test alert")
        dao.insertAlert(alert)

        val alerts = dao.observeAlerts().first()
        assertEquals(1, alerts.size)
        assertEquals("Test alert", alerts.first().message)
    }

    @Test
    fun markAlertRead_updatesIsRead() = runTest {
        dao.insertAlert(testAlert("a1", isRead = false))

        dao.markAlertRead("a1")

        val alert = dao.observeAlerts().first().first()
        assertTrue(alert.isRead)
    }

    @Test
    fun clearAllAlerts_deletesAll() = runTest {
        dao.insertAlert(testAlert("a1"))
        dao.insertAlert(testAlert("a2"))

        dao.clearAllAlerts()

        assertTrue(dao.observeAlerts().first().isEmpty())
    }

    @Test
    fun observeAlerts_returnsNewestFirst() = runTest {
        dao.insertAlert(testAlert("a1", timestamp = 100L))
        dao.insertAlert(testAlert("a2", timestamp = 300L))
        dao.insertAlert(testAlert("a3", timestamp = 200L))

        val alerts = dao.observeAlerts().first()
        assertEquals(listOf("a2", "a3", "a1"), alerts.map { it.id })
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun testReading(
        id: String,
        heartRate: Int = 72,
        steps: Int = 1000,
        oxygenLevel: Float = 98f,
        timestamp: Long = System.currentTimeMillis(),
        isSynced: Boolean = false
    ) = HealthReadingEntity(
        id = id,
        userId = "user1",
        heartRate = heartRate,
        steps = steps,
        oxygenLevel = oxygenLevel,
        timestamp = timestamp,
        isSynced = isSynced
    )

    private fun testAlert(
        id: String,
        type: String = "HIGH_HEART_RATE",
        value: String = "125 bpm",
        message: String = "Alert message",
        timestamp: Long = System.currentTimeMillis(),
        isRead: Boolean = false
    ) = HealthAlertEntity(
        id = id,
        type = type,
        value = value,
        message = message,
        timestamp = timestamp,
        isRead = isRead
    )
}
