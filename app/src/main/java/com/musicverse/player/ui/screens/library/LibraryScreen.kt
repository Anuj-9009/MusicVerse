package com.musicverse.player.ui.screens.library

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.SyncAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.ui.components.AudiophileBadge
import com.musicverse.player.ui.components.BentoCard
import com.musicverse.player.ui.components.BentoRow
import com.musicverse.player.ui.components.HybridDivider
import com.musicverse.player.ui.components.SectionHeader
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseSpacing

/**
 * Library Screen — Browse, search, and filter imported tracks.
 *
 * Features:
 *   - Expandable search bar with 300ms debounce
 *   - Sort pill chips (Recent, A-Z, Z-A, Artist)
 *   - Bento-style track cards with album art
 *   - Empty state with import CTA
 *   - Stats header with track/artist counts
 */
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToImport: () -> Unit,
    onTrackClick: (TrackEntity) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val tracks by viewModel.tracks.collectAsState()
    val totalCount by viewModel.totalTrackCount.collectAsState()
    val filteredCount by viewModel.filteredCount.collectAsState()

    val uniqueArtists = remember(tracks) { tracks.map { it.artist }.distinct().size }

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
            verticalArrangement = Arrangement.spacedBy(MusicVerseSpacing.S)
        ) {
            // ── Header ──
            item {
                Column(modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    text = "LIBRARY",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = MusicVerseColors.ElectricBlue
                                )
                                Text(
                                    text = "$totalCount tracks imported",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MusicVerseColors.TextTertiary
                                )
                            }
                        }
                        IconButton(onClick = viewModel::toggleSearch) {
                            Icon(
                                if (uiState.isSearchActive) Icons.Rounded.SearchOff else Icons.Rounded.Search,
                                contentDescription = "Search",
                                tint = if (uiState.isSearchActive) MusicVerseColors.ElectricBlue else MusicVerseColors.TextSecondary
                            )
                        }
                    }
                }
            }

            // ── Search Bar (Expandable) ──
            item {
                AnimatedVisibility(
                    visible = uiState.isSearchActive,
                    enter = expandVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeIn(),
                    exit = shrinkVertically(spring(stiffness = Spring.StiffnessMedium)) + fadeOut()
                ) {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::updateSearchQuery,
                        onClear = viewModel::clearSearch,
                        resultCount = filteredCount,
                        modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)
                    )
                }
            }

            // ── Sort Chips ──
            item {
                SortChipRow(
                    selectedSort = uiState.sortOption,
                    onSortSelected = viewModel::updateSortOption
                )
            }

            // ── Stats Row ──
            if (totalCount > 0) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = MusicVerseSpacing.ScreenMargin),
                        horizontalArrangement = Arrangement.spacedBy(MusicVerseSpacing.BentoGap)
                    ) {
                        StatMiniCard(
                            value = "$totalCount",
                            label = "TRACKS",
                            accent = MusicVerseColors.ElectricBlue,
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniCard(
                            value = "$uniqueArtists",
                            label = "ARTISTS",
                            accent = MusicVerseColors.SunsetOrange,
                            modifier = Modifier.weight(1f)
                        )
                        StatMiniCard(
                            value = if (uiState.searchQuery.isNotBlank()) "$filteredCount" else "—",
                            label = "MATCHED",
                            accent = MusicVerseColors.Success,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // ── Track List or Empty State ──
            if (tracks.isEmpty() && totalCount == 0) {
                item {
                    EmptyLibraryCard(
                        onNavigateToImport = onNavigateToImport,
                        modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)
                    )
                }
            } else if (tracks.isEmpty() && uiState.searchQuery.isNotBlank()) {
                item {
                    NoResultsCard(
                        query = uiState.searchQuery,
                        modifier = Modifier.padding(horizontal = MusicVerseSpacing.ScreenMargin)
                    )
                }
            } else {
                item {
                    SectionHeader(
                        title = if (uiState.searchQuery.isNotBlank()) "Results" else "All Tracks",
                        subtitle = "$filteredCount tracks"
                    )
                }

                itemsIndexed(
                    items = tracks,
                    key = { _, track -> track.id }
                ) { index, track ->
                    LibraryTrackCard(
                        track = track,
                        onClick = { onTrackClick(track) },
                        modifier = Modifier
                            .padding(horizontal = MusicVerseSpacing.ScreenMargin)
                            .animateItem()
                    )
                }
            }
        }
    }
}

// ── Search Bar ──

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    resultCount: Int,
    modifier: Modifier = Modifier
) {
    val focusRequester = remember { FocusRequester() }

    BentoCard(
        modifier = modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3,
        contentPadding = MusicVerseSpacing.CardPaddingSmall
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MusicVerseColors.ElectricBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(MusicVerseSpacing.S))

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(
                        color = MusicVerseColors.TextPrimary
                    ),
                    cursorBrush = SolidColor(MusicVerseColors.ElectricBlue),
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    decorationBox = { innerTextField ->
                        Box {
                            if (query.isEmpty()) {
                                Text(
                                    text = "Search tracks, artists, albums...",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MusicVerseColors.TextTertiary
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                if (query.isNotEmpty()) {
                    IconButton(onClick = onClear, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Rounded.Clear,
                            contentDescription = "Clear",
                            tint = MusicVerseColors.TextTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (query.isNotBlank()) {
                Spacer(modifier = Modifier.height(MusicVerseSpacing.XS))
                Text(
                    text = "$resultCount results",
                    style = MaterialTheme.typography.labelSmall,
                    color = MusicVerseColors.ElectricBlue
                )
            }
        }
    }
}

// ── Sort Chips ──

@Composable
private fun SortChipRow(
    selectedSort: LibrarySortOption,
    onSortSelected: (LibrarySortOption) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = MusicVerseSpacing.ScreenMargin),
        horizontalArrangement = Arrangement.spacedBy(MusicVerseSpacing.S)
    ) {
        items(LibrarySortOption.entries) { option ->
            SortChip(
                label = option.label,
                isSelected = option == selectedSort,
                onClick = { onSortSelected(option) }
            )
        }
    }
}

@Composable
private fun SortChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    BentoCard(
        onClick = onClick,
        surfaceColor = if (isSelected) MusicVerseColors.ElectricBlue else MusicVerseColors.Surface2,
        contentPadding = 0.dp
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = if (isSelected) MusicVerseColors.TextOnAccent else MusicVerseColors.TextTertiary,
            modifier = Modifier.padding(horizontal = MusicVerseSpacing.M, vertical = MusicVerseSpacing.S)
        )
    }
}

// ── Stats Mini Card ──

@Composable
private fun StatMiniCard(
    value: String,
    label: String,
    accent: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier,
        surfaceColor = MusicVerseColors.Surface2,
        contentPadding = MusicVerseSpacing.M
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MusicVerseColors.TextTertiary
            )
        }
    }
}

// ── Track Card ──

@Composable
private fun LibraryTrackCard(
    track: TrackEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface2,
        contentPadding = MusicVerseSpacing.CardPaddingSmall,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Album art
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
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
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(MusicVerseSpacing.M))

            // Track info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MusicVerseColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = track.artist,
                    style = MaterialTheme.typography.bodySmall,
                    color = MusicVerseColors.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = track.album,
                    style = MaterialTheme.typography.bodySmall,
                    color = MusicVerseColors.TextTertiary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.width(MusicVerseSpacing.S))

            // Duration + ISRC
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatDuration(track.durationMs),
                    style = MaterialTheme.typography.labelSmall,
                    color = MusicVerseColors.TextTertiary
                )
                if (track.isrc != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    AudiophileBadge(
                        text = "ISRC",
                        backgroundColor = MusicVerseColors.BadgeLossless,
                        textColor = MusicVerseColors.ElectricBlue
                    )
                }
            }
        }
    }
}

// ── Empty State ──

@Composable
private fun EmptyLibraryCard(
    onNavigateToImport: () -> Unit,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3,
        onClick = onNavigateToImport
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
            Icon(
                Icons.Rounded.LibraryMusic,
                contentDescription = null,
                tint = MusicVerseColors.TextTertiary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
            Text(
                text = "Your Library is Empty",
                style = MaterialTheme.typography.headlineMedium,
                color = MusicVerseColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.XS))
            Text(
                text = "Import your Spotify Liked Songs to get started.",
                style = MaterialTheme.typography.bodyMedium,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
            AudiophileBadge(
                text = "Tap to import →",
                backgroundColor = MusicVerseColors.ElectricBlueGlow,
                textColor = MusicVerseColors.ElectricBlue
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
        }
    }
}

// ── No Results State ──

@Composable
private fun NoResultsCard(
    query: String,
    modifier: Modifier = Modifier
) {
    BentoCard(
        modifier = modifier.fillMaxWidth(),
        surfaceColor = MusicVerseColors.Surface3
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
            Icon(
                Icons.Rounded.SearchOff,
                contentDescription = null,
                tint = MusicVerseColors.TextTertiary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.M))
            Text(
                text = "No Results",
                style = MaterialTheme.typography.headlineMedium,
                color = MusicVerseColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.XS))
            Text(
                text = "No tracks matching \"$query\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(MusicVerseSpacing.L))
        }
    }
}

// ── Utility ──

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
