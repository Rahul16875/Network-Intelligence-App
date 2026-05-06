package com.example.networkintelligence.data.engine

import com.example.networkintelligence.data.engine.detectors.LatencySpikeDetector
import com.example.networkintelligence.data.engine.detectors.LocationQualityDetector
import com.example.networkintelligence.data.engine.detectors.ProviderComparisonDetector
import com.example.networkintelligence.data.engine.detectors.SignalWeaknessDetector
import com.example.networkintelligence.data.engine.detectors.TimeOfDayDetector
import com.example.networkintelligence.data.engine.detectors.WifiVsCellularDetector
import com.example.networkintelligence.domain.engine.InsightEngine
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.NetworkSample
import javax.inject.Inject

class InsightEngineImpl @Inject constructor() : InsightEngine {

    private val timeOfDayDetector = TimeOfDayDetector()
    private val locationQualityDetector = LocationQualityDetector()
    private val wifiVsCellularDetector = WifiVsCellularDetector()
    private val latencySpikeDetector = LatencySpikeDetector()
    private val signalWeaknessDetector = SignalWeaknessDetector()
    private val providerComparisonDetector = ProviderComparisonDetector()

    override fun generate(samples: List<NetworkSample>): List<Insight> {
        if (samples.isEmpty()) return emptyList()

        return (
            timeOfDayDetector.detect(samples) +
                locationQualityDetector.detect(samples) +
                wifiVsCellularDetector.detect(samples) +
                latencySpikeDetector.detect(samples) +
                signalWeaknessDetector.detect(samples) +
                providerComparisonDetector.detect(samples)
            )
            .distinctBy { it.id }
            .sortedByDescending { it.confidence }
    }
}
