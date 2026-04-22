package com.example.networkintelligence.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.networkintelligence.domain.repository.MonitorRepository
import com.example.networkintelligence.domain.repository.SampleRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class MonitorWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val monitorRepository: MonitorRepository,
    private val sampleRepository: SampleRepository,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = try {
        val sample = monitorRepository.captureNow()
        sampleRepository.insert(sample)
        Result.success()
    } catch (t: Throwable) {
        Log.w(TAG, "Capture failed; will retry", t)
        Result.retry()
    }

    companion object {
        const val TAG = "MonitorWorker"
        const val UNIQUE_PERIODIC_NAME = "monitor_worker_periodic"
        const val UNIQUE_ONE_OFF_NAME = "monitor_worker_one_off"
    }
}
