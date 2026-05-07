package com.musicverse.player.ui.screens.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.ui.components.AudiophileBadge
import com.musicverse.player.ui.components.BentoCard
import com.musicverse.player.ui.components.BentoRow
import com.musicverse.player.ui.components.GradientScrim
import com.musicverse.player.ui.components.HybridDivider
import com.musicverse.player.ui.components.SectionHeader
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseSpacing
import kotlinx.coroutines.delay

/**
 * Track Detail Screen — Full metadata view for a single track.
 *
 * Features:
 *   - Large album art hero
 *   - Metadata grid (Artist, Album, Duration, ISRC)
 *   - Version discovery CTA (placeholder for Step 4)
 *   - Animated entrance
 */
@Composable
fun TrackDetailScreen(
    track: TrackEntity,
    onNavigateBack: () -> Unit,
    onPlayTrack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(80); visible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicVerseColors.DeepCharcoal)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(MusicVerseSpacing.L)
        ) {
            // ── Hero: Album Art ──
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(MaterialTheme.shapes.large)
                            .background(MusicVerseColors.Surface3),
                        contentAlignment = Alignment.Center
                    ) {
                        if (track.albumArtUrl != null) {
                            AsyncImage(
                                model = track.albumArtUrl,
                                contentDescription = track.album,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                Icons.Rounded.MusicNote,
                                contentDescription = null,
                                tint = MusicVerseColors.TextTertiary,
                                modifier = Modifier.size(80.dp)
                            )
                        }
                    }

                    // Gradient overlay at bottom
                    GradientScrim(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.BottomCenter)
                    )

                    // Back button
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .padding(top = MusicVerseSpacing.Giant, start = MusicVerseSpacing.S)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MusicVerseColors.TextPrimary
                        )
                    }
                }
            }

            // ── Track Title & Artist ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(initialOffsetY = { 30 })
                ) {
                    Column(modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)) {
                        Text(
                            text = track.title,
                            style = MaterialTheme.typography.displayMedium,
                            color = MusicVerseColors.TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(MusicVerseSpacing.XS))
                        Text(
                            text = track.artist,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MusicVerseColors.ElectricBlue
                        )
                    }
                }
            }

            // ── Metadata Grid ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(400)) + slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = spring(stiffness = Spring.StiffnessLow)
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)) {
                        BentoRow {
                            MetadataCard(
                                icon = Icons.Rounded.Album,
                                label = "ALBUM",
                                value = track.album,
                                accent = MusicVerseColors.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            MetadataCard(
                                icon = Icons.Rounded.AccessTime,
                                label = "DURATION",
                                value = formatDurationDetail(track.durationMs),
                                accent = MusicVerseColors.TextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(MusicVerseSpacing.BentoGap))
                        BentoRow {
                            MetadataCard(
                                icon = Icons.Rounded.Fingerprint,
                                label = "ISRC",
                                value = track.isrc ?: "Not available",
                                accent = if (track.isrc != null) MusicVerseColors.ElectricBlue else MusicVerseColors.TextTertiary,
                                modifier = Modifier.weight(1f)
                            )
                            MetadataCard(
                                icon = Icons.Rounded.Person,
                                label = "ARTIST",
                                value = track.artist,
                                accent = MusicVerseColors.SunsetOrange,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ── Version Discovery CTA ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(tween(500)) + slideInVertically(
                        initialOffsetY = { 50 },
                        animationSpec = spring(stiffness = Spring.StiffnessVeryLow)
                    )
                ) {
                    Column(modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)) {
                        SectionHeader(title = "Play Track")
                        Spacer(modifier = Modifier.height(MusicVerseSpacing.S))

                        Button(
                            onClick = onPlayTrack,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MusicVerseColors.ElectricBlue,
                                contentColor = MusicVerseColors.TextOnAccent
                            ),
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLAY NOW",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }
                }
            }

            // ── Spotify URI ──
            if (track.spotifyUri != null) {
                item {
                    Column(modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)) {
                        HybridDivider()
                        Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
                        Text(
                            text = track.spotifyUri,
                            style = MaterialTheme.typography.labelSmall,
                            color = MusicVerseColors.TextTertiary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MetadataCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier,
        surfaceColor = MusicVerseColors.Surface2,
        contentPadding = MusicVerseSpacing.M
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.XS))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MusicVerseColors.TextTertiary
                )
            }
            Spacer(modifier = Modifier.height(MusicVerseSpacing.S))
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = accent,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun formatDurationDetail(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "${minutes}m ${seconds}s"
}
