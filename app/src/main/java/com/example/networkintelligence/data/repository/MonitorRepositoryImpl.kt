package com.example.networkintelligence.data.repository

import com.example.networkintelligence.data.monitor.LatencyProbe
import com.example.networkintelligence.data.monitor.LocationProvider
import com.example.networkintelligence.data.monitor.NetworkMonitor
import com.example.networkintelligence.domain.engine.ScoreEngine
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.model.NetworkType
import com.example.networkintelligence.domain.repository.MonitorRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MonitorRepositoryImpl @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val latencyProbe: LatencyProbe,
    private val locationProvider: LocationProvider,
    private val scoreEngine: ScoreEngine,
) : MonitorRepository {

    override suspend fun captureNow(): NetworkSample {
        val snapshot = networkMonitor.captureSnapshot()
        val probe = if (snapshot.networkType == NetworkType.NONE) {
            LatencyProbe.Result(avgLatencyMs = null, failureRate = 1f, successes = 0, attempts = 0)
        } else {
            latencyProbe.probe()
        }
        val locationHash = locationProvider.getCoarseBucket()

        val draft = NetworkSample(
            timestamp = System.currentTimeMillis(),
            networkType = snapshot.networkType,
            providerName = snapshot.providerName,
            locationHash = locationHash,
            signalStrengthDbm = snapshot.signalStrengthDbm,
            latencyMs = probe.avgLatencyMs,
            probeFailureRate = probe.failureRate,
            score = 0,
        )
        return draft.copy(score = scoreEngine.calculate(draft))
    }
}
