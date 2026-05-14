@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.musicverse.player.ui.screens.player

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.SpaceMono
import com.musicverse.player.ui.theme.bouncingClickable

/**
 * Now Playing Screen — Warm Muted Design
 *
 * Matches Stitch design:
 *   - Large album art with warm gradient background
 *   - Amber/orange waveform-style progress bar
 *   - Track info below art
 *   - Alternate Versions section
 *   - Warm amber play button, minimal controls
 */
@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    val albumArtUrl = state.albumArtUrl ?: ""
    val title = state.trackTitle
    val artist = state.trackArtist
    val isPlaying = state.isPlaying
    val currentPositionMs = state.currentPositionMs
    val durationMs = state.durationMs
    val progress = if (durationMs > 0) currentPositionMs.toFloat() / durationMs else 0f

    // Dynamic palette colors
    var dominantColor by remember { mutableStateOf(Color(0xFF2A2420)) }
    var mutedColor by remember { mutableStateOf(Color(0xFF1A1A1A)) }

    // Warm cinematic gradient
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(dominantColor, mutedColor, MusicVerseColors.TrueBlack)
    )

    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .statusBarsPadding()
        ) {
            // ── Top Bar ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Close",
                        tint = MusicVerseColors.TextPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Now Playing",
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MusicVerseColors.TextSecondary
                )

                IconButton(onClick = { /* More options */ }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "More",
                        tint = MusicVerseColors.TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Album Art ──
            val imageScale by animateFloatAsState(
                targetValue = if (isPlaying) 1.0f else 0.92f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "AlbumArtScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                AsyncImage(
                    model = albumArtUrl,
                    contentDescription = "Album Art",
                    contentScale = ContentScale.Crop,
                    onSuccess = { result ->
                        val bitmap = (result.result.drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
                        bitmap?.let {
                            androidx.palette.graphics.Palette.from(it).generate { palette ->
                                palette?.darkMutedSwatch?.rgb?.let { color ->
                                    dominantColor = Color(color)
                                }
                                palette?.mutedSwatch?.rgb?.let { color ->
                                    mutedColor = Color(color).copy(alpha = 0.6f)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .graphicsLayer {
                            scaleX = imageScale
                            scaleY = imageScale
                            shadowElevation = 20f
                            shape = RoundedCornerShape(20.dp)
                            clip = true
                        }
                        .clip(RoundedCornerShape(20.dp))
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Track Info ──
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
            ) {
                Text(
                    text = title,
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MusicVerseColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = artist,
                    fontFamily = InterFont,
                    fontWeight = FontWeight.Normal,
                    fontSize = 15.sp,
                    color = MusicVerseColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Waveform Progress Bar (stylized as amber bars) ──
            WaveformProgressBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .height(48.dp),
                progress = progress,
                activeColor = MusicVerseColors.SunsetOrange,
                inactiveColor = MusicVerseColors.Surface3
            )

            // Time labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatTime(currentPositionMs),
                    fontFamily = SpaceMono,
                    fontSize = 11.sp,
                    color = MusicVerseColors.TextTertiary
                )
                Text(
                    text = if (durationMs > 0) "-${formatTime(durationMs - currentPositionMs)}" else "--:--",
                    fontFamily = SpaceMono,
                    fontSize = 11.sp,
                    color = MusicVerseColors.TextTertiary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ── Playback Controls ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { /* Shuffle */ }) {
                    Icon(
                        Icons.Filled.Shuffle,
                        contentDescription = "Shuffle",
                        tint = MusicVerseColors.TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.onSkipPrevious() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipPrevious,
                        contentDescription = "Previous",
                        tint = MusicVerseColors.TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play/Pause — Amber circle
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MusicVerseColors.SunsetOrange)
                        .bouncingClickable { viewModel.onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                IconButton(
                    onClick = { viewModel.onSkipNext() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Filled.SkipNext,
                        contentDescription = "Next",
                        tint = MusicVerseColors.TextPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                IconButton(onClick = { /* Repeat */ }) {
                    Icon(
                        Icons.Filled.Repeat,
                        contentDescription = "Repeat",
                        tint = MusicVerseColors.TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Alternate Versions Section ──
            AlternateVersionsSection(
                versions = state.versions,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

/**
 * Waveform-style progress bar matching the design.
 */
@Composable
private fun WaveformProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    activeColor: Color,
    inactiveColor: Color
) {
    val barCount = 40
    val activeBarCount = (barCount * progress).toInt()

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        repeat(barCount) { index ->
            val isActive = index < activeBarCount
            // Pseudo-random heights for waveform look
            val heightFraction = remember {
                val seed = (index * 7 + 3) % 10
                when {
                    seed < 2 -> 0.3f
                    seed < 4 -> 0.5f
                    seed < 6 -> 0.7f
                    seed < 8 -> 0.9f
                    else -> 0.6f
                }
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .padding(top = ((1f - heightFraction) * 48).dp)
                    .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp))
                    .background(if (isActive) activeColor else inactiveColor)
            )
        }
    }
}

/**
 * Alternate Versions section matching the Stitch design.
 */
@Composable
private fun AlternateVersionsSection(
    versions: List<VersionUiModel>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ALTERNATE VERSIONS",
                fontFamily = SpaceMono,
                fontSize = 11.sp,
                letterSpacing = 1.5.sp,
                fontWeight = FontWeight.Bold,
                color = MusicVerseColors.TextTertiary
            )
            IconButton(onClick = { /* Settings */ }, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = "Settings",
                    tint = MusicVerseColors.TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (versions.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MusicVerseColors.Surface2.copy(alpha = 0.5f))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No alternate versions discovered yet",
                    fontFamily = InterFont,
                    fontSize = 13.sp,
                    color = MusicVerseColors.TextTertiary
                )
            }
        } else {
            versions.forEach { version ->
                AlternateVersionRow(version = version)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

/**
 * Single alternate version row — matching the Stitch "Live at Red Rocks" / "Acoustic Session" style.
 */
@Composable
private fun AlternateVersionRow(
    version: VersionUiModel,
    modifier: Modifier = Modifier
) {
    val sourceColor = when {
        version.source.contains("spotify", ignoreCase = true) -> MusicVerseColors.Success
        version.source.contains("youtube", ignoreCase = true) -> MusicVerseColors.Teal
        else -> MusicVerseColors.TextTertiary
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MusicVerseColors.Surface2.copy(alpha = 0.6f))
            .clickable { /* Play alternate */ }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Version indicator dot
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(sourceColor)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = version.title,
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MusicVerseColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = version.source.uppercase(),
                fontFamily = SpaceMono,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp,
                color = sourceColor
            )
        }

        Icon(
            Icons.Rounded.ChevronRight,
            contentDescription = "Play",
            tint = MusicVerseColors.TextTertiary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Format milliseconds to MM:SS display.
 */
private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
