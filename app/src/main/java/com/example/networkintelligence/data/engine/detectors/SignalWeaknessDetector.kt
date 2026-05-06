package com.example.networkintelligence.data.engine.detectors

import com.example.networkintelligence.data.aggregation.SampleAggregator
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.InsightType
import com.example.networkintelligence.domain.model.NetworkSample

class SignalWeaknessDetector {

    fun detect(samples: List<NetworkSample>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val byLocation = SampleAggregator.groupByLocation(samples)

        byLocation.forEach { (location, locationSamples) ->
            if (locationSamples.size < MIN_SAMPLES) return@forEach

            val weakFraction = SampleAggregator.weakSignalFraction(locationSamples)
            if (weakFraction < WEAK_THRESHOLD) return@forEach

            val confidence = weakFraction.coerceIn(0.1f, 0.95f)
            val pct = (weakFraction * 100).toInt()

            insights += Insight(
                id = "sigweak_$location",
                type = InsightType.SIGNAL_WEAKNESS,
                title = "Weak signal zone",
                description = "$pct% of samples at this location show weak signal strength.",
                confidence = confidence,
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                evidenceSampleCount = locationSamples.size,
                diagnosisCause = DiagnosisCause.WEAK_SIGNAL,
            )
        }

        return insights
    }

    companion object {
        private const val MIN_SAMPLES = 10
        private const val WEAK_THRESHOLD = 0.7f
    }
}
