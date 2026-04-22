package com.example.networkintelligence.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkintelligence.domain.engine.DiagnosisEngine
import com.example.networkintelligence.domain.model.Diagnosis
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.repository.SampleRepository
import com.example.networkintelligence.worker.WorkScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class DashboardUiState(
    val latest: NetworkSample? = null,
    val totalSamples: Int = 0,
    val monitoring: Boolean = false,
    val diagnosis: Diagnosis? = null,
    val scoreDeltaVsOneHourAgo: Int? = null,
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    sampleRepository: SampleRepository,
    private val diagnosisEngine: DiagnosisEngine,
    private val workScheduler: WorkScheduler,
) : ViewModel() {

    private val _monitoring = MutableStateFlow(false)

    val uiState: StateFlow<DashboardUiState> =
        combine(
            sampleRepository.observeRecent(limit = RECENT_LIMIT),
            sampleRepository.observeCount(),
            _monitoring.asStateFlow(),
        ) { recent, count, monitoring ->
            val latest = recent.firstOrNull()
            DashboardUiState(
                latest = latest,
                totalSamples = count,
                monitoring = monitoring,
                diagnosis = latest?.let { diagnosisEngine.diagnose(it, recent) },
                scoreDeltaVsOneHourAgo = latest?.let { scoreDeltaVsOneHourAgo(it, recent) },
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DashboardUiState(),
        )

    fun startMonitoring() {
        workScheduler.startPeriodicMonitoring()
        _monitoring.value = true
    }

    fun stopMonitoring() {
        workScheduler.stopPeriodicMonitoring()
        _monitoring.value = false
    }

    fun captureOnce() {
        workScheduler.captureOnce()
    }

    private fun scoreDeltaVsOneHourAgo(latest: NetworkSample, recent: List<NetworkSample>): Int? {
        val threshold = latest.timestamp - ONE_HOUR_MS
        val reference = recent.firstOrNull { it.timestamp <= threshold } ?: return null
        return latest.score - reference.score
    }

    private companion object {
        const val RECENT_LIMIT = 60
        const val ONE_HOUR_MS = 60L * 60L * 1000L
    }
}
