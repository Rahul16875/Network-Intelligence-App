package com.example.networkintelligence.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavRoute(val path: String, val label: String, val icon: ImageVector) {
    data object Dashboard : NavRoute("dashboard", "Dashboard", Icons.Outlined.SignalCellularAlt)
    data object History : NavRoute("history", "History", Icons.Outlined.QueryStats)
    data object Insights : NavRoute("insights", "Insights", Icons.Outlined.Insights)
    data object Recommendations : NavRoute("recommendations", "Advice", Icons.Outlined.Lightbulb)
}

val BottomNavRoutes: List<NavRoute> = listOf(
    NavRoute.Dashboard,
    NavRoute.History,
    NavRoute.Insights,
    NavRoute.Recommendations,
)
