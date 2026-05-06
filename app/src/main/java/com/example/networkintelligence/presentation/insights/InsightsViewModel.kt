package com.example.networkintelligence.presentation.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.repository.InsightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class InsightsUiState(
    val insights: List<Insight> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    insightRepository: InsightRepository,
) : ViewModel() {

    val uiState: StateFlow<InsightsUiState> =
        insightRepository.observeInsights()
            .map { InsightsUiState(insights = it, isLoading = false) }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = InsightsUiState(),
            )
}
