package com.example.networkintelligence.presentation.dashboard

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkintelligence.data.monitor.LatencyProbe
import com.example.networkintelligence.data.monitor.NetworkMonitor
import com.example.networkintelligence.data.speedtest.SpeedTestClient
import com.example.networkintelligence.domain.engine.DiagnosisEngine
import com.example.networkintelligence.domain.engine.ScoreEngine
import com.example.networkintelligence.domain.model.NetworkMeasurement
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.util.APP_TAG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MeasureState {
    data object Idle : MeasureState()
    data object Measuring : MeasureState()
    data class Done(val measurement: NetworkMeasurement) : MeasureState()
    data class Error(val message: String) : MeasureState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val latencyProbe: LatencyProbe,
    private val speedTestClient: SpeedTestClient,
    private val scoreEngine: ScoreEngine,
    private val diagnosisEngine: DiagnosisEngine,
) : ViewModel() {

    private val _state = MutableStateFlow<MeasureState>(MeasureState.Idle)
    val state: StateFlow<MeasureState> = _state.asStateFlow()

    fun measure() {
        if (_state.value is MeasureState.Measuring) return
        viewModelScope.launch {
            _state.value = MeasureState.Measuring
            try {
                Log.i(APP_TAG, "DashboardVM: starting measurement")

                val snapshot = networkMonitor.captureSnapshot()
                Log.i(APP_TAG, "DashboardVM: network=${snapshot.networkType} provider=${snapshot.providerName} signal=${snapshot.signalStrengthDbm}dBm")

                val probeResult = latencyProbe.probe()
                Log.i(APP_TAG, "DashboardVM: latency=${probeResult.avgLatencyMs}ms failureRate=${probeResult.failureRate}")

                val speedResult = speedTestClient.measure()
                Log.i(APP_TAG, "DashboardVM: download=${speedResult.downloadMbps}Mbps upload=${speedResult.uploadMbps}Mbps")

                val sample = NetworkSample(
                    timestamp = System.currentTimeMillis(),
                    networkType = snapshot.networkType,
                    providerName = snapshot.providerName,
                    signalStrengthDbm = snapshot.signalStrengthDbm,
                    latencyMs = probeResult.avgLatencyMs,
                    probeFailureRate = probeResult.failureRate,
                    score = 0,
                )
                val score = scoreEngine.calculate(sample)
                val scoredSample = sample.copy(score = score)
                val diagnosis = diagnosisEngine.diagnose(scoredSample)

                Log.i(APP_TAG, "DashboardVM: score=$score diagnosis=${diagnosis.cause}")

                _state.value = MeasureState.Done(
                    NetworkMeasurement(
                        timestamp = scoredSample.timestamp,
                        networkType = scoredSample.networkType,
                        providerName = scoredSample.providerName,
                        signalStrengthDbm = scoredSample.signalStrengthDbm,
                        latencyMs = scoredSample.latencyMs,
                        downloadMbps = speedResult.downloadMbps,
                        uploadMbps = speedResult.uploadMbps,
                        score = score,
                        diagnosis = diagnosis,
                    )
                )
            } catch (t: Throwable) {
                Log.e(APP_TAG, "DashboardVM: measurement failed: ${t.message}")
                _state.value = MeasureState.Error(t.message ?: "Unknown error")
            }
        }
    }
}
