package com.example.networkintelligence.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.networkintelligence.domain.model.NetworkMeasurement
import com.example.networkintelligence.presentation.agent.AgentScreen
import com.example.networkintelligence.presentation.dashboard.DashboardScreen
import com.example.networkintelligence.ui.theme.JetBrainsMonoFamily
import com.example.networkintelligence.ui.theme.KS_Outline
import com.example.networkintelligence.ui.theme.KS_OutlineVariant
import com.example.networkintelligence.ui.theme.KS_Secondary
import com.example.networkintelligence.ui.theme.KS_Surface
import com.example.networkintelligence.ui.theme.KS_SurfaceContainerLowest

@Composable
fun NetworkIntelligenceNavHost() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentPath = backStackEntry?.destination?.route

    var latestMeasurement by remember { mutableStateOf<NetworkMeasurement?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = KS_Surface,
        bottomBar = {
            KineticBottomNav(
                currentPath = currentPath,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = NavRoute.Home.path,
        ) {
            composable(NavRoute.Home.path) {
                DashboardScreen(
                    contentPadding = innerPadding,
                    onMeasurementDone = { latestMeasurement = it },
                )
            }
            composable(NavRoute.AskAI.path) {
                AgentScreen(
                    contentPadding = innerPadding,
                    currentMeasurement = latestMeasurement,
                )
            }
        }
    }
}

@Composable
private fun KineticBottomNav(currentPath: String?, onNavigate: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(KS_OutlineVariant.copy(alpha = 0.15f)),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KS_SurfaceContainerLowest.copy(alpha = 0.95f))
                .padding(horizontal = 32.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            BottomNavRoutes.forEach { route ->
                val selected = currentPath == route.path
                KineticNavItem(
                    route = route,
                    selected = selected,
                    onClick = { if (!selected) onNavigate(route.path) },
                )
            }
        }
    }
}

@Composable
private fun KineticNavItem(route: NavRoute, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 48.dp, height = 32.dp)
                .background(
                    color = if (selected) KS_Secondary.copy(alpha = 0.12f) else Color.Transparent,
                    shape = RoundedCornerShape(50),
                )
                .then(
                    if (selected) Modifier.border(
                        width = 1.dp,
                        color = KS_Secondary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50),
                    ) else Modifier,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = route.icon,
                contentDescription = route.label,
                tint = if (selected) KS_Secondary else KS_Outline,
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = route.label.uppercase().replace(" ", "_"),
            style = TextStyle(
                fontFamily = JetBrainsMonoFamily,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                fontSize = 8.sp,
                letterSpacing = 1.sp,
            ),
            color = if (selected) KS_Secondary else KS_Outline,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
