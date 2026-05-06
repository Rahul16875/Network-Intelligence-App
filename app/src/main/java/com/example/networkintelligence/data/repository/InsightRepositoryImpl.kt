package com.example.networkintelligence.data.repository

import com.example.networkintelligence.domain.engine.InsightEngine
import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.repository.InsightRepository
import com.example.networkintelligence.domain.repository.SampleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InsightRepositoryImpl @Inject constructor(
    private val sampleRepository: SampleRepository,
    private val insightEngine: InsightEngine,
) : InsightRepository {

    override fun observeInsights(): Flow<List<Insight>> =
        sampleRepository.observeRecent(limit = 1000)
            .distinctUntilChanged()
            .conflate()
            .map { samples -> insightEngine.generate(samples) }
}
