package com.musicverse.player.ui.screens.login

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicverse.player.ui.theme.EditorialSerif
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.bouncingClickable
import kotlinx.coroutines.delay

/**
 * Login Screen — "Connect with Spotify / YouTube Music"
 *
 * Design: Dark background, centered branding, elegant serif title,
 * gold "Connect with Spotify" CTA, outlined "YouTube Music" secondary.
 * Matches the Stitch design exactly.
 */
@Composable
fun LoginScreen(
    onConnectSpotify: () -> Unit,
    onConnectYouTube: () -> Unit = {},
    onSkip: () -> Unit = {}
) {
    var showBranding by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        showBranding = true
        delay(400)
        showButtons = true
    }

    // Subtle warm gradient background
    val bgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1E1A16),
            MusicVerseColors.DeepCharcoal,
            MusicVerseColors.TrueBlack
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Branding ──
            AnimatedVisibility(
                visible = showBranding,
                enter = fadeIn(tween(800)) + slideInVertically(
                    initialOffsetY = { -40 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // App Name — Elegant italic serif
                    Text(
                        text = "MusicVerse",
                        fontFamily = EditorialSerif,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 42.sp,
                        color = MusicVerseColors.TextPrimary,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tagline
                    Text(
                        text = "Your universe of alternate\nsounds, unified.",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = MusicVerseColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))

            // ── Connect Buttons ──
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(600)) + slideInVertically(
                    initialOffsetY = { 60 },
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Connect with Spotify — Gold filled button
                    Button(
                        onClick = onConnectSpotify,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MusicVerseColors.Amber,
                            contentColor = MusicVerseColors.TextOnAccent
                        ),
                        shape = RoundedCornerShape(28.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 4.dp,
                            pressedElevation = 1.dp
                        )
                    ) {
                        Icon(
                            Icons.Rounded.MusicNote,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connect with Spotify",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp
                        )
                    }

                    // Connect with YouTube Music — Outlined dark button
                    OutlinedButton(
                        onClick = onConnectYouTube,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MusicVerseColors.TextPrimary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MusicVerseColors.Border
                        )
                    ) {
                        Icon(
                            Icons.Rounded.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = MusicVerseColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Connect with YouTube Music",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            color = MusicVerseColors.TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Skip for now (subtle) ──
            AnimatedVisibility(
                visible = showButtons,
                enter = fadeIn(tween(800, delayMillis = 300))
            ) {
                Text(
                    text = "Skip for now",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = MusicVerseColors.TextTertiary,
                    modifier = Modifier
                        .bouncingClickable { onSkip() }
                        .padding(8.dp)
                )
            }
        }
    }
}
