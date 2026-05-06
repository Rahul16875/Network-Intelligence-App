package com.example.networkintelligence.data.aggregation

import com.example.networkintelligence.domain.model.AggregatedStats
import com.example.networkintelligence.domain.model.NetworkSample
import java.util.Calendar
import javax.inject.Inject

class AggregatedStatsComputer @Inject constructor() {

    fun compute(samples: List<NetworkSample>): List<AggregatedStats> {
        return samples
            .filter { it.locationHash != null }
            .groupBy { Triple(it.locationHash, it.providerName, it.networkType) }
            .map { (key, group) ->
                val (locationHash, providerName, networkType) = key
                val bucketKey = "${locationHash}_${providerName}_$networkType"
                val scores = group.map { it.score }
                val latencies = group.mapNotNull { it.latencyMs?.toInt() }
                val signals = group.mapNotNull { it.signalStrengthDbm }

                AggregatedStats(
                    bucketKey = bucketKey,
                    locationHash = locationHash,
                    providerName = providerName,
                    networkType = networkType,
                    avgScore = scores.average().toFloat(),
                    sampleCount = group.size,
                    avgLatencyMs = latencies.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
                    p90LatencyMs = latencies.takeIf { it.isNotEmpty() }?.let { p90(it) }?.toFloat(),
                    avgSignalDbm = signals.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
                    peakBadHours = findPeakBadHours(group),
                )
            }
    }

    private fun findPeakBadHours(samples: List<NetworkSample>): List<Int> {
        if (samples.size < 6) return emptyList()

        val overallAvg = samples.map { it.score }.average()
        val cal = Calendar.getInstance()

        val byHour = samples.groupBy { sample ->
            cal.timeInMillis = sample.timestamp
            cal.get(Calendar.HOUR_OF_DAY)
        }

        return byHour
            .filter { (_, hourSamples) ->
                hourSamples.size >= 2 &&
                    hourSamples.map { it.score }.average() < overallAvg - BAD_HOUR_THRESHOLD
            }
            .keys
            .sorted()
    }

    private fun p90(values: List<Int>): Int {
        val sorted = values.sorted()
        val index = (sorted.size * 0.9).toInt().coerceAtMost(sorted.size - 1)
        return sorted[index]
    }

    companion object {
        private const val BAD_HOUR_THRESHOLD = 15.0
    }
}
