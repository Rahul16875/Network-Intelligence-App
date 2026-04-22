package com.example.networkintelligence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.networkintelligence.data.local.entity.NetworkSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NetworkSampleDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: NetworkSampleEntity): Long

    @Query("SELECT * FROM network_samples ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<NetworkSampleEntity>>

    @Query(
        "SELECT * FROM network_samples " +
            "WHERE timestamp BETWEEN :fromTs AND :toTs " +
            "ORDER BY timestamp ASC",
    )
    fun observeByRange(fromTs: Long, toTs: Long): Flow<List<NetworkSampleEntity>>

    @Query("SELECT * FROM network_samples WHERE locationHash = :locationHash ORDER BY timestamp DESC")
    suspend fun getByLocation(locationHash: String): List<NetworkSampleEntity>

    @Query("SELECT * FROM network_samples ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatest(): NetworkSampleEntity?

    @Query("SELECT * FROM network_samples ORDER BY timestamp DESC LIMIT 1")
    fun observeLatest(): Flow<NetworkSampleEntity?>

    @Query("SELECT COUNT(*) FROM network_samples")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM network_samples")
    fun observeCount(): Flow<Int>

    @Query("DELETE FROM network_samples WHERE timestamp < :thresholdTs")
    suspend fun deleteOlderThan(thresholdTs: Long): Int
}
