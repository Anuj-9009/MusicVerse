package com.musicverse.player.ui.theme

import androidx.compose.animation.core.Spring

/**
 * MusicVerse 3.0 — Apple-Tier Animation Presets
 *
 * Centralized spring physics constants used across the entire app.
 * These create the "very smooth" interruptible animations inspired by iOS.
 *
 * Key principle: NO linear tweens for interactive elements.
 * Every user-facing animation uses physics-based springs that are
 * interruptible, momentum-aware, and feel natural.
 */
object MusicVerseMotion {

    // ── Press / Release (Card taps, button presses) ──
    // Quick and responsive, slight bounce on release
    const val PRESS_SCALE = 0.95f
    const val PRESS_DAMPING = Spring.DampingRatioMediumBouncy
    const val PRESS_STIFFNESS = Spring.StiffnessHigh

    // ── Page Transitions (Screen enter/exit) ──
    // Slow, cinematic, no bounce — feels like sliding glass panels
    const val TRANSITION_DAMPING = Spring.DampingRatioNoBouncy
    const val TRANSITION_STIFFNESS = Spring.StiffnessLow

    // ── Content Entrance (Staggered item appearance) ──
    // Slightly bouncy, medium speed — items "settle" into place
    const val ENTRANCE_DAMPING = Spring.DampingRatioLowBouncy
    const val ENTRANCE_STIFFNESS = Spring.StiffnessVeryLow

    // ── Scrub / Seek (Thumb drag on scrub bar) ──
    // Very responsive, slight bounce when released
    const val SCRUB_DAMPING = Spring.DampingRatioLowBouncy
    const val SCRUB_STIFFNESS = Spring.StiffnessMedium

    // ── Swipe Dismiss (Player screen swipe down) ──
    // Medium damping, low stiffness — heavy, deliberate feel
    const val DISMISS_DAMPING = Spring.DampingRatioMediumBouncy
    const val DISMISS_STIFFNESS = Spring.StiffnessLow

    // ── Micro-pulse (Music-reactive elements) ──
    // Very subtle, high stiffness — tight pulse
    const val PULSE_DAMPING = Spring.DampingRatioNoBouncy
    const val PULSE_STIFFNESS = Spring.StiffnessHigh

    // ── Stagger Delays (ms between item animations) ──
    const val STAGGER_DELAY_MS = 50L
    const val STAGGER_DELAY_FAST_MS = 30L
}
