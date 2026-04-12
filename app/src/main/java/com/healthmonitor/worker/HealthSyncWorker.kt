package com.healthmonitor.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.healthmonitor.domain.repository.HealthRepository
import com.healthmonitor.notification.HealthNotificationManager
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class HealthSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: HealthRepository,
    private val notificationManager: HealthNotificationManager
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        Log.d(TAG, "HealthSyncWorker started")

        return try {
            val result = repository.syncPendingReadings()
            result.fold(
                onSuccess = { synced ->
                    Log.d(TAG, "Synced $synced readings")
                    if (synced > 0) {
                        notificationManager.showSyncNotification(synced)
                    }
                    Result.success()
                },
                onFailure = { error ->
                    Log.e(TAG, "Sync failed: ${error.message}")
                    if (runAttemptCount < 3) Result.retry() else Result.failure()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Worker crashed: ${e.message}")
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "HealthSyncWorker"
        private const val WORK_NAME = "health_periodic_sync"

        fun schedule(workManager: WorkManager) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val request = PeriodicWorkRequestBuilder<HealthSyncWorker>(
                repeatInterval = 15,
                repeatIntervalTimeUnit = TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    10,
                    TimeUnit.MINUTES
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(workManager: WorkManager) {
            workManager.cancelUniqueWork(WORK_NAME)
        }
    }
}
