package com.example.networkintelligence.domain.model

data class CarrierCoverage(
    val carrierName: String,
    val mcc: Int,
    val mnc: Int,
    val towerCount: Int,
    val hasNr: Boolean,
    val hasLte: Boolean,
    val hasUmts: Boolean,
    val hasGsm: Boolean,
) {
    val bestRadioLabel: String get() = when {
        hasNr -> "5G"
        hasLte -> "LTE"
        hasUmts -> "3G"
        hasGsm -> "2G"
        else -> "Unknown"
    }
}
