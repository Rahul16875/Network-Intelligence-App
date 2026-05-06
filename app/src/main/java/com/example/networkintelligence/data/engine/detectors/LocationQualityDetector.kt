package com.example.networkintelligence.data.engine.detectors

import com.example.networkintelligence.data.aggregation.SampleAggregator
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.InsightType
import com.example.networkintelligence.domain.model.NetworkSample

class LocationQualityDetector {

    fun detect(samples: List<NetworkSample>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val byLocation = SampleAggregator.groupByLocation(samples)

        byLocation.forEach { (location, locationSamples) ->
            if (locationSamples.size < MIN_SAMPLES) return@forEach

            val avg = SampleAggregator.avgScore(locationSamples)
            if (avg >= MAX_POOR_SCORE) return@forEach

            val confidence = ((MAX_POOR_SCORE - avg) / MAX_POOR_SCORE).coerceIn(0.1f, 0.95f)

            insights += Insight(
                id = "locqual_$location",
                type = InsightType.LOCATION_QUALITY,
                title = "Consistently poor network here",
                description = "Average score is ${avg.toInt()}/100 at this location across ${locationSamples.size} samples. Coverage may be limited.",
                confidence = confidence,
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                evidenceSampleCount = locationSamples.size,
                diagnosisCause = DiagnosisCause.MARGINAL,
            )
        }

        return insights
    }

    companion object {
        private const val MIN_SAMPLES = 20
        private const val MAX_POOR_SCORE = 50f
    }
}
