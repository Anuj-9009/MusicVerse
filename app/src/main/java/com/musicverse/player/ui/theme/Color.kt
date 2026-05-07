package com.musicverse.player.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MusicVerse 3.0 — "Editorial Spotify" Color System
 *
 * A refined dark palette inspired by the latest Spotify, optimized for OLED displays.
 * Pure white (#FFFFFF) is FORBIDDEN for text to prevent halation.
 */
object MusicVerseColors {

    // ── Base Canvas ──
    val DeepCharcoal = Color(0xFF121212)       // Primary background (Spotify standard)
    val TrueBlack = Color(0xFF000000)          // System bars, deepest depth layer

    // ── Surface Overlays (Elevation Depth) ──
    val Surface1 = Color(0xFF1A1A1A)           // Level 1 — bottom sheets, nav bars
    val Surface2 = Color(0xFF1E1E1E)           // Level 2 — cards, containers
    val Surface3 = Color(0xFF282828)           // Level 3 — elevated cards, menus
    val Surface4 = Color(0xFF333333)           // Level 4 — popovers, tooltips
    val SurfaceHover = Color(0xFF3A3A3A)       // Hover/pressed states

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
    val TextPrimary = Color(0xFFEAEAEA)        // Body text — warm off-white
    val TextSecondary = Color(0xFFA0A0A0)      // Subtitles, metadata
    val TextTertiary = Color(0xFF6A6A6A)       // Disabled, hints, timestamps
    val TextOnAccent = Color(0xFF0A0A0A)       // Text on blue/orange buttons

    // ── Semantic Colors ──
    val Success = Color(0xFF1DB954)            // Spotify green for "connected" states
    val SuccessGlow = Color(0x331DB954)        // 20% alpha — success glow
    val Warning = Color(0xFFFFC107)            // Medium AI score, caution
    val Error = Color(0xFFEF5350)              // Failed, low quality

    // ── Audiophile Badge Colors (Semi-transparent pills) ──
    val BadgePristine = Color(0x3300E676)      // "Pristine Audio" — green glow
    val BadgeLive = Color(0x33FF5722)          // "Live Recording" — orange glow
    val BadgeAcoustic = Color(0x33FFD54F)      // "Acoustic" — warm amber glow
    val BadgeLossless = Color(0x3300BFFF)      // "Lossless" — electric blue glow
    val BadgeCrowd = Color(0x33E040FB)         // "Crowd Singalong" — purple glow

    // ── Glass / Frosted surfaces ──
    val GlassWhite = Color(0x1AFFFFFF)         // 10% white overlay for glass effects
    val GlassBorder = Color(0x33FFFFFF)        // 20% white for glass card borders

    // ── Gradients ──
    val GradientDark = listOf(DeepCharcoal, TrueBlack)
    val GradientBlueAccent = listOf(ElectricBlue, Color(0xFF0066CC))
    val GradientOrangeAccent = listOf(SunsetOrange, Color(0xFFCC3300))
    val GradientSurfaceFade = listOf(Color(0x00121212), DeepCharcoal)

    // ── Dividers & Borders ──
    val Divider = Color(0xFF2A2A2A)
    val Border = Color(0xFF363636)
    val BorderFocused = ElectricBlue
}
