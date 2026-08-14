package com.periodflow.feature.home.chat

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.periodflow.core.ai.voice.BloomEmotion

/**
 * A tiny animated blooming-flower avatar for Bloom.
 *
 * Five petals rotate slowly around a pulsing center. Petal + center colours
 * animate whenever the [emotion] changes — WARM/pink, THINKING/lavender,
 * EXCITED/coral, GENTLE/peach, CONCERNED/amber, NEUTRAL/rose.
 *
 * Kept pure-Canvas (no image assets) so it scales cleanly across densities.
 */
@Composable
fun BloomAvatar(
    emotion: BloomEmotion,
    modifier: Modifier = Modifier,
) {
    val (petalTarget, centerTarget) = colorPairFor(emotion)

    val petalColor by animateColorAsState(
        targetValue = petalTarget,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "petal-color",
    )
    val centerColor by animateColorAsState(
        targetValue = centerTarget,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "center-color",
    )

    val infinite = rememberInfiniteTransition(label = "bloom-avatar")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = rotationDurationFor(emotion), easing = LinearEasing),
        ),
        label = "rotation",
    )
    val centerPulse by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = pulseDurationFor(emotion), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val petalRadius = size.minDimension * 0.22f
        val orbitRadius = size.minDimension * 0.28f
        rotate(degrees = rotation, pivot = Offset(cx, cy)) {
            repeat(5) { i ->
                val angleRad = (i * 72f) * Math.PI.toFloat() / 180f
                val px = cx + orbitRadius * kotlin.math.cos(angleRad)
                val py = cy + orbitRadius * kotlin.math.sin(angleRad)
                drawCircle(color = petalColor, radius = petalRadius, center = Offset(px, py))
                // subtle inner glow
                drawCircle(
                    color = Color.White.copy(alpha = 0.35f),
                    radius = petalRadius * 0.4f,
                    center = Offset(px - petalRadius * 0.25f, py - petalRadius * 0.25f),
                )
            }
        }
        // Pulsing centre
        drawCircle(
            color = centerColor,
            radius = petalRadius * centerPulse,
            center = Offset(cx, cy),
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.5f),
            radius = petalRadius * 0.3f * centerPulse,
            center = Offset(cx - petalRadius * 0.15f, cy - petalRadius * 0.15f),
        )
    }
}

private fun colorPairFor(emotion: BloomEmotion): Pair<Color, Color> = when (emotion) {
    BloomEmotion.NEUTRAL   -> Color(0xFFE8B4B8) to Color(0xFFB07275) // rose / mocha
    BloomEmotion.WARM      -> Color(0xFFFFC1CC) to Color(0xFFE28D9C) // blossom pink / dusty rose
    BloomEmotion.THINKING  -> Color(0xFFCDB4DB) to Color(0xFF8E7BAA) // lavender / plum
    BloomEmotion.EXCITED   -> Color(0xFFFFB088) to Color(0xFFE07856) // coral / terracotta
    BloomEmotion.GENTLE    -> Color(0xFFFFD5B8) to Color(0xFFD79A72) // peach / caramel
    BloomEmotion.CONCERNED -> Color(0xFFF4C67E) to Color(0xFFB27F35) // amber / warm gold
}

private fun rotationDurationFor(emotion: BloomEmotion): Int = when (emotion) {
    BloomEmotion.EXCITED -> 5_000
    BloomEmotion.CONCERNED, BloomEmotion.GENTLE -> 14_000
    BloomEmotion.THINKING -> 8_000
    else -> 10_000
}

private fun pulseDurationFor(emotion: BloomEmotion): Int = when (emotion) {
    BloomEmotion.EXCITED -> 700
    BloomEmotion.CONCERNED, BloomEmotion.GENTLE -> 1_800
    BloomEmotion.THINKING -> 900
    else -> 1_200
}
