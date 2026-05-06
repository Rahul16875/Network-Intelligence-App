package com.example.networkintelligence.data.engine.detectors

import com.example.networkintelligence.data.aggregation.SampleAggregator
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.InsightType
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.model.TimeWindow

class TimeOfDayDetector {

    fun detect(samples: List<NetworkSample>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val byLocation = SampleAggregator.groupByLocation(samples)

        byLocation.forEach { (location, locationSamples) ->
            val morning = SampleAggregator.morningHours(locationSamples)
            val evening = SampleAggregator.eveningHours(locationSamples)

            if (morning.size < MIN_SAMPLES || evening.size < MIN_SAMPLES) return@forEach

            val morningAvg = SampleAggregator.avgScore(morning)
            val eveningAvg = SampleAggregator.avgScore(evening)
            val diff = morningAvg - eveningAvg

            if (Math.abs(diff) < MIN_DIFF_PTS) return@forEach

            val worseWindow: String
            val betterWindow: String
            val timeWindow: TimeWindow

            if (diff > 0) {
                worseWindow = "evenings (6–11 PM)"
                betterWindow = "mornings (6–11 AM)"
                timeWindow = TimeWindow(18, 23)
            } else {
                worseWindow = "mornings (6–11 AM)"
                betterWindow = "evenings (6–11 PM)"
                timeWindow = TimeWindow(6, 11)
            }

            val confidence = (Math.abs(diff) / 100f).coerceIn(0.1f, 0.95f)
            val evidenceCount = morning.size + evening.size

            insights += Insight(
                id = "tod_${location}",
                type = InsightType.TIME_OF_DAY_PATTERN,
                title = "Network weaker $worseWindow",
                description = "Your network scores ${Math.abs(diff).toInt()} pts lower $worseWindow vs $betterWindow at this location.",
                confidence = confidence,
                locationHash = location,
                timeWindow = timeWindow,
                generatedAt = System.currentTimeMillis(),
                evidenceSampleCount = evidenceCount,
                diagnosisCause = DiagnosisCause.CONGESTION,
            )
        }

        return insights
    }

    companion object {
        private const val MIN_SAMPLES = 5
        private const val MIN_DIFF_PTS = 15f
    }
}
