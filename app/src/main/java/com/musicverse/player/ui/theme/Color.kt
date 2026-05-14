package com.musicverse.player.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MusicVerse — "Warm Muted" Color System
 *
 * A sophisticated warm dark palette with amber/orange accents.
 * Inspired by the Stitch design: natural, non-distracting, premium feel.
 */
object MusicVerseColors {

    // ── Base Canvas ──
    val DeepCharcoal = Color(0xFF1A1A1A)       // Primary background
    val TrueBlack = Color(0xFF111111)           // System bars, deepest depth
    val WarmBlack = Color(0xFF141210)           // Slightly warm black for gradients

    // ── Surface Overlays (Elevation Depth) ──
    val Surface1 = Color(0xFF1E1C1A)           // Level 1 — nav bars, sheets
    val Surface2 = Color(0xFF252220)           // Level 2 — cards, containers
    val Surface3 = Color(0xFF2D2926)           // Level 3 — elevated cards
    val Surface4 = Color(0xFF363230)           // Level 4 — popovers
    val SurfaceHover = Color(0xFF3E3A37)       // Hover/pressed states

    // ── Primary Accent: Warm Amber / Gold ──
    val Amber = Color(0xFFD4A844)              // Primary CTA (Connect with Spotify button)
    val AmberLight = Color(0xFFE0BE6A)         // Hover/lighter variant
    val AmberDim = Color(0xFF8A6C2E)           // Dimmed/inactive
    val AmberGlow = Color(0x33D4A844)          // 20% alpha glow

    // ── Secondary Accent: Sunset Orange ──
    val SunsetOrange = Color(0xFFE86833)       // Active states, play button, tags
    val SunsetOrangeHover = Color(0xFFFF7A45)  // Lighter orange for hover
    val SunsetOrangeDim = Color(0xFF8F3013)    // Dimmed for inactive
    val SunsetOrangeGlow = Color(0x33E86833)   // 20% alpha glow

    // ── Tertiary: Teal / Cyan ──
    val Teal = Color(0xFF4DB6AC)               // YouTube Music, secondary actions
    val TealGlow = Color(0x334DB6AC)           // 20% alpha

    // ── Text Hierarchy ──
    val TextPrimary = Color(0xFFE8E4E0)        // Body text — warm off-white
    val TextSecondary = Color(0xFF9E9792)       // Subtitles, metadata
    val TextTertiary = Color(0xFF6B6460)        // Disabled, hints, timestamps
    val TextOnAccent = Color(0xFF1A1410)        // Text on amber/orange buttons

    // ── Semantic Colors ──
    val Success = Color(0xFF1DB954)             // Spotify green for "connected"
    val SuccessGlow = Color(0x331DB954)         // 20% alpha
    val Warning = Color(0xFFFFC107)             // Caution
    val Error = Color(0xFFEF5350)               // Failed states

    // ── Badge Colors ──
    val BadgeLive = Color(0x33E86833)           // "Live" — orange glow
    val BadgeAcoustic = Color(0x33D4A844)       // "Acoustic" — amber glow
    val BadgeLossless = Color(0x334DB6AC)       // "Lossless" — teal glow
    val BadgeSessions = Color(0x339C27B0)       // "Sessions" — purple glow
    val BadgeSpotify = Color(0x331DB954)        // Spotify tag
    val BadgeYouTube = Color(0x33FF0000)        // YouTube tag

    // ── Tag Colors (Solid for pills) ──
    val TagSpotify = Color(0xFF1DB954)
    val TagYouTube = Color(0xFFFF4444)
    val TagAcoustic = Color(0xFFD4A844)
    val TagLive = Color(0xFFE86833)
    val TagSessions = Color(0xFF9C27B0)

    // ── Glass / Frosted surfaces ──
    val GlassWhite = Color(0x14FFFFFF)          // 8% white overlay
    val GlassBorder = Color(0x22FFFFFF)         // 13% white border

    // ── Gradients ──
    val GradientDark = listOf(DeepCharcoal, TrueBlack)
    val GradientWarm = listOf(Color(0xFF2A2420), Color(0xFF1A1A1A))
    val GradientAmber = listOf(Amber, Color(0xFFBF8E30))
    val GradientSurfaceFade = listOf(Color(0x001A1A1A), DeepCharcoal)

    // ── Dividers & Borders ──
    val Divider = Color(0xFF2A2725)
    val Border = Color(0xFF353130)
    val BorderFocused = Amber

    // ── Legacy aliases for backward compatibility ──
    val ElectricBlue = Amber        // Remap old blue to amber
    val ElectricBlueHover = AmberLight
    val ElectricBlueDim = AmberDim
    val ElectricBlueGlow = AmberGlow
}
