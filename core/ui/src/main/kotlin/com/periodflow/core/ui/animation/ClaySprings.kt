package com.periodflow.core.ui.animation

import androidx.compose.animation.core.*

object ClaySprings {
    val ClayPressSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val ClayPressSpringDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val ClayBounceSpring = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val ClayBounceSpringDp = spring<androidx.compose.ui.unit.Dp>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )

    val ClayTweenShort = tween<Float>(
        durationMillis = 150,
        easing = FastOutSlowInEasing
    )

    val ClayTweenArc = tween<Float>(
        durationMillis = 800,
        easing = LinearOutSlowInEasing
    )

    val ClayInfinitePulse = infiniteRepeatable<Float>(
        animation = tween(2000, easing = FastOutSlowInEasing),
        repeatMode = RepeatMode.Reverse
    )
}
