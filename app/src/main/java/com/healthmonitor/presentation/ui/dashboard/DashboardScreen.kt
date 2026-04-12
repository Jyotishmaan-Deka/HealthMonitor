package com.healthmonitor.presentation.ui.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthmonitor.domain.model.AlertLevel
import com.healthmonitor.domain.model.HealthReading
import com.healthmonitor.presentation.theme.AlertOrange
import com.healthmonitor.presentation.theme.CriticalRed
import com.healthmonitor.presentation.theme.HeartRed
import com.healthmonitor.presentation.theme.OxygenBlue
import com.healthmonitor.presentation.theme.StepsGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToHistory: () -> Unit = {},
    onNavigateToAI: () -> Unit = {},
    onNavigateToBleScanner: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val readings by viewModel.recentReadings.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Header ──────────────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Health Monitor",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = SimpleDateFormat("EEEE, MMM d", Locale.getDefault()).format(Date()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                DeviceStatusChip(
                    connected = state.deviceConnected,
                    address = state.deviceAddress,
                    onClick = onNavigateToBleScanner
                )
            }

            // ── Alert banner ──────────────────────────────────────────────────
            if (state.alertLevel != AlertLevel.NORMAL) {
                val alertColor = if (state.alertLevel == AlertLevel.CRITICAL) CriticalRed else AlertOrange
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(alertColor.copy(alpha = 0.12f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = alertColor, modifier = Modifier.size(20.dp))
                    Text(
                        text = if (state.alertLevel == AlertLevel.CRITICAL)
                            "Critical reading — check your metrics immediately"
                        else
                            "Abnormal reading detected — monitor closely",
                        style = MaterialTheme.typography.bodyMedium,
                        color = alertColor,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // ── Vitals ────────────────────────────────────────────────────────
            if (state.isLoading && state.heartRate == 0) {
                Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    HeartRateCard(state.heartRate, state.alertLevel, Modifier.weight(1f))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SmallVitalCard(
                            label = "SpO₂",
                            value = "${String.format("%.1f", state.oxygenLevel)}%",
                            icon = Icons.Default.WaterDrop,
                            iconTint = OxygenBlue,
                            isAlert = state.oxygenLevel in 1f..94f
                        )
                        SmallVitalCard(
                            label = "Steps",
                            value = "%,d".format(state.steps),
                            icon = Icons.Default.DirectionsWalk,
                            iconTint = StepsGreen
                        )
                    }
                }
            }

            // ── Trend chart ───────────────────────────────────────────────────
            HeartRateTrendCard(readings, onClick = onNavigateToHistory)

            // ── Quick actions ─────────────────────────────────────────────────
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(onClick = onNavigateToBleScanner, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.BluetoothSearching, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Find device")
                }
                Button(onClick = onNavigateToAI, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Psychology, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("AI insights")
                }
            }

            Spacer(Modifier.height(8.dp))
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ── Device chip (tappable → BLE scanner) ─────────────────────────────────────

@Composable
private fun DeviceStatusChip(connected: Boolean, address: String, onClick: () -> Unit) {
    val bgColor by animateColorAsState(
        if (connected) Color(0xFF43A047).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.15f),
        label = "chip_color"
    )
    androidx.compose.foundation.clickable(
        indication = null,
        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    ) { onClick() }.let { mod ->
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .then(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = if (connected) Icons.Default.Bluetooth else Icons.Default.BluetoothDisabled,
                contentDescription = null,
                tint = if (connected) Color(0xFF43A047) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = if (connected) address.take(9) else "Tap to scan",
                style = MaterialTheme.typography.labelMedium,
                color = if (connected) Color(0xFF2E7D32) else Color.Gray
            )
        }
    }
}

// ── Heart rate card with pulsing icon ────────────────────────────────────────

@Composable
private fun HeartRateCard(heartRate: Int, alertLevel: AlertLevel, modifier: Modifier) {
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        1f, 1.2f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "scale"
    )
    val hrColor = when (alertLevel) {
        AlertLevel.NORMAL   -> HeartRed
        AlertLevel.WARNING  -> AlertOrange
        AlertLevel.CRITICAL -> CriticalRed
    }

    Card(
        modifier = modifier.height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = hrColor.copy(alpha = 0.08f))
    ) {
        Column(
            Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Heart Rate", style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Icon(Icons.Default.Favorite, null, tint = hrColor,
                    modifier = Modifier.size(20.dp).scale(if (heartRate > 0) scale else 1f))
            }
            Column {
                Text(
                    text = if (heartRate > 0) "$heartRate" else "--",
                    fontSize = 48.sp, fontWeight = FontWeight.Bold, color = hrColor
                )
                Text("bpm", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

// ── Small vital card ──────────────────────────────────────────────────────────

@Composable
private fun SmallVitalCard(
    label: String, value: String, icon: ImageVector,
    iconTint: Color, isAlert: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth().height(72.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAlert) iconTint.copy(alpha = 0.08f)
                             else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            Modifier.fillMaxSize().padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(value, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAlert) iconTint else MaterialTheme.colorScheme.onSurface)
            }
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(24.dp))
        }
    }
}

// ── Mini sparkline chart ──────────────────────────────────────────────────────

@Composable
private fun HeartRateTrendCard(readings: List<HealthReading>, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Heart Rate Trend", style = MaterialTheme.typography.titleMedium)
                FilledTonalButton(onClick = onClick, modifier = Modifier.height(32.dp)) {
                    Text("Full history", style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(12.dp))
            if (readings.isEmpty()) {
                Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                    Text("Collecting data…", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
            } else {
                MiniSparkline(readings.takeLast(30))
            }
        }
    }
}

@Composable
private fun MiniSparkline(readings: List<HealthReading>) {
    val hrs = readings.map { it.heartRate }
    val minHr = (hrs.minOrNull() ?: 60) - 5
    val maxHr = (hrs.maxOrNull() ?: 100) + 5
    val range = (maxHr - minHr).coerceAtLeast(1).toFloat()

    androidx.compose.foundation.Canvas(Modifier.fillMaxWidth().height(80.dp)) {
        if (hrs.size < 2) return@Canvas
        val stepX = size.width / (hrs.size - 1).toFloat()
        val path = Path()

        hrs.forEachIndexed { i, hr ->
            val x = i * stepX
            val y = size.height - ((hr - minHr) / range) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(path, HeartRed, style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round))

        val lastX = (hrs.size - 1) * stepX
        val lastY = size.height - ((hrs.last() - minHr) / range) * size.height
        drawCircle(HeartRed, radius = 5f, center = Offset(lastX, lastY))
    }
}
