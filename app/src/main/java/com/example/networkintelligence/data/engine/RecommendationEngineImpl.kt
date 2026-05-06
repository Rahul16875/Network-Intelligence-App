package com.example.networkintelligence.data.engine

import com.example.networkintelligence.domain.engine.RecommendationEngine
import com.example.networkintelligence.domain.model.AggregatedStats
import com.example.networkintelligence.domain.model.NetworkType
import com.example.networkintelligence.domain.model.Recommendation
import com.example.networkintelligence.domain.model.RecommendationCategory
import com.example.networkintelligence.domain.model.SourceType
import javax.inject.Inject

class RecommendationEngineImpl @Inject constructor() : RecommendationEngine {

    override fun recommend(stats: List<AggregatedStats>): List<Recommendation> {
        val recommendations = mutableListOf<Recommendation>()
        val byLocation = stats.filter { it.locationHash != null }.groupBy { it.locationHash!! }

        byLocation.forEach { (location, locationStats) ->
            recommendations += wifiVsCellularRec(location, locationStats)
            recommendations += bestProviderRec(location, locationStats)
            recommendations += avoidZoneRec(location, locationStats)
            recommendations += timeShiftRecs(location, locationStats)
        }

        return recommendations.sortedByDescending {
            when (it.category) {
                RecommendationCategory.SWITCH_CARRIER_SUGGESTION -> 4
                RecommendationCategory.BEST_PROVIDER_IN_AREA -> 3
                RecommendationCategory.WIFI_VS_CELLULAR -> 2
                RecommendationCategory.TIME_SHIFT_HEAVY_TASK -> 1
                RecommendationCategory.AVOID_ZONE -> 1
                else -> 0
            }
        }
    }

    private fun wifiVsCellularRec(location: String, stats: List<AggregatedStats>): List<Recommendation> {
        val wifiStats = stats.filter { it.networkType == NetworkType.WIFI && it.sampleCount >= MIN_SAMPLES }
        val cellStats = stats.filter { it.networkType.isCellular() && it.sampleCount >= MIN_SAMPLES }

        if (wifiStats.isEmpty() || cellStats.isEmpty()) return emptyList()

        val wifiAvg = wifiStats.map { it.avgScore }.average().toFloat()
        val cellAvg = cellStats.map { it.avgScore }.average().toFloat()
        val diff = wifiAvg - cellAvg

        if (Math.abs(diff) < MIN_DIFF) return emptyList()

        val (better, worse) = if (diff > 0) "Wi-Fi" to "cellular" else "cellular data" to "Wi-Fi"
        val (betterAvg, worseAvg) = if (diff > 0) wifiAvg to cellAvg else cellAvg to wifiAvg

        return listOf(
            Recommendation(
                id = "wificell_$location",
                category = RecommendationCategory.WIFI_VS_CELLULAR,
                message = "Use $better at this location",
                reason = "$better averages ${betterAvg.toInt()}/100 vs ${worseAvg.toInt()}/100 for $worse here.",
                expectedBenefit = "+${Math.abs(diff).toInt()} pts improvement",
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                sourceType = SourceType.MEASURED,
            ),
        )
    }

    private fun bestProviderRec(location: String, stats: List<AggregatedStats>): List<Recommendation> {
        val cellularByProvider = stats
            .filter { it.networkType.isCellular() && it.providerName != null && it.sampleCount >= MIN_SAMPLES }
            .groupBy { it.providerName!! }

        if (cellularByProvider.size < 2) return emptyList()

        val providerAvgs = cellularByProvider.mapValues { (_, s) -> s.map { it.avgScore }.average().toFloat() }
        val best = providerAvgs.maxByOrNull { it.value } ?: return emptyList()
        val worst = providerAvgs.minByOrNull { it.value } ?: return emptyList()

        val diff = best.value - worst.value
        if (diff < MIN_DIFF) return emptyList()

        val category = if (diff >= STRONG_DIFF) RecommendationCategory.SWITCH_CARRIER_SUGGESTION
        else RecommendationCategory.BEST_PROVIDER_IN_AREA

        return listOf(
            Recommendation(
                id = "provider_$location",
                category = category,
                message = "${best.key} performs better here",
                reason = "${best.key} averages ${best.value.toInt()}/100 vs ${worst.value.toInt()}/100 for ${worst.key} at this location.",
                expectedBenefit = "+${diff.toInt()} pts improvement",
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                sourceType = SourceType.MEASURED,
            ),
        )
    }

    private fun avoidZoneRec(location: String, stats: List<AggregatedStats>): List<Recommendation> {
        val allSamples = stats.sumOf { it.sampleCount }
        if (allSamples < MIN_SAMPLES * 2) return emptyList()

        val overallAvg = stats.map { it.avgScore * it.sampleCount }.sum() / allSamples
        if (overallAvg >= POOR_SCORE_THRESHOLD) return emptyList()

        return listOf(
            Recommendation(
                id = "avoidzone_$location",
                category = RecommendationCategory.AVOID_ZONE,
                message = "Poor network coverage at this location",
                reason = "Average score is ${overallAvg.toInt()}/100 across $allSamples samples — coverage is limited here.",
                expectedBenefit = null,
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                sourceType = SourceType.MEASURED,
            ),
        )
    }

    private fun timeShiftRecs(location: String, stats: List<AggregatedStats>): List<Recommendation> {
        val badHours = stats.flatMap { it.peakBadHours }.distinct().sorted()
        if (badHours.isEmpty()) return emptyList()

        val hoursDesc = badHours.joinToString(", ") { "${it}:00" }

        return listOf(
            Recommendation(
                id = "timeshift_$location",
                category = RecommendationCategory.TIME_SHIFT_HEAVY_TASK,
                message = "Avoid heavy data use at $hoursDesc",
                reason = "Network quality consistently drops during these hours at this location.",
                expectedBenefit = "Better throughput outside these windows",
                locationHash = location,
                generatedAt = System.currentTimeMillis(),
                sourceType = SourceType.MEASURED,
            ),
        )
    }

    private fun NetworkType?.isCellular() = this in setOf(
        NetworkType.CELLULAR_5G, NetworkType.CELLULAR_4G,
        NetworkType.CELLULAR_3G, NetworkType.CELLULAR_2G, NetworkType.CELLULAR_UNKNOWN,
    )

    companion object {
        private const val MIN_SAMPLES = 5
        private const val MIN_DIFF = 10f
        private const val STRONG_DIFF = 20f
        private const val POOR_SCORE_THRESHOLD = 40f
    }
}
