package com.example.networkintelligence.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.networkintelligence.data.local.dao.NetworkSampleDao
import com.example.networkintelligence.data.local.dao.TowerCacheDao
import com.example.networkintelligence.data.local.entity.NetworkSampleEntity
import com.example.networkintelligence.data.local.entity.TowerCacheEntity

@Database(
    entities = [NetworkSampleEntity::class, TowerCacheEntity::class],
    version = 2,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class NetworkIntelligenceDatabase : RoomDatabase() {

    abstract fun networkSampleDao(): NetworkSampleDao
    abstract fun towerCacheDao(): TowerCacheDao

    companion object {
        const val DATABASE_NAME: String = "network_intelligence.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS tower_cache (
                        bucket_key TEXT NOT NULL PRIMARY KEY,
                        fetched_at INTEGER NOT NULL,
                        payload TEXT NOT NULL
                    )
                    """.trimIndent(),
                )
            }
        }
    }
}
