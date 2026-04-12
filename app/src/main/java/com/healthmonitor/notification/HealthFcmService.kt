package com.healthmonitor.notification

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HealthFcmService : FirebaseMessagingService() {

    @Inject lateinit var notificationManager: HealthNotificationManager

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(TAG, "FCM received from: ${message.from}")

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "Health Alert"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: "Check your health metrics"

        notificationManager.showRemoteAlert(title, body)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New FCM token: $token")
        // TODO: Send token to your backend / Firestore user doc
        // viewModel.updateFcmToken(token)
    }

    companion object {
        private const val TAG = "HealthFcmService"
    }
}
