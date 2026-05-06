package com.example.networkintelligence.domain.repository

import com.example.networkintelligence.domain.model.Insight
import kotlinx.coroutines.flow.Flow

interface InsightRepository {
    fun observeInsights(): Flow<List<Insight>>
}
