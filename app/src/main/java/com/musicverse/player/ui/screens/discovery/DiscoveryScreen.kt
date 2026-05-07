package com.musicverse.player.ui.screens.discovery

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicverse.player.data.local.VersionEntity
import com.musicverse.player.ui.components.AudiophileBadge
import com.musicverse.player.ui.components.BentoCard
import com.musicverse.player.ui.components.BentoRow
import com.musicverse.player.ui.components.SectionHeader
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseSpacing

/**
 * Discovery Screen — AI-powered version finding.
 *
 * Phases:
 *   1. IDLE → "Start Discovery" CTA with AI explanation
 *   2. DISCOVERING → Live progress with current track name
 *   3. COMPLETED → Success stats + top discovered versions
 *   4. ERROR → Error card with retry
 */
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val topVersions by viewModel.topVersions.collectAsState()
    val totalVersionCount by viewModel.totalVersionCount.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MusicVerseColors.DeepCharcoal)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = MusicVerseSpacing.Giant,
                bottom = 120.dp
            ),
            verticalArrangement = Arrangement.spacedBy(MusicVerseSpacing.L)
        ) {
            // ── Header ──
            item {
                Column(modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Back",
                                tint = MusicVerseColors.TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                        Column {
                            Text(
                                text = "AI DISCOVERY",
                                style = MaterialTheme.typography.displayMedium,
                                color = MusicVerseColors.SunsetOrange
                            )
                            Text(
                                text = "Gemini × YouTube",
                                style = MaterialTheme.typography.labelMedium,
                                color = MusicVerseColors.TextTertiary
                            )
                        }
                    }
                }
            }

            // ── Phase Content ──
            item {
                AnimatedContent(
                    targetState = uiState.phase,
                    transitionSpec = {
                        (fadeIn(tween(300)) + slideInVertically { 30 })
                            .togetherWith(fadeOut(tween(200)))
                    },
                    label = "discovery_phase"
                ) { phase ->
                    Column(
                        modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin),
                        verticalArrangement = Arrangement.spacedBy(MusicVerseSpacing.BentoGap)
                    ) {
                        when (phase) {
                            DiscoveryPhase.IDLE -> IdleCard(
                                existingVersionCount = totalVersionCount,
                                onStartDiscovery = viewModel::startDiscovery
                            )
                            DiscoveryPhase.DISCOVERING -> DiscoveringCard(
                                processedTracks = uiState.processedTracks,
                                totalTracks = uiState.totalTracks,
                                versionsFound = uiState.totalVersionsFound,
                                currentTrackName = uiState.currentTrackName
                            )
                            DiscoveryPhase.COMPLETED -> CompletedCard(
                                totalVersions = uiState.totalVersionsFound,
                                tracksProcessed = uiState.processedTracks,
                                onReDiscover = {
                                    viewModel.resetState()
                                    viewModel.startDiscovery()
                                }
                            )
                            DiscoveryPhase.ERROR -> ErrorCard(
                                message = uiState.errorMessage ?: "Unknown error",
                                onRetry = viewModel::startDiscovery,
                                onDismiss = viewModel::dismissError
                            )
                        }
                    }
                }
            }

            // ── Top Versions ──
            if (topVersions.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Top Versions",
                        subtitle = "$totalVersionCount versions discovered by AI"
                    )
                }

                itemsIndexed(
                    items = topVersions,
                    key = { _, v -> v.id }
                ) { index, version ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = index * 40)) +
                                slideInVertically(
                                    initialOffsetY = { 20 },
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                    ) {
                        VersionCard(
                            version = version,
                            onClick = { onNavigateToPlayer(version.trackId) },
                            modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)
                        )
                    }
                }
            }
        }
    }
}

// ── Phase Cards ──

@Composable
private fun IdleCard(
    existingVersionCount: Int,
    onStartDiscovery: () -> Unit
) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3
    ) {
        Column {
            Icon(
                Icons.Rounded.Psychology,
                contentDescription = null,
                tint = MusicVerseColors.SunsetOrange,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
            Text(
                text = "Discover Alternate Versions",
                style = MaterialTheme.typography.headlineMedium,
                color = MusicVerseColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.XS))
            Text(
                text = "Gemini AI will scan YouTube for live performances, acoustic sessions, covers, and remixes of every track in your library. Each version is scored on audio quality and authenticity.",
                style = MaterialTheme.typography.bodyMedium,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))

            // Feature pills
            Row(horizontalArrangement = Arrangement.spacedBy(MusicVerseSpacing.S)) {
                AudiophileBadge(
                    text = "Live",
                    backgroundColor = MusicVerseColors.SunsetOrangeGlow,
                    textColor = MusicVerseColors.SunsetOrange
                )
                AudiophileBadge(
                    text = "Acoustic",
                    backgroundColor = MusicVerseColors.BadgeLossless,
                    textColor = MusicVerseColors.ElectricBlue
                )
                AudiophileBadge(
                    text = "Cover",
                    backgroundColor = MusicVerseColors.SuccessGlow,
                    textColor = MusicVerseColors.Success
                )
                AudiophileBadge(
                    text = "Remix",
                    backgroundColor = MusicVerseColors.SunsetOrangeGlow,
                    textColor = MusicVerseColors.SunsetOrange
                )
            }

            if (existingVersionCount > 0) {
                Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
                AudiophileBadge(
                    text = "$existingVersionCount versions already discovered",
                    backgroundColor = MusicVerseColors.SuccessGlow,
                    textColor = MusicVerseColors.Success
                )
            }

            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
            Button(
                onClick = onStartDiscovery,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MusicVerseColors.SunsetOrange,
                    contentColor = MusicVerseColors.TextOnAccent
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AutoAwesome, null, Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                Text("START AI DISCOVERY", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun DiscoveringCard(
    processedTracks: Int,
    totalTracks: Int,
    versionsFound: Int,
    currentTrackName: String
) {
    val progress = if (totalTracks > 0) processedTracks.toFloat() / totalTracks else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "discovery_progress"
    )

    // Pulsing AI icon
    val infiniteTransition = rememberInfiniteTransition(label = "ai_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_pulse_alpha"
    )

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Psychology,
                    contentDescription = null,
                    tint = MusicVerseColors.SunsetOrange.copy(alpha = pulseAlpha),
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                Text(
                    text = "AI Discovering...",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MusicVerseColors.TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(MusicVerseSpacing.S))

            // Current track being analyzed
            if (currentTrackName.isNotBlank()) {
                Text(
                    text = currentTrackName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MusicVerseColors.SunsetOrange,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
            }

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MusicVerseColors.SunsetOrange,
                trackColor = MusicVerseColors.Surface1,
                strokeCap = StrokeCap.Round
            )

            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))

            // Stats row
            BentoRow {
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$processedTracks",
                        style = MaterialTheme.typography.displaySmall,
                        color = MusicVerseColors.SunsetOrange
                    )
                    Text(
                        text = "ANALYZED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MusicVerseColors.TextTertiary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (totalTracks > 0) "$totalTracks" else "...",
                        style = MaterialTheme.typography.displaySmall,
                        color = MusicVerseColors.TextSecondary
                    )
                    Text(
                        text = "TOTAL",
                        style = MaterialTheme.typography.labelSmall,
                        color = MusicVerseColors.TextTertiary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$versionsFound",
                        style = MaterialTheme.typography.displaySmall,
                        color = MusicVerseColors.Success
                    )
                    Text(
                        text = "FOUND",
                        style = MaterialTheme.typography.labelSmall,
                        color = MusicVerseColors.TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedCard(
    totalVersions: Int,
    tracksProcessed: Int,
    onReDiscover: () -> Unit
) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = MusicVerseColors.Success,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                Text(
                    text = "Discovery Complete",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MusicVerseColors.TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))

            BentoRow {
                BentoCard(
                    modifier = Modifier.weight(1f),
                    surfaceColor = MusicVerseColors.Surface2,
                    contentPadding = MusicVerseSpacing.M
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$tracksProcessed",
                            style = MaterialTheme.typography.displaySmall,
                            color = MusicVerseColors.ElectricBlue
                        )
                        Text(
                            text = "TRACKS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MusicVerseColors.TextTertiary
                        )
                    }
                }
                BentoCard(
                    modifier = Modifier.weight(1f),
                    surfaceColor = MusicVerseColors.Surface2,
                    contentPadding = MusicVerseSpacing.M
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$totalVersions",
                            style = MaterialTheme.typography.displaySmall,
                            color = MusicVerseColors.SunsetOrange
                        )
                        Text(
                            text = "VERSIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = MusicVerseColors.TextTertiary
                        )
                    }
                }
                BentoCard(
                    modifier = Modifier.weight(1f),
                    surfaceColor = MusicVerseColors.Surface2,
                    contentPadding = MusicVerseSpacing.M
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "✦",
                            style = MaterialTheme.typography.displaySmall,
                            color = MusicVerseColors.Success
                        )
                        Text(
                            text = "AI SCORED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MusicVerseColors.TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))

            OutlinedButton(
                onClick = onReDiscover,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MusicVerseColors.TextSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(MusicVerseSpacing.XS))
                Text("RE-DISCOVER", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3,
        borderColor = MusicVerseColors.Error.copy(alpha = 0.3f)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Error,
                    contentDescription = null,
                    tint = MusicVerseColors.Error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                Text(
                    text = "Discovery Failed",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MusicVerseColors.Error
                )
            }
            Spacer(modifier = Modifier.height(MusicVerseSpacing.S))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
            Row(horizontalArrangement = Arrangement.spacedBy(MusicVerseSpacing.BentoGap)) {
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MusicVerseColors.SunsetOrange,
                        contentColor = MusicVerseColors.TextOnAccent
                    ),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("RETRY", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MusicVerseColors.TextTertiary
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("DISMISS", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

// ── Version Card ──

@Composable
private fun VersionCard(
    version: VersionEntity,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface2,
        contentPadding = MusicVerseSpacing.CardPaddingSmall,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MusicVerseColors.Surface3),
                contentAlignment = Alignment.Center
            ) {
                if (version.thumbnailUrl != null) {
                    AsyncImage(
                        model = version.thumbnailUrl,
                        contentDescription = version.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = MusicVerseColors.TextTertiary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MusicVerseSpacing.M))

            // Version info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = version.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MusicVerseColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = version.channelName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MusicVerseColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (version.aiVibeReason.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = version.aiVibeReason,
                        style = MaterialTheme.typography.labelSmall,
                        color = MusicVerseColors.TextTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(MusicVerseSpacing.S))

            // Vibe Score + Badge
            Column(horizontalAlignment = Alignment.End) {
                // Vibe score circle
                VibeScoreBadge(score = version.aiVibeScore)
                Spacer(modifier = Modifier.height(4.dp))
                // Type badge
                AudiophileBadge(
                    text = version.type.uppercase(),
                    backgroundColor = when (version.type) {
                        "live" -> MusicVerseColors.SunsetOrangeGlow
                        "acoustic" -> MusicVerseColors.BadgeLossless
                        "cover" -> MusicVerseColors.SuccessGlow
                        else -> MusicVerseColors.SunsetOrangeGlow
                    },
                    textColor = when (version.type) {
                        "live" -> MusicVerseColors.SunsetOrange
                        "acoustic" -> MusicVerseColors.ElectricBlue
                        "cover" -> MusicVerseColors.Success
                        else -> MusicVerseColors.SunsetOrange
                    }
                )
            }
        }
    }
}

@Composable
private fun VibeScoreBadge(score: Int) {
    val scoreColor = when {
        score >= 80 -> MusicVerseColors.Success
        score >= 60 -> MusicVerseColors.ElectricBlue
        score >= 40 -> MusicVerseColors.SunsetOrange
        else -> MusicVerseColors.Error
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(scoreColor.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score",
            style = MaterialTheme.typography.labelMedium,
            color = scoreColor
        )
    }
}
