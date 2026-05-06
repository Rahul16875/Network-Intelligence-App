package com.example.networkintelligence.data.engine.detectors

import com.example.networkintelligence.data.aggregation.SampleAggregator
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.InsightType
import com.example.networkintelligence.domain.model.NetworkSample

class WifiVsCellularDetector {

    fun detect(samples: List<NetworkSample>): List<Insight> {
        val insights = mutableListOf<Insight>()
        val byLocation = SampleAggregator.groupByLocation(samples)

        byLocation.forEach { (location, locationSamples) ->
            val wifi = SampleAggregator.wifiSamples(locationSamples)
            val cellular = SampleAggregator.cellularSamples(locationSamples)

            if (wifi.size < MIN_SAMPLES || cellular.size < MIN_SAMPLES) return@forEach

            val wifiAvg = SampleAggregator.avgScore(wifi)
            val cellAvg = SampleAggregator.avgScore(cellular)
            val diff = wifiAvg - cellAvg

            if (Math.abs(diff) < MIN_DIFF_PTS) return@forEach

            val confidence = (Math.abs(diff) / 100f).coerceIn(0.1f, 0.95f)
            val (better, worse, betterAvg, worseAvg) = if (diff > 0) {
                listOf("Wi-Fi", "cellular", wifiAvg, cellAvg)
            } else {
                listOf("cellular", "Wi-Fi", cellAvg, wifiAvg)
            }

            insights += Insight(
                id = "wificell_$location",
                type = InsightType.CONNECTION_TYPE_PREFERENCE,
                title = "$better is better here",
                description = "$better averages ${(betterAvg as Float).toInt()}/100 vs ${(worseAvg as Float).toInt()}/100 for $worse at this location.",
                confidence = confidence,
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                evidenceSampleCount = wifi.size + cellular.size,
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
