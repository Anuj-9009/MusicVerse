package com.musicverse.player.ui.screens.import_

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
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
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.LinkOff
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicverse.player.ui.components.AudiophileBadge
import com.musicverse.player.ui.components.BentoCard
import com.musicverse.player.ui.components.BentoRow
import com.musicverse.player.ui.components.HybridDivider
import com.musicverse.player.ui.components.SectionHeader
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseSpacing
import com.musicverse.player.ui.theme.SpaceMono

/**
 * Smart Importer Screen — Phase-driven UI
 *
 * Phases:
 *   1. NOT_CONNECTED → Spotify login card
 *   2. READY → "Start Import" button
 *   3. IMPORTING → Live progress with animated counters
 *   4. COMPLETED → Success stats + recently imported tracks
 *   5. ERROR → Error card with retry
 */
@Composable
fun ImportScreen(
    viewModel: ImportViewModel,
    onNavigateBack: () -> Unit,
    onLaunchSpotifyAuth: (android.content.Intent) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val importedTracks by viewModel.importedTracks.collectAsState()
    val trackCount by viewModel.trackCount.collectAsState()

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
                                text = "SMART IMPORTER",
                                style = MaterialTheme.typography.displayMedium,
                                color = MusicVerseColors.ElectricBlue
                            )
                            Text(
                                text = "Spotify → Local Cache",
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
                    label = "phase_transition"
                ) { phase ->
                    Column(
                        modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin),
                        verticalArrangement = Arrangement.spacedBy(MusicVerseSpacing.BentoGap)
                    ) {
                        when (phase) {
                            ImportPhase.NOT_CONNECTED -> SpotifyConnectCard(
                                onConnect = { onLaunchSpotifyAuth(viewModel.getSpotifyAuthIntent()) }
                            )
                            ImportPhase.READY -> ReadyToImportCard(
                                userName = uiState.userDisplayName,
                                existingCount = trackCount,
                                onStartImport = viewModel::startImport,
                                onDisconnect = viewModel::disconnect
                            )
                            ImportPhase.IMPORTING -> ImportingCard(
                                imported = uiState.importedCount,
                                total = uiState.totalCount
                            )
                            ImportPhase.COMPLETED -> CompletedCard(
                                totalImported = uiState.importedCount,
                                onReImport = viewModel::reImport
                            )
                            ImportPhase.ERROR -> ErrorCard(
                                message = uiState.errorMessage ?: "Unknown error",
                                onRetry = viewModel::startImport,
                                onDismiss = viewModel::dismissError
                            )
                        }
                    }
                }
            }

            // ── Recently Imported Tracks ──
            if (importedTracks.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Imported Tracks",
                        subtitle = "$trackCount tracks cached locally"
                    )
                }

                itemsIndexed(
                    items = importedTracks.take(50),
                    key = { _, track -> track.id }
                ) { index, track ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(tween(300, delayMillis = index * 30)) +
                                slideInVertically(
                                    initialOffsetY = { 20 },
                                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                                )
                    ) {
                        ImportedTrackRow(
                            title = track.title,
                            artist = track.artist,
                            album = track.album,
                            albumArtUrl = track.albumArtUrl,
                            isrc = track.isrc,
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
private fun SpotifyConnectCard(onConnect: () -> Unit) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3
    ) {
        Column {
            Icon(
                Icons.Rounded.SyncAlt,
                contentDescription = null,
                tint = MusicVerseColors.ElectricBlue,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
            Text(
                text = "Connect Spotify",
                style = MaterialTheme.typography.headlineMedium,
                color = MusicVerseColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.XS))
            Text(
                text = "Sign in with your Spotify account to import your Liked Songs library. We'll extract ISRC metadata for precise version matching.",
                style = MaterialTheme.typography.bodyMedium,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MusicVerseColors.ElectricBlue,
                    contentColor = MusicVerseColors.TextOnAccent
                ),
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Rounded.SyncAlt,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                Text(
                    text = "CONNECT SPOTIFY",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun ReadyToImportCard(
    userName: String?,
    existingCount: Int,
    onStartImport: () -> Unit,
    onDisconnect: () -> Unit
) {
    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MusicVerseColors.Success)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                Text(
                    text = "Connected${userName?.let { " as $it" } ?: ""}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MusicVerseColors.Success
                )
            }
            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))

            if (existingCount > 0) {
                AudiophileBadge(
                    text = "$existingCount tracks already cached",
                    backgroundColor = MusicVerseColors.BadgeLossless,
                    textColor = MusicVerseColors.ElectricBlue
                )
                Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
            }

            Text(
                text = "Ready to Import",
                style = MaterialTheme.typography.headlineMedium,
                color = MusicVerseColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.XS))
            Text(
                text = "Fetch your entire Liked Songs library with ISRC codes for precision version matching.",
                style = MaterialTheme.typography.bodyMedium,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))

            Row(horizontalArrangement = Arrangement.spacedBy(MusicVerseSpacing.BentoGap)) {
                Button(
                    onClick = onStartImport,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MusicVerseColors.ElectricBlue,
                        contentColor = MusicVerseColors.TextOnAccent
                    ),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.CloudDownload, null, Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(MusicVerseSpacing.XS))
                    Text("IMPORT", style = MaterialTheme.typography.labelLarge)
                }
                OutlinedButton(
                    onClick = onDisconnect,
                    shape = MaterialTheme.shapes.small,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MusicVerseColors.TextTertiary
                    )
                ) {
                    Icon(Icons.Rounded.LinkOff, null, Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
private fun ImportingCard(imported: Int, total: Int) {
    val progress = if (total > 0) imported.toFloat() / total else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "import_progress"
    )

    // Spinning icon
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sync_rotation"
    )

    BentoCard(
        modifier = Modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.SyncAlt,
                    contentDescription = null,
                    tint = MusicVerseColors.ElectricBlue,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotation)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))
                Text(
                    text = "Importing...",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MusicVerseColors.TextPrimary
                )
            }
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))

            // Progress bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MusicVerseColors.ElectricBlue,
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
                        text = "$imported",
                        style = MaterialTheme.typography.displaySmall,
                        color = MusicVerseColors.ElectricBlue
                    )
                    Text(
                        text = "IMPORTED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MusicVerseColors.TextTertiary
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (total > 0) "$total" else "...",
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
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.displaySmall,
                        color = MusicVerseColors.Success
                    )
                    Text(
                        text = "PROGRESS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MusicVerseColors.TextTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun CompletedCard(totalImported: Int, onReImport: () -> Unit) {
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
                    text = "Import Complete",
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
                            text = "$totalImported",
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
                            text = "✓",
                            style = MaterialTheme.typography.displaySmall,
                            color = MusicVerseColors.Success
                        )
                        Text(
                            text = "ISRC TAGGED",
                            style = MaterialTheme.typography.labelSmall,
                            color = MusicVerseColors.TextTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))

            OutlinedButton(
                onClick = onReImport,
                shape = MaterialTheme.shapes.small,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MusicVerseColors.TextSecondary
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Refresh, null, Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(MusicVerseSpacing.XS))
                Text("RE-IMPORT", style = MaterialTheme.typography.labelMedium)
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
                    text = "Import Failed",
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
                        containerColor = MusicVerseColors.ElectricBlue,
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

// ── Track Row ──

@Composable
private fun ImportedTrackRow(
    title: String,
    artist: String,
    album: String,
    albumArtUrl: String?,
    isrc: String?,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface2,
        contentPadding = MusicVerseSpacing.CardPaddingSmall
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Album art
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(MusicVerseColors.Surface3),
                contentAlignment = Alignment.Center
            ) {
                if (albumArtUrl != null) {
                    AsyncImage(
                        model = albumArtUrl,
                        contentDescription = album,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Icon(
                        Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = MusicVerseColors.TextTertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MusicVerseSpacing.M))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MusicVerseColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MusicVerseColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // ISRC badge
            if (isrc != null) {
                AudiophileBadge(
                    text = "ISRC",
                    backgroundColor = MusicVerseColors.BadgeLossless,
                    textColor = MusicVerseColors.ElectricBlue
                )
            }
        }
    }
}
