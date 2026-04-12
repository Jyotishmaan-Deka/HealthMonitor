package com.healthmonitor.presentation.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthmonitor.presentation.theme.HeartRed
import com.healthmonitor.presentation.theme.OxygenBlue
import kotlin.math.roundToInt

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onSignOut: () -> Unit
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        // ── Avatar ────────────────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(60.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        Icons.Default.AccountCircle, null,
                        modifier = Modifier.padding(10.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Column {
                    Text("Health User", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(
                        if (prefs.useMockBle) "Using simulated data" else "Using real BLE sensor",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }

        // ── Notification & sync toggles ───────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column {
                SettingToggle(
                    icon = Icons.Default.Notifications,
                    label = "Health alerts",
                    description = "Notify for abnormal readings",
                    checked = prefs.notificationsEnabled,
                    onCheckedChange = viewModel::setNotificationsEnabled
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingToggle(
                    icon = Icons.Default.Sync,
                    label = "Auto sync",
                    description = "Upload to Firebase when online",
                    checked = prefs.autoSyncEnabled,
                    onCheckedChange = viewModel::setAutoSyncEnabled
                )
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                SettingToggle(
                    icon = Icons.Default.Bluetooth,
                    label = "Simulate BLE data",
                    description = "Use realistic mock sensor readings",
                    checked = prefs.useMockBle,
                    onCheckedChange = viewModel::setUseMockBle
                )
            }
        }

        // ── Alert thresholds ──────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Alert thresholds", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                ThresholdSlider(
                    icon = Icons.Default.Favorite,
                    label = "High heart rate alert",
                    value = prefs.heartRateAlertHigh.toFloat(),
                    valueRange = 100f..180f,
                    unit = "bpm",
                    color = HeartRed,
                    onValueChange = { viewModel.setHeartRateAlertHigh(it.roundToInt()) }
                )

                ThresholdSlider(
                    icon = Icons.Default.Favorite,
                    label = "Low heart rate alert",
                    value = prefs.heartRateAlertLow.toFloat(),
                    valueRange = 30f..70f,
                    unit = "bpm",
                    color = HeartRed,
                    onValueChange = { viewModel.setHeartRateAlertLow(it.roundToInt()) }
                )

                ThresholdSlider(
                    icon = Icons.Default.WaterDrop,
                    label = "Low SpO₂ alert",
                    value = prefs.oxygenAlertLow,
                    valueRange = 85f..98f,
                    unit = "%",
                    steps = 12,
                    color = OxygenBlue,
                    onValueChange = { viewModel.setOxygenAlertLow(it) }
                )
            }
        }

        // ── Info ──────────────────────────────────────────────────────────────
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.PrivacyTip, null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(22.dp))
                Text("Privacy policy", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Icon(Icons.Default.ChevronRight, null,
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
            }
        }

        Spacer(Modifier.weight(1f))

        // ── Sign out ──────────────────────────────────────────────────────────
        Button(
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HeartRed)
        ) {
            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(20.dp))
            Text("  Sign out", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ── Reusable toggle row ───────────────────────────────────────────────────────

@Composable
private fun SettingToggle(
    icon: ImageVector,
    label: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            Text(description, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

// ── Threshold slider ──────────────────────────────────────────────────────────

@Composable
private fun ThresholdSlider(
    icon: ImageVector,
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    unit: String,
    color: androidx.compose.ui.graphics.Color,
    steps: Int = 0,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                Text(label, style = MaterialTheme.typography.bodyMedium)
            }
            Text(
                text = if (unit == "%") "${String.format("%.0f", value)}%" else "${value.roundToInt()} $unit",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
