package com.example.networkintelligence.data.engine

import com.example.networkintelligence.domain.engine.DiagnosisEngine
import com.example.networkintelligence.domain.model.Diagnosis
import com.example.networkintelligence.domain.model.DiagnosisCause
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.model.NetworkType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiagnosisEngineImpl @Inject constructor() : DiagnosisEngine {

    override fun diagnose(sample: NetworkSample): Diagnosis {
        if (sample.networkType == NetworkType.NONE) {
            return Diagnosis(
                cause = DiagnosisCause.NO_NETWORK,
                summary = "No network",
                detail = "Your phone reports no active connection. Check airplane mode, SIM, or Wi-Fi settings.",
            )
        }

        if (sample.networkType == NetworkType.CELLULAR_2G || sample.networkType == NetworkType.CELLULAR_3G) {
            return Diagnosis(
                cause = DiagnosisCause.DEGRADED_GENERATION,
                summary = "On ${if (sample.networkType == NetworkType.CELLULAR_2G) "2G" else "3G"} network",
                detail = "Your phone dropped to an older generation. Either 4G/5G is unavailable here or the tower is overloaded.",
            )
        }

        val weakSignal = isWeakSignal(sample)
        val highFailureRate = sample.probeFailureRate > 0.3f
        val highLatency = sample.latencyMs != null && sample.latencyMs > 300L

        if (highFailureRate && !weakSignal) {
            return Diagnosis(
                cause = DiagnosisCause.PACKET_LOSS,
                summary = "Packets dropping",
                detail = "Signal looks strong but ${(sample.probeFailureRate * 100).toInt()}% of probes failed. Likely a captive portal (public Wi-Fi sign-in) or severe congestion.",
            )
        }

        if (weakSignal) {
            return Diagnosis(
                cause = DiagnosisCause.WEAK_SIGNAL,
                summary = "Weak signal",
                detail = "Signal strength is low (${sample.signalStrengthDbm ?: "unknown"} dBm). You're far from the access point or there's something blocking the signal.",
            )
        }

        if (highLatency && !highFailureRate) {
            return Diagnosis(
                cause = DiagnosisCause.CONGESTION,
                summary = "Network congestion",
                detail = "Signal is good but response time is ${sample.latencyMs}ms. The path is busy — likely peak-hour congestion or a slow backend.",
            )
        }

        if (sample.score >= HEALTHY_SCORE_THRESHOLD) {
            return Diagnosis(
                cause = DiagnosisCause.HEALTHY,
                summary = "All good",
                detail = "Network is performing well on all measured signals.",
            )
        }

        return Diagnosis(
            cause = DiagnosisCause.MARGINAL,
            summary = "Marginal quality",
            detail = "No single signal is broken, but several are mediocre. Expect occasional slowness.",
        )
    }

    private fun isWeakSignal(sample: NetworkSample): Boolean {
        val dbm = sample.signalStrengthDbm ?: return false
        return when (sample.networkType) {
            NetworkType.WIFI -> dbm < WIFI_WEAK_DBM
            NetworkType.CELLULAR_5G,
            NetworkType.CELLULAR_4G,
            NetworkType.CELLULAR_UNKNOWN -> dbm < CELLULAR_WEAK_DBM
            else -> false
        }
    }

    private companion object {
        const val HEALTHY_SCORE_THRESHOLD = 70
        const val WIFI_WEAK_DBM = -90
        const val CELLULAR_WEAK_DBM = -105
    }
}
