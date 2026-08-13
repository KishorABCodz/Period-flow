package com.periodflow.core.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DustyRose,
    onPrimary = DarkSurface,
    primaryContainer = DarkSurfaceVariant,
    onPrimaryContainer = DarkTextPrimary,
    secondary = MutedTerracotta,
    onSecondary = DarkSurface,
    secondaryContainer = DarkSurfaceVariant,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = SoftSage,
    onTertiary = DarkSurface,
    tertiaryContainer = DarkSurfaceVariant,
    onTertiaryContainer = DarkTextPrimary,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    error = ErrorTerracotta,
    onError = DarkTextPrimary,
)

private val LightColorScheme = lightColorScheme(
    primary = DustyRose,
    onPrimary = LightSurface,
    primaryContainer = LightSurfaceVariant,
    onPrimaryContainer = LightTextPrimary,
    secondary = MutedTerracotta,
    onSecondary = LightSurface,
    secondaryContainer = LightSurfaceVariant,
    onSecondaryContainer = LightTextPrimary,
    tertiary = SoftSage,
    onTertiary = LightSurface,
    tertiaryContainer = LightSurfaceVariant,
    onTertiaryContainer = LightTextPrimary,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    error = ErrorTerracotta,
    onError = LightSurface,
)

@Composable
fun PeriodFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default to maintain the curated aesthetic
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = PeriodFlowTypography,
        shapes = PeriodFlowShapes,
        content = content,
    )
}
