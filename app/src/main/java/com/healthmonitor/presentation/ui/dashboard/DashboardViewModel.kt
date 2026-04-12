package com.healthmonitor.presentation.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
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
import com.healthmonitor.worker.HealthSyncWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class DashboardUiState(
    val heartRate: Int = 0,
    val steps: Int = 0,
    val oxygenLevel: Float = 0f,
    val alertLevel: AlertLevel = AlertLevel.NORMAL,
    val deviceConnected: Boolean = false,
    val deviceAddress: String = "—",
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val useMockData: Boolean = true
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bleManager: BleManager,
    private val saveReadingUseCase: SaveReadingUseCase,
    private val observeLatestReading: ObserveLatestReadingUseCase,
    private val observeReadings: ObserveReadingsUseCase,
    private val observeDailyStats: ObserveDailyStatsUseCase,
    private val notificationManager: HealthNotificationManager,
    private val workManager: WorkManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val recentReadings: StateFlow<List<HealthReading>> = observeReadings(50)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dailyStats: StateFlow<List<DailyStats>> = observeDailyStats(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private var bleJob: Job? = null

    init {
        startBleCollection(useMock = true)
        scheduleBackgroundSync()
        collectPersistedLatest()
    }

    // ── BLE collection ────────────────────────────────────────────────────────

    fun startBleCollection(useMock: Boolean = true) {
        bleJob?.cancel()
        _uiState.update { it.copy(useMockData = useMock, isLoading = true, deviceConnected = false) }

        bleJob = viewModelScope.launch {
            bleManager.observeHealthData(useMock = useMock)
                .catch { e ->
                    _uiState.update { it.copy(errorMessage = e.message, isLoading = false) }
                }
                .collect { data ->
                    handleBleData(data)
                }
        }
    }

    private suspend fun handleBleData(data: BleHealthData) {
        val reading = HealthReading(
            id           = UUID.randomUUID().toString(),
            heartRate    = data.heartRate,
            steps        = data.steps,
            oxygenLevel  = data.oxygenLevel,
            timestamp    = System.currentTimeMillis(),
            isSynced     = false
        )

        _uiState.update {
            it.copy(
                heartRate       = data.heartRate,
                steps           = data.steps,
                oxygenLevel     = data.oxygenLevel,
                alertLevel      = reading.alertLevel,
                deviceConnected = true,
                deviceAddress   = data.deviceAddress,
                isLoading       = false,
                errorMessage    = null
            )
        }

        // Persist to Room (alert creation is inside repository)
        saveReadingUseCase(reading)

        // Push local notification for abnormal values
        if (reading.alertLevel != AlertLevel.NORMAL) {
            // AlertsViewModel will handle building the HealthAlert; here we just push the UI
        }
    }

    // ── Sync persisted latest reading on startup ───────────────────────────────

    private fun collectPersistedLatest() {
        viewModelScope.launch {
            observeLatestReading().collect { reading ->
                reading ?: return@collect
                if (!_uiState.value.deviceConnected) {
                    _uiState.update {
                        it.copy(
                            heartRate    = reading.heartRate,
                            steps        = reading.steps,
                            oxygenLevel  = reading.oxygenLevel,
                            alertLevel   = reading.alertLevel,
                            isLoading    = false
                        )
                    }
                }
            }
        }
    }

    // ── WorkManager sync ──────────────────────────────────────────────────────

    private fun scheduleBackgroundSync() {
        HealthSyncWorker.schedule(workManager)
    }

    fun toggleDataSource() {
        startBleCollection(useMock = !_uiState.value.useMockData)
    }

    fun dismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        bleJob?.cancel()
    }
}
