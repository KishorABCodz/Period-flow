package com.periodflow.feature.onboarding.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.periodflow.feature.onboarding.OnboardingRoute
import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

fun NavGraphBuilder.onboardingScreen(
    onComplete: () -> Unit
) {
    composable<OnboardingRoute> {
        OnboardingRoute(
            onComplete = onComplete
        )
    }
}
