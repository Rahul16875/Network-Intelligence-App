package com.example.networkintelligence.di

import com.example.networkintelligence.data.engine.DiagnosisEngineImpl
import com.example.networkintelligence.data.engine.InsightEngineImpl
import com.example.networkintelligence.data.engine.RecommendationEngineImpl
import com.example.networkintelligence.data.engine.ScoreEngineImpl
import com.example.networkintelligence.data.repository.InsightRepositoryImpl
import com.example.networkintelligence.data.repository.MonitorRepositoryImpl
import com.example.networkintelligence.data.repository.RecommendationRepositoryImpl
import com.example.networkintelligence.data.repository.SampleRepositoryImpl
import com.example.networkintelligence.domain.engine.DiagnosisEngine
import com.example.networkintelligence.domain.engine.InsightEngine
import com.example.networkintelligence.domain.engine.RecommendationEngine
import com.example.networkintelligence.domain.engine.ScoreEngine
import com.example.networkintelligence.domain.repository.InsightRepository
import com.example.networkintelligence.domain.repository.MonitorRepository
import com.example.networkintelligence.domain.repository.RecommendationRepository
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

    @Binds
    @Singleton
    abstract fun bindInsightEngine(impl: InsightEngineImpl): InsightEngine

    @Binds
    @Singleton
    abstract fun bindInsightRepository(impl: InsightRepositoryImpl): InsightRepository

    @Binds
    @Singleton
    abstract fun bindRecommendationEngine(impl: RecommendationEngineImpl): RecommendationEngine

    @Binds
    @Singleton
    abstract fun bindRecommendationRepository(impl: RecommendationRepositoryImpl): RecommendationRepository
}
