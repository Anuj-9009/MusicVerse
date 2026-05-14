package com.musicverse.player.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicverse.player.ui.components.EditorialSectionHeader
import com.musicverse.player.ui.components.FilterChipRow
import com.musicverse.player.ui.components.GlassPillButton
import com.musicverse.player.ui.components.HeroGridCard
import com.musicverse.player.ui.components.SpotifyCard
import com.musicverse.player.ui.components.TrackRow
import com.musicverse.player.ui.theme.EditorialCondensed
import com.musicverse.player.ui.theme.EditorialHeavy
import com.musicverse.player.ui.theme.EditorialSerif
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseMotion
import com.musicverse.player.ui.theme.SpaceMono
import kotlinx.coroutines.delay

/**
 * Home Screen — "Editorial Spotify" Layout
 *
 * Features:
 *   - Dynamic greeting in elegant serif
 *   - Auth-aware "Get Started" CTA (hides after login)
 *   - Filter pill chips (All, Imported, Discovered) — FUNCTIONAL
 *   - 2x2 Hero Grid for quick actions
 *   - Live stats from Room DB
 *   - Recent tracks list with play action
 *   - Staggered spring entrance animations
 */
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    sharedTransitionScope: SharedTransitionScope,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    onNavigateToImport: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToDiscovery: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    onNavigateToSearch: () -> Unit = {},
    onNavigateToPlayer: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    val chips = listOf("All", "Imported", "Discovered")

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MusicVerseColors.DeepCharcoal)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(top = 56.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Greeting Header ──────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = spring(stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS)
                    ) + slideInVertically(
                        initialOffsetY = { -30 },
                        animationSpec = spring(
                            dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                            stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = getGreeting(),
                                fontFamily = EditorialSerif,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = MusicVerseColors.TextPrimary
                            )
                            if (uiState.userDisplayName != null) {
                                Text(
                                    text = uiState.userDisplayName!!,
                                    fontFamily = InterFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    color = MusicVerseColors.TextSecondary
                                )
                            }
                        }
                        Row {
                            IconButton(onClick = onNavigateToSearch) {
                                Icon(
                                    Icons.Rounded.Search,
                                    contentDescription = "Search",
                                    tint = MusicVerseColors.TextPrimary
                                )
                            }
                            IconButton(onClick = onNavigateToSettings) {
                                Icon(
                                    Icons.Rounded.Settings,
                                    contentDescription = "Settings",
                                    tint = MusicVerseColors.TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            // ── Filter Chips ─────────────────────────────────────────────
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(
                        animationSpec = spring(stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS)
                    ) + slideInVertically(
                        initialOffsetY = { 30 },
                        animationSpec = spring(
                            dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                            stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                        )
                    )
                ) {
                    FilterChipRow(
                        chips = chips,
                        selectedIndex = uiState.selectedFilter,
                        onChipSelected = { viewModel.setFilter(it) }
                    )
                }
            }

            // ── Hero Grid (2x2) — shown when "All" filter ───────────────
            if (uiState.selectedFilter == 0) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(
                            animationSpec = spring(stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS)
                        ) + slideInVertically(
                            initialOffsetY = { 50 },
                            animationSpec = spring(
                                dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                                stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Simplified Quick Actions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HeroGridCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Library",
                                    icon = Icons.Rounded.LibraryMusic,
                                    iconTint = MusicVerseColors.ElectricBlue,
                                    onClick = onNavigateToLibrary
                                )
                                HeroGridCard(
                                    modifier = Modifier.weight(1f),
                                    title = "Discover",
                                    icon = Icons.Rounded.GraphicEq,
                                    iconTint = MusicVerseColors.SunsetOrange,
                                    onClick = onNavigateToDiscovery
                                )
                            }
                        }
                    }
                }


            }

            // ── "Imported" filter — show recent tracks ───────────────────
            if (uiState.selectedFilter == 1 && uiState.recentTracks.isNotEmpty()) {
                item {
                    EditorialSectionHeader(title = "RECENTLY IMPORTED")
                }
                items(
                    items = uiState.recentTracks,
                    key = { it.id }
                ) { track ->
                    TrackRow(
                        title = track.title,
                        artist = track.artist,
                        albumArtUrl = track.albumArtUrl,
                        onClick = { onNavigateToPlayer(track.id) }
                    )
                }
            }

            // ── "Discovered" filter — show top versions ──────────────────
            if (uiState.selectedFilter == 2 && uiState.topVersions.isNotEmpty()) {
                item {
                    EditorialSectionHeader(title = "AI DISCOVERED VERSIONS")
                }
                items(
                    items = uiState.topVersions,
                    key = { it.id }
                ) { version ->
                    TrackRow(
                        title = version.title,
                        artist = "${version.channelName} · Vibe: ${version.aiVibeScore}",
                        albumArtUrl = version.thumbnailUrl,
                        onClick = { onNavigateToPlayer(version.trackId) }
                    )
                }
            }

            // ── Get Started CTA — ONLY when NOT authenticated ───────────
            if (!uiState.isAuthenticated) {
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(
                            animationSpec = spring(stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS)
                        ) + slideInVertically(
                            initialOffsetY = { 90 },
                            animationSpec = spring(
                                dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                                stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                            )
                        )
                    ) {
                        SpotifyCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClick = onNavigateToImport,
                            surfaceColor = MusicVerseColors.Surface3,
                            cornerRadius = 12.dp,
                            contentPadding = 20.dp
                        ) {
                            Column {
                                Text(
                                    text = "Connect to Spotify",
                                    style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MusicVerseColors.TextPrimary
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Connect your Spotify account to import your library and discover live, acoustic, and cover versions of your favorite tracks.",
                                    fontFamily = InterFont,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp,
                                    color = MusicVerseColors.TextSecondary
                                )
                                Spacer(Modifier.height(16.dp))
                                GlassPillButton(
                                    text = "Connect Spotify",
                                    onClick = onNavigateToImport,
                                    accentColor = MusicVerseColors.Success
                                )
                            }
                        }
                    }
                }
            }

            // ── Recent Tracks section (on "All" filter, when authenticated) ──
            if (uiState.selectedFilter == 0 && uiState.isAuthenticated && uiState.recentTracks.isNotEmpty()) {
                item {
                    EditorialSectionHeader(title = "RECENT TRACKS")
                }
                items(
                    items = uiState.recentTracks.take(10),
                    key = { "recent_${it.id}" }
                ) { track ->
                    TrackRow(
                        title = track.title,
                        artist = track.artist,
                        albumArtUrl = track.albumArtUrl,
                        trackId = track.id,
                        sharedTransitionScope = sharedTransitionScope,
                        animatedVisibilityScope = animatedVisibilityScope,
                        onClick = { onNavigateToPlayer(track.id) }
                    )
                }
            }
        }
    }
}

// ── Stat Pill ─────────────────────────────────────────────────────────────────

@Composable
private fun StatPill(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    SpotifyCard(
        modifier = modifier,
        surfaceColor = MusicVerseColors.Surface3,
        cornerRadius = 12.dp,
        contentPadding = 16.dp
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontFamily = EditorialHeavy,
                fontWeight = FontWeight.Normal,
                fontSize = 28.sp,
                color = accentColor
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = label,
                fontFamily = SpaceMono,
                fontWeight = FontWeight.Normal,
                fontSize = 10.sp,
                letterSpacing = 1.sp,
                color = MusicVerseColors.TextTertiary
            )
        }
    }
}

// ── Greeting Helper ───────────────────────────────────────────────────────────

private fun getGreeting(): String {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    return when {
        hour < 5 -> "Late Night Session"
        hour < 12 -> "Good Morning"
        hour < 17 -> "Good Afternoon"
        hour < 21 -> "Good Evening"
        else -> "Night Mode"
    }
}
