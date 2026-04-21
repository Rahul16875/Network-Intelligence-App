package com.example.networkintelligence.domain.model

data class Recommendation(
    val id: String,
    val category: RecommendationCategory,
    val message: String,
    val reason: String,
    val expectedBenefit: String?,
    val locationHash: String? = null,
    val generatedAt: Long,
)

enum class RecommendationCategory {
    BEST_PROVIDER_IN_AREA,
    WIFI_VS_CELLULAR,
    AVOID_ZONE,
    TIME_SHIFT_HEAVY_TASK,
    SWITCH_CARRIER_SUGGESTION,
}
