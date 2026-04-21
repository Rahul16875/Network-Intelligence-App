package com.example.networkintelligence.domain.engine

import com.example.networkintelligence.domain.model.NetworkSample

interface ScoreEngine {

    fun calculate(sample: NetworkSample): Int
}
