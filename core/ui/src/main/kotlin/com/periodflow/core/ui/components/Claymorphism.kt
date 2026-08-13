package com.periodflow.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme

/**
 * Minimal claymorphism modifier.
 * Achieves the soft, voluminous 3D clay look using:
 * 1. A soft outer drop shadow
 * 2. A large rounded corner shape
 * 3. A linear gradient border simulating inner light (top-left) and inner shadow (bottom-right)
 */
fun Modifier.claymorphism(
    backgroundColor: Color,
    shape: Shape = RoundedCornerShape(32.dp),
    elevation: Dp = 12.dp,
    lightHighlight: Color = Color.White.copy(alpha = 0.08f),
    darkShadow: Color = Color.Black.copy(alpha = 0.06f) // Softer, more diffused shadow
): Modifier = composed {
    this
        .shadow(
            elevation = elevation,
            shape = shape,
            spotColor = darkShadow,
            ambientColor = darkShadow
        )
        .clip(shape)
        .background(backgroundColor)
        .border(
            width = 3.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    lightHighlight,
                    Color.Transparent,
                    darkShadow
                )
            ),
            shape = shape
        )
}

@androidx.compose.runtime.Composable
fun ClayCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(32.dp),
    content: @androidx.compose.runtime.Composable () -> Unit
) {
    androidx.compose.foundation.layout.Box(
        modifier = modifier.claymorphism(backgroundColor = backgroundColor, shape = shape)
    ) {
        content()
    }
}

@androidx.compose.runtime.Composable
fun ClayButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    shape: Shape = RoundedCornerShape(32.dp),
    content: @androidx.compose.runtime.Composable androidx.compose.foundation.layout.BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "button_scale"
    )
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 12.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "button_elevation"
    )

    androidx.compose.foundation.layout.Box(
        modifier = modifier
            .scale(scale)
            .claymorphism(
                backgroundColor = backgroundColor,
                shape = shape,
                elevation = elevation,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Disable default ripple for physical clay feel
                onClick = onClick
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
        ) {
            content()
        }
    }
}

@androidx.compose.runtime.Composable
fun ClayChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    activeColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.primary,
    inactiveColor: Color = androidx.compose.material3.MaterialTheme.colorScheme.surface,
    shape: Shape = RoundedCornerShape(50), // Lozenge/pill shape for chips
    content: @androidx.compose.runtime.Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // 1. Scale animation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "chip_scale"
    )
    
    // 2. Elevation animation
    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else if (selected) 8.dp else 4.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "chip_elevation"
    )

    // 3. Color animation
    val backgroundColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) activeColor else inactiveColor,
        animationSpec = androidx.compose.animation.core.tween(200),
        label = "chip_color"
    )

    androidx.compose.foundation.layout.Row(
        modifier = modifier
            .scale(scale)
            .claymorphism(
                backgroundColor = backgroundColor,
                shape = shape,
                elevation = elevation,
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(
            LocalContentColor provides 
                if (selected) MaterialTheme.colorScheme.onPrimary 
                else MaterialTheme.colorScheme.onSurface
        ) {
            content()
        }
    }
}
