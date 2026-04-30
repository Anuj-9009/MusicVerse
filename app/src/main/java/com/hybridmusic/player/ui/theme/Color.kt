package com.hybridmusic.player.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Hybrid Music Player — Industrial Minimalist Color System
 *
 * Inspired by Teenage Engineering & Nothing OS.
 * Strict dark-mode palette; pure white (#FFFFFF) is FORBIDDEN for text
 * to prevent halation on AMOLED displays.
 */
object HybridColors {

    // ── Base Canvas ──
    val DeepCharcoal = Color(0xFF121212)       // Primary background — reduces eye strain
    val TrueBlack = Color(0xFF000000)          // System bars, deepest depth layer

    // ── Surface Overlays (Elevation Depth) ──
    val Surface1 = Color(0xFF1A1A1A)           // Level 1 — bottom sheets, nav bars
    val Surface2 = Color(0xFF1E1E1E)           // Level 2 — cards, containers
    val Surface3 = Color(0xFF242424)           // Level 3 — elevated cards, menus
    val Surface4 = Color(0xFF2C2C2C)           // Level 4 — popovers, tooltips
    val SurfaceHover = Color(0xFF333333)       // Hover/pressed states

    // ── Primary Accent: Electric Blue ──
    val ElectricBlue = Color(0xFF00BFFF)       // Active states, play button, scrubber
    val ElectricBlueHover = Color(0xFF33CCFF)  // Lighter blue for hover
    val ElectricBlueDim = Color(0xFF006B8F)    // Dimmed for inactive indicators
    val ElectricBlueGlow = Color(0x3300BFFF)   // 20% alpha — glow/shadow underlays

    // ── Secondary Accent: Sunset Orange ──
    val SunsetOrange = Color(0xFFFF5722)       // LIVE indicators, errors, critical
    val SunsetOrangeHover = Color(0xFFFF7043)  // Lighter orange for hover
    val SunsetOrangeDim = Color(0xFF8F3013)    // Dimmed for inactive
    val SunsetOrangeGlow = Color(0x33FF5722)   // 20% alpha — glow

    // ── Text Hierarchy ──
    val TextPrimary = Color(0xFFE0E0E0)        // Body text — light gray, NOT pure white
    val TextSecondary = Color(0xFFB0B0B0)      // Subtitles, metadata
    val TextTertiary = Color(0xFF787878)        // Disabled, hints, timestamps
    val TextOnAccent = Color(0xFF0A0A0A)       // Text on blue/orange buttons

    // ── Semantic Colors ──
    val Success = Color(0xFF4CAF50)            // Verified, high AI score
    val Warning = Color(0xFFFFC107)            // Medium AI score, caution
    val Error = Color(0xFFEF5350)              // Failed, low quality

    // ── Audiophile Badge Colors (Semi-transparent pills) ──
    val BadgePristine = Color(0x3300E676)      // "Pristine Audio" — green glow
    val BadgeLive = Color(0x33FF5722)          // "Live Recording" — orange glow
    val BadgeAcoustic = Color(0x33FFD54F)      // "Acoustic" — warm amber glow
    val BadgeLossless = Color(0x3300BFFF)      // "Lossless" — electric blue glow
    val BadgeCrowd = Color(0x33E040FB)         // "Crowd Singalong" — purple glow

    // ── Gradients ──
    val GradientDark = listOf(DeepCharcoal, TrueBlack)
    val GradientBlueAccent = listOf(ElectricBlue, Color(0xFF0066CC))
    val GradientOrangeAccent = listOf(SunsetOrange, Color(0xFFCC3300))
    val GradientSurfaceFade = listOf(Color(0x00121212), DeepCharcoal) // Transparent to solid

    // ── Dividers & Borders ──
    val Divider = Color(0xFF2A2A2A)
    val Border = Color(0xFF363636)
    val BorderFocused = ElectricBlue
}
