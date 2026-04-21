package com.example.networkintelligence.domain.model

data class AggregatedStats(
    val bucketKey: String,
    val locationHash: String?,
    val providerName: String?,
    val networkType: NetworkType?,
    val avgScore: Float,
    val sampleCount: Int,
    val avgLatencyMs: Float?,
    val p90LatencyMs: Float?,
    val avgSignalDbm: Float?,
    val peakBadHours: List<Int>,
)
