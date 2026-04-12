package com.healthmonitor.presentation.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthmonitor.domain.model.HealthAlert
import com.healthmonitor.domain.usecase.ObserveAlertsUseCase
import com.healthmonitor.domain.repository.HealthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AlertsViewModel @Inject constructor(
    private val observeAlerts: ObserveAlertsUseCase,
    private val repository: HealthRepository
) : ViewModel() {

    val alerts: StateFlow<List<HealthAlert>> = observeAlerts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val unreadCount: StateFlow<Int> = observeAlerts.unreadCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun markRead(alertId: String) {
        viewModelScope.launch { repository.markAlertRead(alertId) }
    }

    fun clearAll() {
        viewModelScope.launch { repository.clearAllAlerts() }
    }
}
