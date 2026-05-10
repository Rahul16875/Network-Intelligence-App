package com.example.networkintelligence.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavRoute(val path: String, val label: String, val icon: ImageVector) {
    data object Home : NavRoute("home", "Home", Icons.Outlined.SignalCellularAlt)
    data object AskAI : NavRoute("ask_ai", "Ask AI", Icons.Outlined.Psychology)
}

val BottomNavRoutes: List<NavRoute> = listOf(
    NavRoute.Home,
    NavRoute.AskAI,
)
