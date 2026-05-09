package com.example.networkintelligence.domain.model

data class NetworkMeasurement(
    val timestamp: Long,
    val networkType: NetworkType,
    val providerName: String?,
    val signalStrengthDbm: Int?,
    val latencyMs: Long?,
    val downloadMbps: Float?,
    val uploadMbps: Float?,
    val score: Int,
    val diagnosis: Diagnosis?,
)
