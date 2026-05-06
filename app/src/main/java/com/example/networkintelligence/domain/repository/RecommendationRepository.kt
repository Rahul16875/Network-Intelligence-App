package com.example.networkintelligence.domain.repository

import com.example.networkintelligence.domain.model.Recommendation
import kotlinx.coroutines.flow.Flow

interface RecommendationRepository {
    fun observeMeasuredRecommendations(): Flow<List<Recommendation>>
}
