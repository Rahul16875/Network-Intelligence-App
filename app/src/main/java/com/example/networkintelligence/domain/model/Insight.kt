package com.example.networkintelligence.domain.model

data class Insight(
    val id: String,
    val type: InsightType,
    val title: String,
    val description: String,
    val confidence: Float,
    val locationHash: String? = null,
    val timeWindow: TimeWindow? = null,
    val generatedAt: Long,
    val evidenceSampleCount: Int,
)

enum class InsightType {
    TIME_OF_DAY_PATTERN,
    PROVIDER_COMPARISON,
    LOCATION_QUALITY,
    CONNECTION_TYPE_PREFERENCE,
    LATENCY_SPIKE,
    SIGNAL_WEAKNESS,
}
