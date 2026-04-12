package com.healthmonitor.presentation.ui.ble

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthmonitor.presentation.theme.OxygenBlue
import com.healthmonitor.presentation.theme.StepsGreen

@Composable
fun BleScannerScreen(
    viewModel: BleScannerViewModel = hiltViewModel(),
    onDeviceSelected: (String) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // ── Header ──────────────────────────────────────────────────────────
        Row(verticalAlignment = Alignment.CenterVertically) {
            ScannerPulseIcon(isScanning = state.isScanning)
            Spacer(Modifier.width(12.dp))
            Column {
                Text("BLE Device Scanner", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (state.isScanning) "Scanning for nearby devices…"
                    else if (state.devices.isEmpty()) "Tap scan to find devices"
                    else "${state.devices.size} device${if (state.devices.size != 1) "s" else ""} found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (state.isScanning) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)))
            Spacer(Modifier.height(16.dp))
        }

        // ── Scan / Stop button ───────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (!state.isScanning) {
                Button(onClick = viewModel::startScan, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.BluetoothSearching, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Scan for devices")
                }
            } else {
                OutlinedButton(onClick = viewModel::stopScan, modifier = Modifier.weight(1f)) {
                    Text("Stop scan")
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // ── Permission denied banner ─────────────────────────────────────────
        if (state.permissionDenied) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Bluetooth permissions are required. Grant them in Settings → App permissions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(14.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        // ── Device list ──────────────────────────────────────────────────────
        if (state.devices.isEmpty() && !state.isScanning) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Bluetooth,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No devices found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
                    )
                    Text(
                        "Make sure your device is nearby and powered on",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                }
            }
        } else {
            // Section: health devices first
            val healthDevices = state.devices.filter { it.isHealthDevice }
            val otherDevices  = state.devices.filter { !it.isHealthDevice }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (healthDevices.isNotEmpty()) {
                    item {
                        Text(
                            "Health devices",
                            style = MaterialTheme.typography.labelLarge,
                            color = StepsGreen,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(healthDevices, key = { it.address }) { device ->
                        DeviceCard(
                            device = device,
                            isSelected = device.address == state.selectedAddress,
                            onClick = {
                                viewModel.selectDevice(device.address)
                                onDeviceSelected(device.address)
                            }
                        )
                    }
                }

                if (otherDevices.isNotEmpty()) {
                    item {
                        Text(
                            "Other devices",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                    items(otherDevices, key = { it.address }) { device ->
                        DeviceCard(
                            device = device,
                            isSelected = device.address == state.selectedAddress,
                            onClick = {
                                viewModel.selectDevice(device.address)
                                onDeviceSelected(device.address)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    device: ScannedDevice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) StepsGreen else Color.Transparent
    val signalColor = when {
        device.rssi > -60 -> StepsGreen
        device.rssi > -75 -> Color(0xFFFFA726)
        else              -> Color(0xFFEF5350)
    }
    val signalLabel = when {
        device.rssi > -60 -> "Strong"
        device.rssi > -75 -> "Fair"
        else              -> "Weak"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                StepsGreen.copy(alpha = 0.07f)
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Device type icon
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(10.dp))
                    .background(
                        if (device.isHealthDevice) StepsGreen.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (device.isHealthDevice) Icons.Default.Favorite else Icons.Default.DeviceUnknown,
                    contentDescription = null,
                    tint = if (device.isHealthDevice) StepsGreen else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.SignalCellularAlt, null, tint = signalColor, modifier = Modifier.size(18.dp))
                Text(
                    signalLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = signalColor
                )
                Text(
                    "${device.rssi} dBm",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = StepsGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun ScannerPulseIcon(isScanning: Boolean) {
    val transition = rememberInfiniteTransition(label = "scan_pulse")
    val scale by transition.animateFloat(
        initialValue = 1f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "scale"
    )
    val alpha by transition.animateFloat(
        initialValue = 1f, targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(48.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(OxygenBlue.copy(alpha = 0.2f))
            )
        }
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(OxygenBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Bluetooth,
                contentDescription = null,
                tint = OxygenBlue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
