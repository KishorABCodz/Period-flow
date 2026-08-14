package com.periodflow.feature.home.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.periodflow.feature.home.HomeScreen
import kotlinx.serialization.Serializable

@Serializable
object HomeRoute

fun NavGraphBuilder.homeScreen(
    onNavigateToLog: (dateEpochDay: Long) -> Unit,
) {
    composable<HomeRoute> {
        HomeScreen(
            onNavigateToLog = onNavigateToLog,
        )
    }
}
