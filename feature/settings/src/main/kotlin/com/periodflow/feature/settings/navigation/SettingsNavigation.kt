package com.periodflow.feature.settings.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.periodflow.feature.settings.SettingsScreen
import kotlinx.serialization.Serializable

@Serializable
object SettingsRoute

fun NavGraphBuilder.settingsScreen() {
    composable<SettingsRoute> {
        SettingsScreen()
    }
}
