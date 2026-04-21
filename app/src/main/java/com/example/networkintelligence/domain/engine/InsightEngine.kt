package com.example.networkintelligence.domain.engine

import com.example.networkintelligence.domain.model.Insight
import com.example.networkintelligence.domain.model.NetworkSample

interface InsightEngine {

    fun generate(samples: List<NetworkSample>): List<Insight>
}
