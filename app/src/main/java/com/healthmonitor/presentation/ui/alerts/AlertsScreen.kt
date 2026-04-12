package com.healthmonitor.presentation.ui.alerts

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.healthmonitor.domain.model.AlertType
import com.healthmonitor.domain.model.HealthAlert
import com.healthmonitor.presentation.theme.AlertOrange
import com.healthmonitor.presentation.theme.CriticalRed
import com.healthmonitor.presentation.theme.HeartRed
import com.healthmonitor.presentation.theme.OxygenBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AlertsScreen(viewModel: AlertsViewModel = hiltViewModel()) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Alerts", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            if (alerts.isNotEmpty()) {
                TextButton(onClick = viewModel::clearAll) {
                    Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Clear all")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        if (alerts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.NotificationsNone,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "No alerts",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        "All vitals are within normal range",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(alerts, key = { it.id }) { alert ->
                    AlertCard(alert = alert, onMarkRead = { viewModel.markRead(alert.id) })
                }
            }
        }
    }
}

@Composable
private fun AlertCard(alert: HealthAlert, onMarkRead: () -> Unit) {
    val (icon, tint) = alertVisuals(alert.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (alert.isRead)
                MaterialTheme.colorScheme.surfaceVariant
            else
                tint.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(28.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        alert.type.displayName(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (alert.isRead) FontWeight.Normal else FontWeight.SemiBold,
                        color = tint
                    )
                    Text(
                        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(alert.timestamp)),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    alert.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(
                        alpha = if (alert.isRead) 0.5f else 0.85f
                    )
                )
            }
            if (!alert.isRead) {
                FilledTonalIconButton(onClick = onMarkRead, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

private fun alertVisuals(type: AlertType): Pair<ImageVector, Color> = when (type) {
    AlertType.HIGH_HEART_RATE  -> Icons.Default.Favorite to CriticalRed
    AlertType.LOW_HEART_RATE   -> Icons.Default.Favorite to AlertOrange
    AlertType.CRITICAL_OXYGEN  -> Icons.Default.Warning  to CriticalRed
    AlertType.LOW_OXYGEN       -> Icons.Default.WaterDrop to OxygenBlue
    AlertType.INACTIVITY       -> Icons.Default.Warning  to AlertOrange
}

private fun AlertType.displayName() = when (this) {
    AlertType.HIGH_HEART_RATE  -> "High heart rate"
    AlertType.LOW_HEART_RATE   -> "Low heart rate"
    AlertType.CRITICAL_OXYGEN  -> "Critical oxygen level"
    AlertType.LOW_OXYGEN       -> "Low SpO₂"
    AlertType.INACTIVITY       -> "Inactivity alert"
}
