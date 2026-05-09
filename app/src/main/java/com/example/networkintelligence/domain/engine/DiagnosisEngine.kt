package com.example.networkintelligence.domain.engine

import com.example.networkintelligence.domain.model.Diagnosis
import com.example.networkintelligence.domain.model.NetworkSample

interface DiagnosisEngine {
    fun diagnose(sample: NetworkSample): Diagnosis
}
