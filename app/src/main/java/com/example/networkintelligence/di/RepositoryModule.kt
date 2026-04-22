package com.example.networkintelligence.di

import com.example.networkintelligence.data.engine.DiagnosisEngineImpl
import com.example.networkintelligence.data.engine.ScoreEngineImpl
import com.example.networkintelligence.data.repository.MonitorRepositoryImpl
import com.example.networkintelligence.data.repository.SampleRepositoryImpl
import com.example.networkintelligence.domain.engine.DiagnosisEngine
import com.example.networkintelligence.domain.engine.ScoreEngine
import com.example.networkintelligence.domain.repository.MonitorRepository
import com.example.networkintelligence.domain.repository.SampleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSampleRepository(impl: SampleRepositoryImpl): SampleRepository

    @Binds
    @Singleton
    abstract fun bindMonitorRepository(impl: MonitorRepositoryImpl): MonitorRepository

    @Binds
    @Singleton
    abstract fun bindScoreEngine(impl: ScoreEngineImpl): ScoreEngine

    @Binds
    @Singleton
    abstract fun bindDiagnosisEngine(impl: DiagnosisEngineImpl): DiagnosisEngine
}
