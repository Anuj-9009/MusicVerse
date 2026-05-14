package com.musicverse.player.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * MusicVerse Theme — Warm Muted Dark
 *
 * Strict dark-mode only. Premium, natural aesthetic.
 * Warm charcoal base with amber accents.
 */

private val WarmMutedColorScheme = darkColorScheme(
    // Primary accent — Warm Amber
    primary = MusicVerseColors.Amber,
    onPrimary = MusicVerseColors.TextOnAccent,
    primaryContainer = MusicVerseColors.AmberDim,
    onPrimaryContainer = MusicVerseColors.Amber,

    // Secondary accent — Sunset Orange
    secondary = MusicVerseColors.SunsetOrange,
    onSecondary = MusicVerseColors.TextOnAccent,
    secondaryContainer = MusicVerseColors.SunsetOrangeDim,
    onSecondaryContainer = MusicVerseColors.SunsetOrange,

    // Tertiary — Teal
    tertiary = MusicVerseColors.Teal,
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

    // Inverse
    inverseSurface = MusicVerseColors.TextPrimary,
    inverseOnSurface = MusicVerseColors.DeepCharcoal,
    inversePrimary = MusicVerseColors.AmberDim,

    // Scrim
    scrim = MusicVerseColors.TrueBlack,
)

@Composable
fun MusicVerseTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = WarmMutedColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = MusicVerseColors.TrueBlack.toArgb()
            window.navigationBarColor = MusicVerseColors.TrueBlack.toArgb()

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
