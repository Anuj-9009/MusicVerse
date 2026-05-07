package com.musicverse.player.ui.theme

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
    primary = MusicVerseColors.ElectricBlue,
    onPrimary = MusicVerseColors.TextOnAccent,
    primaryContainer = MusicVerseColors.ElectricBlueDim,
    onPrimaryContainer = MusicVerseColors.ElectricBlue,

    // Secondary accent — Sunset Orange
    secondary = MusicVerseColors.SunsetOrange,
    onSecondary = MusicVerseColors.TextOnAccent,
    secondaryContainer = MusicVerseColors.SunsetOrangeDim,
    onSecondaryContainer = MusicVerseColors.SunsetOrange,

    // Tertiary — Success green
    tertiary = MusicVerseColors.Success,
    onTertiary = MusicVerseColors.TextOnAccent,

    // Background & Surface
    background = MusicVerseColors.DeepCharcoal,
    onBackground = MusicVerseColors.TextPrimary,
    surface = MusicVerseColors.Surface1,
    onSurface = MusicVerseColors.TextPrimary,
    surfaceVariant = MusicVerseColors.Surface2,
    onSurfaceVariant = MusicVerseColors.TextSecondary,
    surfaceContainerLowest = MusicVerseColors.TrueBlack,
    surfaceContainerLow = MusicVerseColors.Surface1,
    surfaceContainer = MusicVerseColors.Surface2,
    surfaceContainerHigh = MusicVerseColors.Surface3,
    surfaceContainerHighest = MusicVerseColors.Surface4,

    // Outline
    outline = MusicVerseColors.Border,
    outlineVariant = MusicVerseColors.Divider,

    // Error
    error = MusicVerseColors.Error,
    onError = MusicVerseColors.TextOnAccent,

    // Inverse (for snackbars, etc.)
    inverseSurface = MusicVerseColors.TextPrimary,
    inverseOnSurface = MusicVerseColors.DeepCharcoal,
    inversePrimary = MusicVerseColors.ElectricBlueDim,

    // Scrim
    scrim = MusicVerseColors.TrueBlack,
)

@Composable
fun MusicVerseTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = HybridDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Make status bar and nav bar fully transparent for edge-to-edge
            window.statusBarColor = MusicVerseColors.TrueBlack.toArgb()
            window.navigationBarColor = MusicVerseColors.TrueBlack.toArgb()

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
