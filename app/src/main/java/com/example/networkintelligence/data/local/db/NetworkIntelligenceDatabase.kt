package com.example.networkintelligence.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.networkintelligence.data.local.dao.NetworkSampleDao
import com.example.networkintelligence.data.local.entity.NetworkSampleEntity

@Database(
    entities = [NetworkSampleEntity::class],
    version = 1,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class NetworkIntelligenceDatabase : RoomDatabase() {

    abstract fun networkSampleDao(): NetworkSampleDao

    companion object {
        const val DATABASE_NAME: String = "network_intelligence.db"
    }
}
