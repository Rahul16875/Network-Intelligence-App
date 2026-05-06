package com.example.networkintelligence.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.networkintelligence.data.local.entity.TowerCacheEntity

@Dao
interface TowerCacheDao {

    @Query("SELECT * FROM tower_cache WHERE bucket_key = :bucketKey LIMIT 1")
    suspend fun getByBucket(bucketKey: String): TowerCacheEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: TowerCacheEntity): Long

    @Query("DELETE FROM tower_cache WHERE fetched_at < :expiryTimestamp")
    suspend fun deleteExpired(expiryTimestamp: Long): Int
}
