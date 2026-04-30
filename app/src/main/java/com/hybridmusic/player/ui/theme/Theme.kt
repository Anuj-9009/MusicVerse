package com.hybridmusic.player.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Hybrid Music Player Theme
 *
 * Strict dark-mode only — Industrial Minimalist / Premium Audiophile.
 * No light theme variant. This is by design.
 */

private val HybridDarkColorScheme = darkColorScheme(
    // Primary accent — Electric Blue
    primary = HybridColors.ElectricBlue,
    onPrimary = HybridColors.TextOnAccent,
    primaryContainer = HybridColors.ElectricBlueDim,
    onPrimaryContainer = HybridColors.ElectricBlue,

    // Secondary accent — Sunset Orange
    secondary = HybridColors.SunsetOrange,
    onSecondary = HybridColors.TextOnAccent,
    secondaryContainer = HybridColors.SunsetOrangeDim,
    onSecondaryContainer = HybridColors.SunsetOrange,

    // Tertiary — Success green
    tertiary = HybridColors.Success,
    onTertiary = HybridColors.TextOnAccent,

    // Background & Surface
    background = HybridColors.DeepCharcoal,
    onBackground = HybridColors.TextPrimary,
    surface = HybridColors.Surface1,
    onSurface = HybridColors.TextPrimary,
    surfaceVariant = HybridColors.Surface2,
    onSurfaceVariant = HybridColors.TextSecondary,
    surfaceContainerLowest = HybridColors.TrueBlack,
    surfaceContainerLow = HybridColors.Surface1,
    surfaceContainer = HybridColors.Surface2,
    surfaceContainerHigh = HybridColors.Surface3,
    surfaceContainerHighest = HybridColors.Surface4,

    // Outline
    outline = HybridColors.Border,
    outlineVariant = HybridColors.Divider,

    // Error
    error = HybridColors.Error,
    onError = HybridColors.TextOnAccent,

    // Inverse (for snackbars, etc.)
    inverseSurface = HybridColors.TextPrimary,
    inverseOnSurface = HybridColors.DeepCharcoal,
    inversePrimary = HybridColors.ElectricBlueDim,

    // Scrim
    scrim = HybridColors.TrueBlack,
)

@Composable
fun HybridMusicTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = HybridDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar and nav bar fully transparent for edge-to-edge
            window.statusBarColor = HybridColors.TrueBlack.toArgb()
            window.navigationBarColor = HybridColors.TrueBlack.toArgb()

            // Light icons on dark background
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = false
            insetsController.isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HybridTypography,
        shapes = HybridShapes,
        content = content
    )
}
