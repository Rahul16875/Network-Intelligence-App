package com.example.networkintelligence.data.engine.detectors

import com.example.networkintelligence.data.aggregation.SampleAggregator
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.InsightType
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.model.TimeWindow

class LatencySpikeDetector {

    fun detect(samples: List<NetworkSample>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val byLocation = SampleAggregator.groupByLocation(samples)

        byLocation.forEach { (location, locationSamples) ->
            val withLatency = locationSamples.filter { it.latencyMs != null }
            if (withLatency.size < MIN_SAMPLES) return@forEach

            val overallAvg = SampleAggregator.avgLatency(withLatency) ?: return@forEach
            val byHour = SampleAggregator.groupByHourOfDay(withLatency)

            val spikeHours = byHour.filter { (_, hourSamples) ->
                hourSamples.size >= MIN_HOUR_SAMPLES &&
                    (SampleAggregator.avgLatency(hourSamples) ?: 0f) > overallAvg * SPIKE_MULTIPLIER
            }.keys.sorted()

            if (spikeHours.isEmpty()) return@forEach

            val confidence = (spikeHours.size / 6f).coerceIn(0.1f, 0.9f)
            val hoursDesc = spikeHours.joinToString(", ") { "${it}:00" }
            val evidenceCount = spikeHours.sumOf { h -> byHour[h]?.size ?: 0 }

            insights += Insight(
                id = "latspike_$location",
                type = InsightType.LATENCY_SPIKE,
                title = "High latency during certain hours",
                description = "Latency spikes >2× average at $hoursDesc at this location.",
                confidence = confidence,
                locationHash = location,
                timeWindow = TimeWindow(spikeHours.first(), spikeHours.last()),
                generatedAt = System.currentTimeMillis(),
                evidenceSampleCount = evidenceCount,
                diagnosisCause = DiagnosisCause.CONGESTION,
            )
        }

        return insights
    }

    companion object {
        private const val MIN_SAMPLES = 10
        private const val MIN_HOUR_SAMPLES = 3
        private const val SPIKE_MULTIPLIER = 2.0f
    }
}
