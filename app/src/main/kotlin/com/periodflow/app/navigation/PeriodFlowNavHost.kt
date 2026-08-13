package com.periodflow.app.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.List
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.periodflow.feature.home.navigation.HomeRoute
import com.periodflow.feature.home.navigation.homeScreen
import com.periodflow.feature.log.navigation.LogRoute
import com.periodflow.feature.log.navigation.logScreen
import com.periodflow.feature.log.navigation.navigateToLog
import com.periodflow.feature.stats.navigation.StatsRoute
import com.periodflow.feature.stats.navigation.statsScreen
import com.periodflow.feature.settings.navigation.SettingsRoute
import com.periodflow.feature.settings.navigation.settingsScreen

import com.periodflow.feature.onboarding.navigation.onboardingScreen
import com.periodflow.feature.onboarding.navigation.OnboardingRoute
import com.periodflow.feature.health_insights.navigation.healthInsightsScreen
import com.periodflow.feature.health_insights.navigation.HealthInsightsDestination
import com.periodflow.feature.health_insights.navigation.navigateToHealthInsights
import com.periodflow.core.ui.components.ClayNavigationBar
import com.periodflow.core.ui.components.ClayNavigationBarItem

data class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val label: String,
)

val TOP_LEVEL_DESTINATIONS = listOf(
    TopLevelDestination(HomeRoute, Icons.Rounded.Home, "Home"),
    TopLevelDestination(StatsRoute, Icons.Rounded.List, "Stats"),
    TopLevelDestination(SettingsRoute, Icons.Rounded.Settings, "Settings"),
)

@Composable
fun PeriodFlowNavHost(startDestination: Any = HomeRoute) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Hide bottom bar on log screen and onboarding screen
    val showBottomBar = currentDestination?.let { dest ->
        TOP_LEVEL_DESTINATIONS.any { dest.hasRoute(it.route::class) }
    } ?: true

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ClayNavigationBar {
                    TOP_LEVEL_DESTINATIONS.forEach { destination ->
                        val selected = currentDestination?.hasRoute(destination.route::class) == true

                        ClayNavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = destination.icon,
                            label = destination.label,
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
        ) {
            onboardingScreen(
                onComplete = {
                    navController.navigate(HomeRoute) {
                        popUpTo(OnboardingRoute) { inclusive = true }
                    }
                }
            )

            homeScreen(
                onNavigateToLog = { dateEpochDay ->
                    navController.navigateToLog(dateEpochDay)
                },
            )

            logScreen(
                onNavigateBack = { navController.popBackStack() },
            )

            statsScreen(
                onNavigateToHealthInsights = {
                    navController.navigateToHealthInsights()
                }
            )

            settingsScreen()
            
            healthInsightsScreen()
        }
    }
}
