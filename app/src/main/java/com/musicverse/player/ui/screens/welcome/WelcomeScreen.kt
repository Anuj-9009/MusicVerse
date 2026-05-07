package com.musicverse.player.ui.screens.welcome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicverse.player.ui.components.GlassPillButton
import com.musicverse.player.ui.theme.EditorialHeavy
import com.musicverse.player.ui.theme.EditorialSerif
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseMotion
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.sin

/**
 * WelcomeScreen — The editorial-style login/onboarding screen.
 *
 * Features:
 *   - Slow-moving abstract gradient background
 *   - Massive Archivo Black branding text
 *   - Elegant serif tagline
 *   - "Connect Spotify" pill button with spring press
 *   - Staggered entrance animations
 */
@Composable
fun WelcomeScreen(
    onConnectSpotify: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showBranding by remember { mutableStateOf(false) }
    var showTagline by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showBranding = true
        delay(300)
        showTagline = true
        delay(300)
        showButton = true
    }

    // Animated gradient shift
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientShift"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicVerseColors.TrueBlack)
    ) {
        // ── Abstract Gradient Background ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(80.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0D47A1).copy(alpha = 0.4f * (0.5f + gradientOffset * 0.5f)),
                            MusicVerseColors.TrueBlack,
                            Color(0xFFFF5722).copy(alpha = 0.3f * (1f - gradientOffset * 0.5f)),
                            MusicVerseColors.TrueBlack,
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.weight(1f))

            // ── Giant Branding ────────────────────────────────────────────
            AnimatedVisibility(
                visible = showBranding,
                enter = fadeIn(
                    animationSpec = spring(
                        stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                    )
                ) + slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = spring(
                        dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                        stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                    )
                )
            ) {
                Text(
                    text = "MUSIC\nVERSE",
                    fontFamily = EditorialHeavy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 72.sp,
                    lineHeight = 68.sp,
                    letterSpacing = (-3).sp,
                    color = MusicVerseColors.TextPrimary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))

            // ── Elegant Tagline ───────────────────────────────────────────
            AnimatedVisibility(
                visible = showTagline,
                enter = fadeIn(
                    animationSpec = spring(
                        stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                    )
                ) + slideInVertically(
                    initialOffsetY = { 40 },
                    animationSpec = spring(
                        dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                        stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                    )
                )
            ) {
                Text(
                    text = "Discover every version\nof every song",
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Normal,
                    fontSize = 18.sp,
                    lineHeight = 26.sp,
                    color = MusicVerseColors.TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            // ── Connect Button ────────────────────────────────────────────
            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(
                    animationSpec = spring(
                        stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                    )
                ) + slideInVertically(
                    initialOffsetY = { 80 },
                    animationSpec = spring(
                        dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                        stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                    )
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    GlassPillButton(
                        text = "Connect Spotify",
                        onClick = onConnectSpotify,
                        accentColor = MusicVerseColors.Success // Spotify green
                    )

                    Spacer(Modifier.height(16.dp))

                    Text(
                        text = "We'll import your liked songs",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = MusicVerseColors.TextTertiary
                    )
                }
            }

            Spacer(Modifier.height(64.dp))
        }
    }
}
