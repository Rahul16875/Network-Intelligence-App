package com.example.networkintelligence.data.repository

import com.example.networkintelligence.data.local.dao.NetworkSampleDao
import com.example.networkintelligence.data.local.mapper.toDomain
import com.example.networkintelligence.data.local.mapper.toEntity
import com.example.networkintelligence.domain.model.NetworkSample
import com.example.networkintelligence.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SampleRepositoryImpl @Inject constructor(
    private val dao: NetworkSampleDao,
) : SampleRepository {

    override suspend fun insert(sample: NetworkSample): Long =
        dao.insert(sample.toEntity())

    override fun observeRecent(limit: Int): Flow<List<NetworkSample>> =
        dao.observeRecent(limit).map { list -> list.map(NetworkSampleEntityToDomain) }

    override fun observeByRange(fromTimestamp: Long, toTimestamp: Long): Flow<List<NetworkSample>> =
        dao.observeByRange(fromTimestamp, toTimestamp)
            .map { list -> list.map(NetworkSampleEntityToDomain) }

    override suspend fun getByLocation(locationHash: String): List<NetworkSample> =
        dao.getByLocation(locationHash).map(NetworkSampleEntityToDomain)

    override suspend fun getLatest(): NetworkSample? = dao.getLatest()?.toDomain()

    override fun observeLatest(): Flow<NetworkSample?> =
        dao.observeLatest().map { it?.toDomain() }

    override suspend fun count(): Int = dao.count()

    override fun observeCount(): Flow<Int> = dao.observeCount()

    override suspend fun deleteOlderThan(timestamp: Long): Int = dao.deleteOlderThan(timestamp)

    private companion object {
        val NetworkSampleEntityToDomain: (com.example.networkintelligence.data.local.entity.NetworkSampleEntity) -> NetworkSample =
            { it.toDomain() }
    }
}
