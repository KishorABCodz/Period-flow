package com.periodflow.feature.stats.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.periodflow.feature.stats.StatsScreen
import kotlinx.serialization.Serializable

@Serializable
object StatsRoute

fun NavGraphBuilder.statsScreen(
    onNavigateToHealthInsights: () -> Unit,
) {
    composable<StatsRoute> {
        StatsScreen(
            onNavigateToHealthInsights = onNavigateToHealthInsights
        )
    }
}
