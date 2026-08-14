package com.periodflow.feature.log.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.periodflow.feature.log.LogScreen
import kotlinx.serialization.Serializable

@Serializable
data class LogRoute(val dateEpochDay: Long)

fun NavGraphBuilder.logScreen(
    onNavigateBack: () -> Unit,
) {
    composable<LogRoute> {
        LogScreen(
            onNavigateBack = onNavigateBack,
        )
    }
}

fun NavController.navigateToLog(dateEpochDay: Long) {
    navigate(LogRoute(dateEpochDay = dateEpochDay))
}
