package com.healthmonitor.presentation.ui.history

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.healthmonitor.domain.model.DailyStats
import com.healthmonitor.domain.model.HealthReading
import com.healthmonitor.domain.usecase.ObserveDailyStatsUseCase
import com.healthmonitor.domain.usecase.ObserveReadingsUseCase
import com.healthmonitor.presentation.theme.HeartRed
import com.healthmonitor.presentation.theme.OxygenBlue
import com.healthmonitor.presentation.theme.StepsGreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeReadings: ObserveReadingsUseCase,
    observeDailyStats: ObserveDailyStatsUseCase
) : ViewModel() {

    val readings: StateFlow<List<HealthReading>> = observeReadings(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val dailyStats: StateFlow<List<DailyStats>> = observeDailyStats(7)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}

// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun HistoryScreen(viewModel: HistoryViewModel = hiltViewModel()) {
    val readings by viewModel.readings.collectAsStateWithLifecycle()
    val dailyStats by viewModel.dailyStats.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            "History",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Daily stats") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("All readings") })
        }

        when (selectedTab) {
            0 -> DailyStatsTab(dailyStats)
            1 -> ReadingsListTab(readings)
        }
    }
}

@Composable
private fun DailyStatsTab(stats: List<DailyStats>) {
    if (stats.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { WeeklyBarChart(stats = stats) }
        items(stats) { stat -> DailyStatCard(stat) }
    }
}

@Composable
private fun WeeklyBarChart(stats: List<DailyStats>) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Avg heart rate — last 7 days", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            val maxHr = (stats.maxOfOrNull { it.avgHeartRate } ?: 100) + 10

            Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                if (stats.isEmpty()) return@Canvas
                val barWidth = (size.width / (stats.size * 2f)).coerceAtMost(32f)
                val spacing = size.width / stats.size

                stats.reversed().forEachIndexed { i, stat ->
                    val barH = (stat.avgHeartRate.toFloat() / maxHr) * size.height
                    val x = i * spacing + spacing / 2f - barWidth / 2f
                    val y = size.height - barH

                    drawRoundRect(
                        color = HeartRed,
                        topLeft = Offset(x, y),
                        size = androidx.compose.ui.geometry.Size(barWidth, barH),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DailyStatCard(stat: DailyStats) {
    Card(shape = RoundedCornerShape(12.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date(stat.date)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("Avg HR", "${stat.avgHeartRate} bpm", HeartRed)
                StatItem("Max HR", "${stat.maxHeartRate} bpm", HeartRed.copy(alpha = 0.7f))
                StatItem("Steps", "%,d".format(stat.totalSteps), StepsGreen)
                StatItem("SpO₂ avg", "${String.format("%.1f", stat.avgOxygenLevel)}%", OxygenBlue)
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
    }
}

@Composable
private fun ReadingsListTab(readings: List<HealthReading>) {
    if (readings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No readings yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
        return
    }

    LazyColumn {
        items(readings, key = { it.id }) { reading ->
            ReadingRow(reading)
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun ReadingRow(reading: HealthReading) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            SimpleDateFormat("MMM d  HH:mm:ss", Locale.getDefault()).format(Date(reading.timestamp)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("${reading.heartRate} bpm", color = HeartRed, fontWeight = FontWeight.Medium)
            Text("${String.format("%.1f", reading.oxygenLevel)}%", color = OxygenBlue, fontWeight = FontWeight.Medium)
        }
    }
}
