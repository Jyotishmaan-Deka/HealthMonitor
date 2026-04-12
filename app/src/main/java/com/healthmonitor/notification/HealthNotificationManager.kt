package com.healthmonitor.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.healthmonitor.MainActivity
import com.healthmonitor.domain.model.AlertLevel
import com.healthmonitor.domain.model.AlertType
import com.healthmonitor.domain.model.HealthAlert
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HealthNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    // ── Channel setup ─────────────────────────────────────────────────────────

    private fun createChannels() {
        val channels = listOf(
            NotificationChannel(
                CHANNEL_ALERTS,
                "Health Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical health value notifications"
                enableVibration(true)
            },
            NotificationChannel(
                CHANNEL_SYNC,
                "Sync Status",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Background data sync updates"
            },
            NotificationChannel(
                CHANNEL_REMINDERS,
                "Health Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Activity and hydration reminders"
            }
        )

        val sysManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { sysManager.createNotificationChannel(it) }
    }

    // ── Tap-to-open intent ─────────────────────────────────────────────────────

    private fun mainPendingIntent(): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // ── Alert notification ────────────────────────────────────────────────────

    fun showAlertNotification(alert: HealthAlert) {
        val (title, icon, priority) = when (alert.type) {
            AlertType.HIGH_HEART_RATE -> Triple(
                "High Heart Rate ⚠️",
                android.R.drawable.ic_dialog_alert,
                NotificationCompat.PRIORITY_HIGH
            )
            AlertType.LOW_HEART_RATE -> Triple(
                "Low Heart Rate ⚠️",
                android.R.drawable.ic_dialog_alert,
                NotificationCompat.PRIORITY_HIGH
            )
            AlertType.CRITICAL_OXYGEN -> Triple(
                "Critical SpO₂ 🚨",
                android.R.drawable.ic_dialog_alert,
                NotificationCompat.PRIORITY_MAX
            )
            AlertType.LOW_OXYGEN -> Triple(
                "Low Oxygen Level ⚠️",
                android.R.drawable.ic_dialog_info,
                NotificationCompat.PRIORITY_HIGH
            )
            AlertType.INACTIVITY -> Triple(
                "Time to Move 🚶",
                android.R.drawable.ic_dialog_info,
                NotificationCompat.PRIORITY_DEFAULT
            )
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(alert.message)
            .setPriority(priority)
            .setContentIntent(mainPendingIntent())
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(alert.id.hashCode(), notification)
        } catch (e: SecurityException) {
            // POST_NOTIFICATIONS permission not granted
        }
    }

    // ── Sync notification ─────────────────────────────────────────────────────

    fun showSyncNotification(count: Int) {
        val notification = NotificationCompat.Builder(context, CHANNEL_SYNC)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("Health data synced")
            .setContentText("$count reading${if (count > 1) "s" else ""} uploaded successfully")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(NOTIF_ID_SYNC, notification)
        } catch (e: SecurityException) { /* permission not granted */ }
    }

    // ── FCM (from Firebase push) ──────────────────────────────────────────────

    fun showRemoteAlert(title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(mainPendingIntent())
            .setAutoCancel(true)
            .build()

        try {
            notificationManager.notify(NOTIF_ID_REMOTE, notification)
        } catch (e: SecurityException) { /* permission not granted */ }
    }

    companion object {
        const val CHANNEL_ALERTS = "health_alerts"
        const val CHANNEL_SYNC = "health_sync"
        const val CHANNEL_REMINDERS = "health_reminders"
        private const val NOTIF_ID_SYNC = 1001
        private const val NOTIF_ID_REMOTE = 1002
    }
}
