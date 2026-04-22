package com.example.networkintelligence.domain.repository

import com.example.networkintelligence.domain.model.NetworkSample
import kotlinx.coroutines.flow.Flow

interface SampleRepository {

    suspend fun insert(sample: NetworkSample): Long

    fun observeRecent(limit: Int = 100): Flow<List<NetworkSample>>

    fun observeByRange(fromTimestamp: Long, toTimestamp: Long): Flow<List<NetworkSample>>

    suspend fun getByLocation(locationHash: String): List<NetworkSample>

    suspend fun getLatest(): NetworkSample?

    fun observeLatest(): Flow<NetworkSample?>

    suspend fun count(): Int

    fun observeCount(): Flow<Int>

    suspend fun deleteOlderThan(timestamp: Long): Int
}
