package com.example.networkintelligence.data.engine

import com.example.networkintelligence.domain.engine.ScoreEngine
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.model.NetworkType
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class ScoreEngineImpl @Inject constructor() : ScoreEngine {

    override fun calculate(sample: NetworkSample): Int {
        if (sample.networkType == NetworkType.NONE) return 0

        val latencyScore = latencyToScore(sample.latencyMs)
        val signalScore = signalToScore(sample.signalStrengthDbm, sample.networkType)
        val reliabilityScore = reliabilityToScore(sample.probeFailureRate)
        val typeScore = typeToScore(sample.networkType)

        val weighted =
            latencyScore * WEIGHT_LATENCY +
                signalScore * WEIGHT_SIGNAL +
                reliabilityScore * WEIGHT_RELIABILITY +
                typeScore * WEIGHT_TYPE

        return weighted.roundToInt().coerceIn(0, 100)
    }

    private fun latencyToScore(latencyMs: Long?): Double {
        if (latencyMs == null) return 0.0
        return when {
            latencyMs <= 50 -> 100.0
            latencyMs <= 150 -> lerp(100.0, 80.0, fraction(latencyMs, 50, 150))
            latencyMs <= 300 -> lerp(80.0, 60.0, fraction(latencyMs, 150, 300))
            latencyMs <= 600 -> lerp(60.0, 30.0, fraction(latencyMs, 300, 600))
            latencyMs <= 1200 -> lerp(30.0, 0.0, fraction(latencyMs, 600, 1200))
            else -> 0.0
        }
    }

    private fun signalToScore(dbm: Int?, type: NetworkType): Double {
        if (dbm == null) return NEUTRAL_SIGNAL_SCORE
        return when (type) {
            NetworkType.WIFI -> wifiSignal(dbm)
            NetworkType.CELLULAR_5G,
            NetworkType.CELLULAR_4G,
            NetworkType.CELLULAR_3G,
            NetworkType.CELLULAR_2G,
            NetworkType.CELLULAR_UNKNOWN -> cellularSignal(dbm)
            NetworkType.ETHERNET -> 100.0
            NetworkType.NONE -> 0.0
        }
    }

    private fun wifiSignal(dbm: Int): Double = when {
        dbm >= -50 -> 100.0
        dbm >= -70 -> lerp(100.0, 75.0, fraction(dbm.toLong(), -50L, -70L))
        dbm >= -85 -> lerp(75.0, 40.0, fraction(dbm.toLong(), -70L, -85L))
        dbm >= -95 -> lerp(40.0, 10.0, fraction(dbm.toLong(), -85L, -95L))
        else -> 5.0
    }

    private fun cellularSignal(dbm: Int): Double = when {
        dbm >= -75 -> 100.0
        dbm >= -90 -> lerp(100.0, 70.0, fraction(dbm.toLong(), -75L, -90L))
        dbm >= -105 -> lerp(70.0, 40.0, fraction(dbm.toLong(), -90L, -105L))
        dbm >= -115 -> lerp(40.0, 10.0, fraction(dbm.toLong(), -105L, -115L))
        else -> 5.0
    }

    private fun reliabilityToScore(failureRate: Float): Double =
        ((1f - failureRate).coerceIn(0f, 1f) * 100f).toDouble()

    private fun typeToScore(type: NetworkType): Double = when (type) {
        NetworkType.WIFI -> 100.0
        NetworkType.ETHERNET -> 100.0
        NetworkType.CELLULAR_5G -> 90.0
        NetworkType.CELLULAR_4G -> 70.0
        NetworkType.CELLULAR_UNKNOWN -> 55.0
        NetworkType.CELLULAR_3G -> 40.0
        NetworkType.CELLULAR_2G -> 10.0
        NetworkType.NONE -> 0.0
    }

    private fun fraction(value: Long, from: Long, to: Long): Double =
        ((value - from).toDouble() / (to - from).toDouble()).coerceIn(0.0, 1.0)

    private fun lerp(start: Double, end: Double, t: Double): Double = start + (end - start) * t

    private companion object {
        const val WEIGHT_LATENCY = 0.35
        const val WEIGHT_SIGNAL = 0.30
        const val WEIGHT_RELIABILITY = 0.25
        const val WEIGHT_TYPE = 0.10
        const val NEUTRAL_SIGNAL_SCORE = 50.0
    }
}
