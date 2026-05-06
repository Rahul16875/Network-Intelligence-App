package com.example.networkintelligence.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tower_cache")
data class TowerCacheEntity(
    @PrimaryKey
    @ColumnInfo(name = "bucket_key")
    val bucketKey: String,
    @ColumnInfo(name = "fetched_at")
    val fetchedAt: Long,
    @ColumnInfo(name = "payload")
    val payload: String,
)
