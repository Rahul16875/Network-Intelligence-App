package com.example.networkintelligence.presentation.recommendations

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.networkintelligence.util.APP_TAG
import androidx.lifecycle.viewModelScope
import com.example.networkintelligence.data.monitor.LocationProvider
import com.example.networkintelligence.data.repository.TowerCoverageRepository
import com.example.networkintelligence.domain.model.CarrierCoverage
import com.example.networkintelligence.domain.model.Recommendation
import com.example.networkintelligence.domain.model.RecommendationCategory
import com.example.networkintelligence.domain.model.SourceType
import com.example.networkintelligence.domain.repository.RecommendationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RecommendationsUiState(
    val measured: List<Recommendation> = emptyList(),
    val predicted: List<Recommendation> = emptyList(),
    val isMeasuredLoading: Boolean = true,
    val isPredictedLoading: Boolean = true,
    val hasPredictedData: Boolean = false,
)

@HiltViewModel
class RecommendationsViewModel @Inject constructor(
    private val recommendationRepository: RecommendationRepository,
    private val towerCoverageRepository: TowerCoverageRepository,
    private val locationProvider: LocationProvider,
) : ViewModel() {

    private val _predicted = MutableStateFlow<List<Recommendation>>(emptyList())
    private val _isPredictedLoading = MutableStateFlow(true)
    private val _hasPredictedData = MutableStateFlow(false)

    val measuredRecommendations: StateFlow<List<Recommendation>> =
        recommendationRepository.observeMeasuredRecommendations()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList(),
            )

    val predictedRecommendations: StateFlow<List<Recommendation>> = _predicted
    val isPredictedLoading: StateFlow<Boolean> = _isPredictedLoading
    val hasPredictedData: StateFlow<Boolean> = _hasPredictedData

    init {
        loadPredictedRecommendations()
    }

    fun refresh() {
        Log.d(TAG, "refresh: triggered by user")
        loadPredictedRecommendations()
    }

    private fun loadPredictedRecommendations() {
        viewModelScope.launch {
            Log.d(TAG, "loadPredicted: start")
            _isPredictedLoading.value = true

            val bucketKey = locationProvider.getCoarseBucket()
            if (bucketKey == null) {
                Log.w(TAG, "loadPredicted: location unavailable — permission denied or GPS off")
                _isPredictedLoading.value = false
                _hasPredictedData.value = false
                return@launch
            }

            Log.i(TAG, "loadPredicted: current bucket=$bucketKey")

            val coverage = towerCoverageRepository.fetchCoverage(bucketKey)
            Log.i(TAG, "loadPredicted: got ${coverage.size} carrier(s) from tower repository")

            val recs = buildPredictedRecommendations(coverage, bucketKey)
            Log.i(TAG, "loadPredicted: built ${recs.size} predicted recommendations")

            _predicted.value = recs
            _hasPredictedData.value = coverage.isNotEmpty()
            _isPredictedLoading.value = false
        }
    }

    private fun buildPredictedRecommendations(
        coverage: List<CarrierCoverage>,
        bucketKey: String,
    ): List<Recommendation> {
        if (coverage.isEmpty()) return emptyList()

        val recommendations = mutableListOf<Recommendation>()
        val best = coverage.maxByOrNull { coverageScore(it) }

        if (best != null) {
            val towersDesc = coverage.joinToString(", ") {
                "${it.carrierName}: ${it.towerCount} ${it.bestRadioLabel} tower${if (it.towerCount != 1) "s" else ""}"
            }
            recommendations += Recommendation(
                id = "predicted_best_$bucketKey",
                category = RecommendationCategory.PREDICTED_BEST_PROVIDER,
                message = "${best.carrierName} likely has the best coverage here",
                reason = "Tower data: $towersDesc.",
                expectedBenefit = "${best.towerCount} ${best.bestRadioLabel} towers in this area",
                locationHash = bucketKey,
                generatedAt = System.currentTimeMillis(),
                sourceType = SourceType.PREDICTED,
            )
        }

        coverage.forEach { carrier ->
            recommendations += Recommendation(
                id = "predicted_carrier_${bucketKey}_${carrier.mcc}_${carrier.mnc}",
                category = RecommendationCategory.PREDICTED_BEST_PROVIDER,
                message = "${carrier.carrierName}: ${carrier.towerCount} tower${if (carrier.towerCount != 1) "s" else ""} (${carrier.bestRadioLabel})",
                reason = buildString {
                    if (carrier.hasNr) append("5G ")
                    if (carrier.hasLte) append("LTE ")
                    if (carrier.hasUmts) append("3G ")
                    if (carrier.hasGsm) append("2G")
                    append("coverage present in this area.")
                }.trim(),
                expectedBenefit = null,
                locationHash = bucketKey,
                generatedAt = System.currentTimeMillis(),
                sourceType = SourceType.PREDICTED,
            )
        }

        return recommendations
    }

    private fun coverageScore(c: CarrierCoverage): Int {
        var score = c.towerCount * 10
        if (c.hasNr) score += 40
        if (c.hasLte) score += 20
        if (c.hasUmts) score += 5
        return score
    }

    companion object {
        private const val TAG = APP_TAG
    }
}
