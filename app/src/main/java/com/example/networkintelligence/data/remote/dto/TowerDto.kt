package com.example.networkintelligence.data.remote.dto

data class TowerDto(
    val lat: Double,
    val lon: Double,
    val mcc: Int,
    val mnc: Int,
    val radio: String,
    val range: Int,
    val samples: Int,
)
