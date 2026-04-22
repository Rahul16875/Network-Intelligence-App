package com.example.networkintelligence.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.repository.SampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class HistoryRange(val label: String, val durationMs: Long) {
    LAST_24H("24h", TimeUnit.HOURS.toMillis(24)),
    LAST_7D("7d", TimeUnit.DAYS.toMillis(7)),
    LAST_30D("30d", TimeUnit.DAYS.toMillis(30)),
}

data class HistoryUiState(
    val range: HistoryRange = HistoryRange.LAST_24H,
    val samples: List<NetworkSample> = emptyList(),
    val averageScore: Int? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val sampleRepository: SampleRepository,
) : ViewModel() {

    private val _range = MutableStateFlow(HistoryRange.LAST_24H)

    val uiState: StateFlow<HistoryUiState> =
        _range
            .asStateFlow()
            .flatMapLatest { range ->
                val now = System.currentTimeMillis()
                kotlinx.coroutines.flow.combine(
                    kotlinx.coroutines.flow.flowOf(range),
                    sampleRepository.observeByRange(
                        fromTimestamp = now - range.durationMs,
                        toTimestamp = now,
                    ),
                ) { r, samples ->
                    HistoryUiState(
                        range = r,
                        samples = samples,
                        averageScore = samples.takeIf { it.isNotEmpty() }?.map { it.score }?.average()?.toInt(),
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = HistoryUiState(),
            )

    fun selectRange(range: HistoryRange) {
        _range.value = range
    }
}
