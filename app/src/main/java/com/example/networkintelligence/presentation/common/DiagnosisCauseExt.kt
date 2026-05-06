package com.example.networkintelligence.presentation.common

import com.example.networkintelligence.domain.model.DiagnosisCause

fun DiagnosisCause.causeLabel(): String = when (this) {
    DiagnosisCause.HEALTHY -> "Healthy"
    DiagnosisCause.MARGINAL -> "Marginal coverage"
    DiagnosisCause.WEAK_SIGNAL -> "Weak signal"
    DiagnosisCause.PACKET_LOSS -> "Packet loss"
    DiagnosisCause.CONGESTION -> "Network congestion"
    DiagnosisCause.DEGRADED_GENERATION -> "Degraded network type"
    DiagnosisCause.NO_NETWORK -> "No network"
}
