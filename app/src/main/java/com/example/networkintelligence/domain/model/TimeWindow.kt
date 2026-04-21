package com.example.networkintelligence.domain.model

data class TimeWindow(
    val startHour: Int,
    val endHour: Int,
    val daysOfWeek: Set<Int> = ALL_DAYS,
) {
    companion object {
        val ALL_DAYS: Set<Int> = (1..7).toSet()
    }
}
