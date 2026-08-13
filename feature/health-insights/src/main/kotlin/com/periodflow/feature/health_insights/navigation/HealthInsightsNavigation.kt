package com.periodflow.feature.health_insights.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.periodflow.feature.health_insights.HealthInsightsRoute
import kotlinx.serialization.Serializable

@Serializable
data object HealthInsightsDestination

fun NavController.navigateToHealthInsights(navOptions: NavOptions? = null) {
    navigate(HealthInsightsDestination, navOptions)
}

fun NavGraphBuilder.healthInsightsScreen() {
    composable<HealthInsightsDestination> {
        HealthInsightsRoute()
    }
}
