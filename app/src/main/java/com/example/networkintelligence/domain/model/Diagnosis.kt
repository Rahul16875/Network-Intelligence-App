package com.example.networkintelligence.domain.model

data class Diagnosis(
    val cause: DiagnosisCause,
    val summary: String,
    val detail: String,
)

enum class DiagnosisCause {
    HEALTHY,
    MARGINAL,
    WEAK_SIGNAL,
    PACKET_LOSS,
    CONGESTION,
    DEGRADED_GENERATION,
    NO_NETWORK,
}
