package com.healthmonitor.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val notificationsEnabled: Boolean = true,
    val autoSyncEnabled: Boolean = true,
    val useMockBle: Boolean = true,
    val heartRateAlertHigh: Int = 120,
    val heartRateAlertLow: Int = 50,
    val oxygenAlertLow: Float = 95f,
    val fcmToken: String = ""
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // ── Keys ─────────────────────────────────────────────────────────────────
    private object Keys {
        val NOTIFICATIONS_ENABLED   = booleanPreferencesKey("notifications_enabled")
        val AUTO_SYNC_ENABLED       = booleanPreferencesKey("auto_sync_enabled")
        val USE_MOCK_BLE            = booleanPreferencesKey("use_mock_ble")
        val HR_ALERT_HIGH           = intPreferencesKey("hr_alert_high")
        val HR_ALERT_LOW            = intPreferencesKey("hr_alert_low")
        val OXYGEN_ALERT_LOW        = stringPreferencesKey("oxygen_alert_low")
        val FCM_TOKEN               = stringPreferencesKey("fcm_token")
    }

    // ── Observe ───────────────────────────────────────────────────────────────
    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
            autoSyncEnabled      = prefs[Keys.AUTO_SYNC_ENABLED]      ?: true,
            useMockBle           = prefs[Keys.USE_MOCK_BLE]           ?: true,
            heartRateAlertHigh   = prefs[Keys.HR_ALERT_HIGH]          ?: 120,
            heartRateAlertLow    = prefs[Keys.HR_ALERT_LOW]           ?: 50,
            oxygenAlertLow       = prefs[Keys.OXYGEN_ALERT_LOW]?.toFloatOrNull() ?: 95f,
            fcmToken             = prefs[Keys.FCM_TOKEN]              ?: ""
        )
    }

    // ── Update ────────────────────────────────────────────────────────────────
    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setAutoSyncEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_SYNC_ENABLED] = enabled }
    }

    suspend fun setUseMockBle(mock: Boolean) {
        dataStore.edit { it[Keys.USE_MOCK_BLE] = mock }
    }

    suspend fun setHeartRateAlertHigh(value: Int) {
        dataStore.edit { it[Keys.HR_ALERT_HIGH] = value }
    }

    suspend fun setHeartRateAlertLow(value: Int) {
        dataStore.edit { it[Keys.HR_ALERT_LOW] = value }
    }

    suspend fun setOxygenAlertLow(value: Float) {
        dataStore.edit { it[Keys.OXYGEN_ALERT_LOW] = value.toString() }
    }

    suspend fun setFcmToken(token: String) {
        dataStore.edit { it[Keys.FCM_TOKEN] = token }
    }
}
