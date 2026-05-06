package com.example.networkintelligence.data.repository

import android.util.Log
import com.example.networkintelligence.data.aggregation.AggregatedStatsComputer
import com.example.networkintelligence.util.APP_TAG
import com.example.networkintelligence.domain.engine.RecommendationEngine
import com.example.networkintelligence.domain.model.Recommendation
import com.example.networkintelligence.domain.repository.RecommendationRepository
import com.example.networkintelligence.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RecommendationRepositoryImpl @Inject constructor(
    private val sampleRepository: SampleRepository,
    private val statsComputer: AggregatedStatsComputer,
    private val recommendationEngine: RecommendationEngine,
) : RecommendationRepository {

    override fun observeMeasuredRecommendations(): Flow<List<Recommendation>> =
        sampleRepository.observeRecent(limit = 1000)
            .distinctUntilChanged()
            .conflate()
            .map { samples ->
                Log.d(TAG, "observeMeasured: recomputing from ${samples.size} samples")
                val stats = statsComputer.compute(samples)
                Log.d(TAG, "observeMeasured: computed ${stats.size} aggregated stats groups")
                stats.forEach { s ->
                    Log.d(TAG, "  stat: location=${s.locationHash} provider=${s.providerName} type=${s.networkType} avgScore=${s.avgScore.toInt()} n=${s.sampleCount}")
                }
                val recs = recommendationEngine.recommend(stats)
                Log.i(TAG, "observeMeasured: generated ${recs.size} measured recommendations")
                recs.forEach { r ->
                    Log.d(TAG, "  rec [${r.category}]: ${r.message}")
                }
                recs
            }

    companion object {
        private const val TAG = APP_TAG
    }
}
