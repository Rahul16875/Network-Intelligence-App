package com.example.networkintelligence.data.engine.detectors

import com.example.networkintelligence.data.aggregation.SampleAggregator
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.InsightType
import com.example.networkintelligence.domain.model.NetworkSample

class ProviderComparisonDetector {

    fun detect(samples: List<NetworkSample>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val byLocation = SampleAggregator.groupByLocation(samples)

        byLocation.forEach { (location, locationSamples) ->
            val byProvider = SampleAggregator.groupByProvider(locationSamples)
                .filter { (_, s) -> s.size >= MIN_SAMPLES }

            if (byProvider.size < 2) return@forEach

            val providerAvgs = byProvider.mapValues { (_, s) -> SampleAggregator.avgScore(s) }
            val best = providerAvgs.maxByOrNull { it.value } ?: return@forEach
            val worst = providerAvgs.minByOrNull { it.value } ?: return@forEach

            if (best.key == worst.key) return@forEach

            val diff = best.value - worst.value
            if (diff < MIN_DIFF_PTS) return@forEach

            val confidence = (diff / 100f).coerceIn(0.1f, 0.95f)
            val totalEvidence = byProvider.values.sumOf { it.size }

            insights += Insight(
                id = "provider_$location",
                type = InsightType.PROVIDER_COMPARISON,
                title = "${best.key} outperforms ${worst.key} here",
                description = "${best.key} averages ${best.value.toInt()}/100 vs ${worst.value.toInt()}/100 for ${worst.key} at this location.",
                confidence = confidence,
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                evidenceSampleCount = totalEvidence,
                diagnosisCause = DiagnosisCause.MARGINAL,
            )
        }

        return insights
    }

    companion object {
        private const val MIN_SAMPLES = 10
        private const val MIN_DIFF_PTS = 10f
    }
}
