package com.example.networkintelligence.data.aggregation

import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.model.NetworkType
import java.util.Calendar

object SampleAggregator {

    fun groupByLocation(samples: List<NetworkSample>): Map<String, List<NetworkSample>> =
        samples.filter { it.locationHash != null }.groupBy { it.locationHash!! }

    fun groupByProvider(samples: List<NetworkSample>): Map<String, List<NetworkSample>> =
        samples.filter { it.providerName != null }.groupBy { it.providerName!! }

    fun groupByLocationAndProvider(samples: List<NetworkSample>): Map<Pair<String, String>, List<NetworkSample>> =
        samples.filter { it.locationHash != null && it.providerName != null }
            .groupBy { it.locationHash!! to it.providerName!! }

    fun groupByHourOfDay(samples: List<NetworkSample>): Map<Int, List<NetworkSample>> {
        val cal = Calendar.getInstance()
        return samples.groupBy { sample ->
            cal.timeInMillis = sample.timestamp
            cal.get(Calendar.HOUR_OF_DAY)
        }
    }

    fun avg(values: List<Number>): Float {
        if (values.isEmpty()) return 0f
        return values.sumOf { it.toDouble() }.toFloat() / values.size
    }

    fun avgScore(samples: List<NetworkSample>): Float = avg(samples.map { it.score })

    fun avgLatency(samples: List<NetworkSample>): Float? {
        val latencies = samples.mapNotNull { it.latencyMs }
        return if (latencies.isEmpty()) null else avg(latencies)
    }

    fun avgSignalDbm(samples: List<NetworkSample>): Float? {
        val signals = samples.mapNotNull { it.signalStrengthDbm }
        return if (signals.isEmpty()) null else avg(signals)
    }

    fun weakSignalFraction(samples: List<NetworkSample>): Float {
        val withSignal = samples.filter { it.signalStrengthDbm != null }
        if (withSignal.isEmpty()) return 0f
        val weak = withSignal.count { s ->
            val dbm = s.signalStrengthDbm!!
            when {
                s.networkType == NetworkType.WIFI -> dbm < -80
                else -> dbm < -105
            }
        }
        return weak.toFloat() / withSignal.size
    }

    fun wifiSamples(samples: List<NetworkSample>): List<NetworkSample> =
        samples.filter { it.networkType == NetworkType.WIFI }

    fun cellularSamples(samples: List<NetworkSample>): List<NetworkSample> =
        samples.filter {
            it.networkType in setOf(
                NetworkType.CELLULAR_5G, NetworkType.CELLULAR_4G,
                NetworkType.CELLULAR_3G, NetworkType.CELLULAR_2G, NetworkType.CELLULAR_UNKNOWN,
            )
        }

    fun morningHours(samples: List<NetworkSample>): List<NetworkSample> =
        samples.filter { hourOf(it) in 6..11 }

    fun eveningHours(samples: List<NetworkSample>): List<NetworkSample> =
        samples.filter { hourOf(it) in 18..23 }

    private fun hourOf(sample: NetworkSample): Int {
        val cal = Calendar.getInstance()
        cal.timeInMillis = sample.timestamp
        return cal.get(Calendar.HOUR_OF_DAY)
    }
}
