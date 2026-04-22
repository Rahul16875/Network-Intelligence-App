package com.example.networkintelligence.di

import android.content.Context
import androidx.room.Room
import com.example.networkintelligence.data.local.dao.NetworkSampleDao
import com.example.networkintelligence.data.local.db.NetworkIntelligenceDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NetworkIntelligenceDatabase =
        Room.databaseBuilder(
            context,
            NetworkIntelligenceDatabase::class.java,
            NetworkIntelligenceDatabase.DATABASE_NAME,
        ).build()

    @Provides
    fun provideNetworkSampleDao(db: NetworkIntelligenceDatabase): NetworkSampleDao =
        db.networkSampleDao()
}
