package com.example.networkintelligence.domain.engine

import com.example.networkintelligence.domain.model.AggregatedStats
import com.example.networkintelligence.domain.model.Recommendation

interface RecommendationEngine {

    fun recommend(stats: List<AggregatedStats>): List<Recommendation>
}
