package com.example.networkintelligence.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    fun startPeriodicMonitoring() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<MonitorWorker>(
            PERIODIC_INTERVAL_MINUTES,
            TimeUnit.MINUTES,
            PERIODIC_FLEX_MINUTES,
            TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .addTag(MonitorWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MonitorWorker.UNIQUE_PERIODIC_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    fun stopPeriodicMonitoring() {
        WorkManager.getInstance(context).cancelUniqueWork(MonitorWorker.UNIQUE_PERIODIC_NAME)
    }

    fun captureOnce() {
        val request = OneTimeWorkRequestBuilder<MonitorWorker>()
            .addTag(MonitorWorker.TAG)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            MonitorWorker.UNIQUE_ONE_OFF_NAME,
            ExistingWorkPolicy.REPLACE,
            request,
        )
    }

    private companion object {
        const val PERIODIC_INTERVAL_MINUTES: Long = 15L
        const val PERIODIC_FLEX_MINUTES: Long = 5L
    }
}
