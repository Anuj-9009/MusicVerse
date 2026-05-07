package com.musicverse.player.ui.theme

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.Typography
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.unit.sp
import com.musicverse.player.R

/**
 * MusicVerse 3.0 — "Editorial Spotify" Typography
 *
 * Five-tier font system inspired by brutalist editorial design:
 *
 *   1. Archivo Black   → Massive hero headers, track titles (Tusker Grotesk analog)
 *   2. Bebas Neue      → Ultra-condensed section labels (Calcio Super Cond analog)
 *   3. Playfair Display → Elegant serif for artist names (Moonic Serif analog)
 *   4. Space Mono       → Metadata, badges, technical labels
 *   5. Inter            → Clean body text, UI labels (Spotify base)
 *
 * All fonts loaded from local res/font for offline resilience and zero latency.
 */

// ── Tier 1: Heavy Block — Archivo Black (Tusker Grotesk / Varien analog) ──
val EditorialHeavy = FontFamily(
    Font(R.font.archivo_black, weight = FontWeight.Normal),
)

// ── Tier 2: Ultra Condensed — Bebas Neue (Calcio Super Condensed / Salva analog) ──
val EditorialCondensed = FontFamily(
    Font(R.font.bebas_neue, weight = FontWeight.Normal),
)

// ── Tier 3: Elegant Serif — Playfair Display (Moonic Serif / Glow Night analog) ──
val EditorialSerif = FontFamily(
    Font(R.font.playfair_display, weight = FontWeight.Normal),
    Font(R.font.playfair_display, weight = FontWeight.Medium),
    Font(R.font.playfair_display, weight = FontWeight.SemiBold),
    Font(R.font.playfair_display, weight = FontWeight.Bold),
    Font(
        R.font.playfair_display,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    ),
)

// ── Tier 4: Monospace Metadata — Space Mono ──
val SpaceMono = FontFamily(
    Font(R.font.space_mono, weight = FontWeight.Normal),
    Font(R.font.space_mono, weight = FontWeight.Bold),
)

// ── Tier 5: Base UI — Inter (Spotify clean readability) ──
val InterFont = FontFamily(
    Font(R.font.inter, weight = FontWeight.Light),
    Font(R.font.inter, weight = FontWeight.Normal),
    Font(R.font.inter, weight = FontWeight.Medium),
    Font(R.font.inter, weight = FontWeight.SemiBold),
    Font(R.font.inter, weight = FontWeight.Bold),
)

// ── Keep legacy references working ──
val ClashGrotesk = EditorialHeavy

/**
 * Material3 Typography — "Editorial Spotify" System
 *
 * Display:   Archivo Black (massive, brutalist hero text)
 * Headlines: Bebas Neue (condensed, aggressive section headers)
 * Titles:    Inter SemiBold (card headers, list items)
 * Body:      Inter (readable content)
 * Labels:    Space Mono (badges, metadata, technical)
 */
val HybridTypography = Typography(

    // ── Display — Poster-style hero text (Archivo Black) ──
    displayLarge = TextStyle(
        fontFamily = EditorialHeavy,
        fontWeight = FontWeight.Normal, // Archivo Black is inherently ultra-bold
        fontSize = 48.sp,
        lineHeight = 48.sp,
        letterSpacing = (-2).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = EditorialHeavy,
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 38.sp,
        letterSpacing = (-1).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = EditorialHeavy,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp,
    ),

    // ── Headlines — Ultra-condensed section headers (Bebas Neue) ──
    headlineLarge = TextStyle(
        fontFamily = EditorialCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 34.sp,
        letterSpacing = 2.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = EditorialCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp,
        lineHeight = 26.sp,
        letterSpacing = 1.5.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = EditorialCondensed,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 22.sp,
        letterSpacing = 1.sp,
    ),

    // ── Titles — Card headers, list items (Inter) ──
    titleLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),

    // ── Body — Readable content (Inter) ──
    bodyLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),

    // ── Labels — Badges, metadata (Space Mono for editorial feel) ──
    labelLarge = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
    ),
)

// ── Touch Physics: bouncingClickable Modifier ─────────────────────────────────

/**
 * Custom bouncingClickable modifier — spring-animated scale + opacity
 * on press for that premium Apple-tier feel.
 *
 * Scale: 0.94f on press, 1f on release
 * Opacity: 0.7f on press, 1f on release
 */
fun Modifier.bouncingClickable(
    enabled: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = 400f
        ),
        label = "bounce_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (isPressed) 0.7f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "bounce_alpha"
    )

    this
        .scale(scale)
        .alpha(alpha)
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            enabled = enabled
        ) {
            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        }
}
