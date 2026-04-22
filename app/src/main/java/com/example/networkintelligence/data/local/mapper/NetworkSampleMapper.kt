package com.example.networkintelligence.data.local.mapper

import com.example.networkintelligence.data.local.entity.NetworkSampleEntity
import com.example.networkintelligence.domain.model.NetworkSample

fun NetworkSampleEntity.toDomain(): NetworkSample = NetworkSample(
    id = id,
    timestamp = timestamp,
    networkType = networkType,
    providerName = providerName,
    locationHash = locationHash,
    signalStrengthDbm = signalStrengthDbm,
    latencyMs = latencyMs,
    probeFailureRate = probeFailureRate,
    score = score,
)

fun NetworkSample.toEntity(): NetworkSampleEntity = NetworkSampleEntity(
    id = id,
    timestamp = timestamp,
    networkType = networkType,
    providerName = providerName,
    locationHash = locationHash,
    signalStrengthDbm = signalStrengthDbm,
    latencyMs = latencyMs,
    probeFailureRate = probeFailureRate,
    score = score,
)
