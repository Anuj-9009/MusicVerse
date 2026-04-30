package com.hybridmusic.player.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Bento Grid 2.0 Shape System
 *
 * Smooth, rounded rectangular cards inspired by TE and Nothing OS.
 * All radii follow the 8-point grid system.
 */
val HybridShapes = Shapes(
    // Pill buttons, small badges
    extraSmall = RoundedCornerShape(8.dp),

    // Segmented controls, chips, toggle pills
    small = RoundedCornerShape(12.dp),

    // Standard Bento cards, containers
    medium = RoundedCornerShape(16.dp),

    // Large feature cards, album art containers
    large = RoundedCornerShape(20.dp),

    // Bottom sheets, full-width overlays
    extraLarge = RoundedCornerShape(28.dp),
)

/**
 * Spacing & sizing constants — strict 8-point baseline grid.
 */
object HybridSpacing {
    val XXS = 2.dp
    val XS = 4.dp
    val S = 8.dp
    val M = 12.dp
    val L = 16.dp
    val XL = 20.dp
    val XXL = 24.dp
    val XXXL = 32.dp
    val Giant = 48.dp
    val Huge = 64.dp

    // Bento grid gaps
    val BentoGap = 12.dp
    val BentoGapLarge = 16.dp

    // Card internal padding
    val CardPadding = 16.dp
    val CardPaddingSmall = 12.dp

    // Screen edge margins
    val ScreenMargin = 20.dp
    val ScreenMarginSmall = 16.dp
}

/**
 * Elevation tokens — subtle depth without muddy shadows.
 */
object HybridElevation {
    val Level0 = 0.dp
    val Level1 = 1.dp
    val Level2 = 2.dp
    val Level3 = 4.dp
    val Level4 = 8.dp
}
