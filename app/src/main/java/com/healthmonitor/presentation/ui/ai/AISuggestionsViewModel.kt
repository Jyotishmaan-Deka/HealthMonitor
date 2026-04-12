package com.healthmonitor.presentation.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.healthmonitor.domain.model.AISuggestion
import com.healthmonitor.domain.usecase.GetAISuggestionsUseCase
import com.healthmonitor.domain.usecase.ObserveDailyStatsUseCase
import com.healthmonitor.domain.usecase.ObserveLatestReadingUseCase
import com.healthmonitor.domain.usecase.ObserveReadingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AISuggestionsViewModel @Inject constructor(
    observeLatest: ObserveLatestReadingUseCase,
    observeReadings: ObserveReadingsUseCase,
    observeStats: ObserveDailyStatsUseCase,
    private val getAISuggestions: GetAISuggestionsUseCase
) : ViewModel() {

    val suggestions: StateFlow<List<AISuggestion>> = combine(
        observeLatest(),
        observeReadings(50),
        observeStats(7)
    ) { latest, readings, stats ->
        getAISuggestions(latest, readings, stats)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )
}
