package com.hybridmusic.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.hybridmusic.player.R

/**
 * Hybrid Music Player — Cinematic Industrial Typography
 *
 * Three-tier font system:
 *   1. Clash Grotesk (via Google Fonts "Outfit" as closest match) → Headers
 *   2. Space Mono → Technical metadata, badges, bitrate
 *   3. Inter → Body text, high legibility
 *
 * All fonts loaded via Google Fonts provider for zero-APK-size overhead.
 */

private val googleFontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// ── Header Font: Outfit (Clash Grotesk analog — geometric, aggressive) ──
private val outfitFont = GoogleFont("Outfit")
val ClashGrotesk = FontFamily(
    Font(googleFont = outfitFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = outfitFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = outfitFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = outfitFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
)

// ── Technical Font: Space Mono (Monospaced, sci-fi, technical) ──
private val spaceMonoFont = GoogleFont("Space Mono")
val SpaceMono = FontFamily(
    Font(googleFont = spaceMonoFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = spaceMonoFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
)

// ── Body Font: Inter (High legibility, clean) ──
private val interFont = GoogleFont("Inter")
val InterFont = FontFamily(
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Bold),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Medium),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Normal),
    Font(googleFont = interFont, fontProvider = googleFontProvider, weight = FontWeight.Light),
)

/**
 * Material3 Typography built on our three-tier font system.
 */
val HybridTypography = Typography(

    // ── Display — Poster-style hero text ──
    displayLarge = TextStyle(
        fontFamily = ClashGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 40.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.5).sp,
        color = HybridColors.TextPrimary
    ),
    displayMedium = TextStyle(
        fontFamily = ClashGrotesk,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp,
        color = HybridColors.TextPrimary
    ),
    displaySmall = TextStyle(
        fontFamily = ClashGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
        color = HybridColors.TextPrimary
    ),

    // ── Headlines — Section headers ──
    headlineLarge = TextStyle(
        fontFamily = ClashGrotesk,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 28.sp,
        letterSpacing = (-0.25).sp,
        color = HybridColors.TextPrimary
    ),
    headlineMedium = TextStyle(
        fontFamily = ClashGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = HybridColors.TextPrimary
    ),
    headlineSmall = TextStyle(
        fontFamily = ClashGrotesk,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.sp,
        color = HybridColors.TextPrimary
    ),

    // ── Titles — Card headers, list items ──
    titleLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp,
        color = HybridColors.TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.15.sp,
        color = HybridColors.TextPrimary
    ),
    titleSmall = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
        color = HybridColors.TextSecondary
    ),

    // ── Body — Readable content ──
    bodyLarge = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
        color = HybridColors.TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
        color = HybridColors.TextSecondary
    ),
    bodySmall = TextStyle(
        fontFamily = InterFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
        color = HybridColors.TextTertiary
    ),

    // ── Labels — Badges, metadata, technical data ──
    labelLarge = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp,
        color = HybridColors.TextPrimary
    ),
    labelMedium = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp,
        color = HybridColors.TextSecondary
    ),
    labelSmall = TextStyle(
        fontFamily = SpaceMono,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp,
        color = HybridColors.TextTertiary
    ),
)
