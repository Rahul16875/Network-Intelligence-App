package com.example.networkintelligence.domain.model

data class NetworkSample(
    val id: Long = 0L,
    val timestamp: Long,
    val networkType: NetworkType,
    val providerName: String?,
    val locationHash: String?,
    val signalStrengthDbm: Int?,
    val latencyMs: Long?,
    val probeFailureRate: Float,
    val score: Int,
)
