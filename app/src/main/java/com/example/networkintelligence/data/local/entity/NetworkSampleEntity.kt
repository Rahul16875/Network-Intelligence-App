package com.example.networkintelligence.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.networkintelligence.domain.model.NetworkType

@Entity(
    tableName = "network_samples",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["locationHash"]),
        Index(value = ["providerName"]),
    ],
)

data class NetworkSampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long,
    val networkType: NetworkType,
    val providerName: String?,
    val locationHash: String?,
    val signalStrengthDbm: Int?,
    val latencyMs: Long?,
    val probeFailureRate: Float,
    val score: Int,
)
