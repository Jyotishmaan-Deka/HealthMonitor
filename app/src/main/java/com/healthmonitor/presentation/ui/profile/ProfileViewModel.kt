package com.healthmonitor.presentation.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthmonitor.data.local.UserPreferences
import com.healthmonitor.data.local.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefsRepo: UserPreferencesRepository
) : ViewModel() {

    val prefs: StateFlow<UserPreferences> = prefsRepo.userPreferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserPreferences())

    fun setNotificationsEnabled(on: Boolean) {
        viewModelScope.launch { prefsRepo.setNotificationsEnabled(on) }
    }

    fun setAutoSyncEnabled(on: Boolean) {
        viewModelScope.launch { prefsRepo.setAutoSyncEnabled(on) }
    }

    fun setUseMockBle(mock: Boolean) {
        viewModelScope.launch { prefsRepo.setUseMockBle(mock) }
    }

    fun setHeartRateAlertHigh(value: Int) {
        viewModelScope.launch { prefsRepo.setHeartRateAlertHigh(value) }
    }

    fun setHeartRateAlertLow(value: Int) {
        viewModelScope.launch { prefsRepo.setHeartRateAlertLow(value) }
    }

    fun setOxygenAlertLow(value: Float) {
        viewModelScope.launch { prefsRepo.setOxygenAlertLow(value) }
    }
}
