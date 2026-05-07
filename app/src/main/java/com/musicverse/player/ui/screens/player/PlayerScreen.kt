package com.musicverse.player.ui.screens.player

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musicverse.player.audio.PlaybackSource
import com.musicverse.player.ui.components.FluidVisualizer
import com.musicverse.player.ui.components.ScrubBar
import com.musicverse.player.ui.components.rememberAlbumPalette
import com.musicverse.player.ui.theme.EditorialCondensed
import com.musicverse.player.ui.theme.EditorialHeavy
import com.musicverse.player.ui.theme.EditorialSerif
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors

/**
 * PlayerScreen — The Immersive Now Playing View (Step 6 Final)
 *
 * Premium Features:
 *  - Dynamic album-art palette extraction (background adapts to artwork)
 *  - Blurred album art background with gradient scrim
 *  - FluidVisualizer procedural waveform
 *  - ScrubBar with spring-physics thumb and elapsed/remaining time
 *  - Play/Pause controls with animated accent color
 *  - The "Version Toggle" — bento-style switch between Spotify & YouTube
 *  - Source badge with pulsing live indicator
 */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // ── Dynamic Palette from Album Art ────────────────────────────────────
    val palette = rememberAlbumPalette(uiState.albumArtUrl)

    // Animated accent: YouTube = vibrant album color, Spotify = palette vibrant
    val accentColor by animateColorAsState(
        targetValue = if (uiState.currentSource == PlaybackSource.YOUTUBE)
            palette.vibrant else palette.vibrant,
        animationSpec = tween(800),
        label = "accentColor"
    )

    val sourceAccent by animateColorAsState(
        targetValue = if (uiState.currentSource == PlaybackSource.YOUTUBE)
            MusicVerseColors.SunsetOrange else MusicVerseColors.ElectricBlue,
        animationSpec = tween(600),
        label = "sourceAccent"
    )

    // Animated background color derived from palette
    val bgDominant by animateColorAsState(
        targetValue = palette.mutedDark,
        animationSpec = tween(1200),
        label = "bgDominant"
    )

    val bgVibrantDark by animateColorAsState(
        targetValue = palette.vibrantDark,
        animationSpec = tween(1200),
        label = "bgVibrantDark"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MusicVerseColors.TrueBlack)
    ) {
        // ── Blurred Album Art Background ──────────────────────────────────
        uiState.albumArtUrl?.let { url ->
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(60.dp)
                    .scale(1.2f) // Scale up to hide blur edge artifacts
            )
        }

        // ── Dynamic Gradient Overlay ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            bgDominant.copy(alpha = 0.5f),
                            bgVibrantDark.copy(alpha = 0.7f),
                            MusicVerseColors.TrueBlack.copy(alpha = 0.9f),
                            MusicVerseColors.TrueBlack
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ── Top Bar ───────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Back",
                        tint = MusicVerseColors.TextPrimary
                    )
                }
                Spacer(Modifier.weight(1f))
                SourceBadge(source = uiState.currentSource, accentColor = sourceAccent)
            }

            Spacer(Modifier.weight(1f))

            // ── Album Art (Centered, Rounded) ─────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                uiState.albumArtUrl?.let { url ->
                    // Glow shadow behind album art
                    Box(
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(accentColor.copy(alpha = 0.15f))
                            .blur(30.dp)
                    )
                    AsyncImage(
                        model = url,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(280.dp)
                            .clip(RoundedCornerShape(24.dp))
                    )
                }
            }

            Spacer(Modifier.weight(0.5f))

            // ── Fluid Visualizer ──────────────────────────────────────────
            FluidVisualizer(
                isPlaying = uiState.isPlaying,
                accentColor = accentColor,
                secondaryColor = palette.muted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            )

            Spacer(Modifier.height(16.dp))

            // ── Track Info + Controls ─────────────────────────────────────
            Column(modifier = Modifier.padding(bottom = 48.dp)) {

                // Track Title & Artist
                // Track title — massive editorial heavy (Archivo Black)
                Text(
                    text = uiState.trackTitle.ifEmpty { "Loading..." },
                    fontFamily = EditorialHeavy,
                    fontWeight = FontWeight.Normal,
                    fontSize = 32.sp,
                    lineHeight = 34.sp,
                    letterSpacing = (-1).sp,
                    color = MusicVerseColors.TextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                // Artist name — elegant serif italic (Playfair Display)
                Text(
                    text = uiState.trackArtist,
                    fontFamily = EditorialSerif,
                    fontWeight = FontWeight.Normal,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                    fontSize = 16.sp,
                    color = MusicVerseColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(24.dp))

                // ── Scrub Bar ─────────────────────────────────────────────
                ScrubBar(
                    currentPositionMs = uiState.currentPositionMs,
                    durationMs = uiState.durationMs,
                    accentColor = accentColor,
                    onSeek = viewModel::onSeek
                )
                Spacer(Modifier.height(24.dp))

                // ── Playback Controls ─────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = viewModel::onSkipPrevious) {
                        Icon(
                            Icons.Rounded.SkipPrevious, "Previous",
                            tint = MusicVerseColors.TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Play/Pause button with spring scale
                    val playScale by animateFloatAsState(
                        targetValue = if (uiState.isPlaying) 1f else 1.05f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        ),
                        label = "playScale"
                    )
                    Box(
                        modifier = Modifier
                            .scale(playScale)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = viewModel::onPlayPause) {
                            Icon(
                                imageVector = if (uiState.isPlaying)
                                    Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = if (uiState.isPlaying) "Pause" else "Play",
                                tint = MusicVerseColors.TrueBlack,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    IconButton(onClick = viewModel::onSkipNext) {
                        Icon(
                            Icons.Rounded.SkipNext, "Next",
                            tint = MusicVerseColors.TextSecondary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── The Version Toggle ────────────────────────────────────
                VersionSwitchToggle(
                    currentSource = uiState.currentSource,
                    ghostReady = uiState.ghostReady,
                    topVersionTitle = uiState.topVersion?.title,
                    topVersionScore = uiState.topVersion?.aiVibeScore,
                    accentColor = sourceAccent,
                    onToggle = viewModel::onSwitchSource
                )
            }
        }
    }
}

// ── Version Switch Toggle ─────────────────────────────────────────────────────

@Composable
fun VersionSwitchToggle(
    currentSource: PlaybackSource,
    ghostReady: Boolean,
    topVersionTitle: String?,
    topVersionScore: Int?,
    accentColor: Color,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val glowScale by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glowScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MusicVerseColors.Surface2.copy(alpha = 0.8f))
            .then(if (ghostReady) Modifier.scale(glowScale) else Modifier)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = if (ghostReady) accentColor else MusicVerseColors.TextTertiary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = if (ghostReady) "AI VERSION READY" else "SCANNING FOR VERSIONS...",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = if (ghostReady) accentColor else MusicVerseColors.TextTertiary
                )
            }

            topVersionTitle?.let { title ->
                Spacer(Modifier.height(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    color = MusicVerseColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                topVersionScore?.let { score ->
                    Text(
                        text = "Vibe Score: $score / 100",
                        fontSize = 11.sp,
                        color = MusicVerseColors.TextSecondary
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // The Toggle Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (ghostReady) accentColor.copy(alpha = 0.1f)
                        else MusicVerseColors.Surface3
                    )
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SourceTab(
                    label = "SPOTIFY",
                    isActive = currentSource == PlaybackSource.SPOTIFY,
                    activeColor = MusicVerseColors.ElectricBlue,
                    onClick = { if (currentSource == PlaybackSource.YOUTUBE) onToggle() }
                )
                SourceTab(
                    label = "YOUTUBE",
                    isActive = currentSource == PlaybackSource.YOUTUBE,
                    activeColor = MusicVerseColors.SunsetOrange,
                    enabled = ghostReady,
                    onClick = { if (currentSource == PlaybackSource.SPOTIFY) onToggle() }
                )
            }
        }
    }
}

@Composable
fun SourceTab(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) activeColor else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = enabled
            ) { onClick() }
            .padding(horizontal = 24.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            letterSpacing = 1.5.sp,
            color = when {
                isActive -> MusicVerseColors.TrueBlack
                enabled -> MusicVerseColors.TextSecondary
                else -> MusicVerseColors.TextTertiary
            }
        )
    }
}

@Composable
fun SourceBadge(source: PlaybackSource, accentColor: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse),
        label = "dotAlpha"
    )

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MusicVerseColors.Surface2.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = dotAlpha))
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (source == PlaybackSource.SPOTIFY) "SPOTIFY" else "YOUTUBE",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
            color = accentColor
        )
    }
}
