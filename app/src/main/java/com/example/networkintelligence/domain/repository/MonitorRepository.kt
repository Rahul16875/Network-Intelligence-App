package com.example.networkintelligence.domain.repository

import com.example.networkintelligence.domain.model.NetworkSample

interface MonitorRepository {

    suspend fun captureNow(): NetworkSample
}
