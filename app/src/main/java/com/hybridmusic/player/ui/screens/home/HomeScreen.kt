package com.hybridmusic.player.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.LibraryMusic
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.SyncAlt
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hybridmusic.player.ui.components.AudiophileBadge
import com.hybridmusic.player.ui.components.BentoCard
import com.hybridmusic.player.ui.components.BentoRow
import com.hybridmusic.player.ui.components.HybridDivider
import com.hybridmusic.player.ui.components.SectionHeader
import com.hybridmusic.player.ui.theme.HybridColors
import com.hybridmusic.player.ui.theme.HybridSpacing
import com.hybridmusic.player.ui.theme.SpaceMono
import kotlinx.coroutines.delay

/**
 * Home Screen — Bento Grid Dashboard
 *
 * Features:
 *   - Hero greeting with staggered entrance animation
 *   - Quick Actions bento row (Import, Discover, Library)
 *   - Recently Discovered section
 *   - Stats overview cards
 */
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onNavigateToImport: () -> Unit = {},
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HybridColors.DeepCharcoal)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(
                top = HybridSpacing.Giant,
                bottom = 120.dp // Space for mini player
            ),
            verticalArrangement = Arrangement.spacedBy(HybridSpacing.L)
        ) {
            // ── Header ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioLowBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = HybridSpacing.ScreenMargin)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "HYBRID",
                                    style = MaterialTheme.typography.displayLarge,
                                    color = HybridColors.ElectricBlue
                                )
                                Text(
                                    text = "Music Player",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = HybridColors.TextSecondary
                                )
                            }
                            Row {
                                IconButton(onClick = { /* search */ }) {
                                    Icon(
                                        Icons.Rounded.Search,
                                        contentDescription = "Search",
                                        tint = HybridColors.TextSecondary
                                    )
                                }
                                IconButton(onClick = onNavigateToSettings) {
                                    Icon(
                                        Icons.Rounded.Settings,
                                        contentDescription = "Settings",
                                        tint = HybridColors.TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(HybridSpacing.S))

                        Text(
                            text = "v1.0.0 — ${getGreeting()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = HybridColors.TextTertiary
                        )
                    }
                }
            }

            // ── Quick Actions Bento Grid ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow)) +
                            slideInVertically(initialOffsetY = { 60 })
                ) {
                    Column {
                        SectionHeader(title = "Quick Actions")
                        Spacer(modifier = Modifier.height(HybridSpacing.S))

                        Column(
                            modifier = Modifier.padding(horizontal = HybridSpacing.ScreenMargin),
                            verticalArrangement = Arrangement.spacedBy(HybridSpacing.BentoGap)
                        ) {
                            // First row — two cards
                            BentoRow {
                                QuickActionCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Rounded.SyncAlt,
                                    title = "Import",
                                    subtitle = "Sync Spotify playlist",
                                    accentColor = HybridColors.ElectricBlue,
                                    onClick = onNavigateToImport
                                )
                                QuickActionCard(
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Rounded.GraphicEq,
                                    title = "Discover",
                                    subtitle = "Find alternate versions",
                                    accentColor = HybridColors.SunsetOrange,
                                    onClick = {}
                                )
                            }

                            // Second row — full width
                            QuickActionCard(
                                modifier = Modifier.fillMaxWidth(),
                                icon = Icons.Rounded.LibraryMusic,
                                title = "Library",
                                subtitle = "Browse all tracks & versions",
                                accentColor = HybridColors.ElectricBlue,
                                onClick = onNavigateToLibrary
                            )
                        }
                    }
                }
            }

            // ── Stats Overview ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow)) +
                            slideInVertically(initialOffsetY = { 80 })
                ) {
                    Column {
                        SectionHeader(
                            title = "Overview",
                            subtitle = "Your collection at a glance"
                        )
                        Spacer(modifier = Modifier.height(HybridSpacing.S))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = HybridSpacing.ScreenMargin),
                            horizontalArrangement = Arrangement.spacedBy(HybridSpacing.BentoGap)
                        ) {
                            StatCard(
                                modifier = Modifier.weight(1f),
                                value = "0",
                                label = "TRACKS",
                                accentColor = HybridColors.ElectricBlue
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                value = "0",
                                label = "VERSIONS",
                                accentColor = HybridColors.SunsetOrange
                            )
                            StatCard(
                                modifier = Modifier.weight(1f),
                                value = "—",
                                label = "AVG VIBE",
                                accentColor = HybridColors.Success
                            )
                        }
                    }
                }
            }

            // ── Empty state prompt ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = spring(stiffness = Spring.StiffnessVeryLow)) +
                            slideInVertically(initialOffsetY = { 100 })
                ) {
                    BentoCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = HybridSpacing.ScreenMargin),
                        surfaceColor = HybridColors.Surface3,
                        onClick = onNavigateToImport
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Rounded.Album,
                                    contentDescription = null,
                                    tint = HybridColors.ElectricBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(HybridSpacing.S))
                                Text(
                                    text = "Get Started",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = HybridColors.TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(HybridSpacing.S))
                            Text(
                                text = "Connect your Spotify account to import your library and discover live, acoustic, and cover versions of your favorite tracks.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = HybridColors.TextSecondary
                            )
                            Spacer(modifier = Modifier.height(HybridSpacing.M))
                            AudiophileBadge(
                                text = "Tap to connect Spotify",
                                backgroundColor = HybridColors.ElectricBlueGlow,
                                textColor = HybridColors.ElectricBlue
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    BentoCard(
        modifier = modifier,
        surfaceColor = HybridColors.Surface2,
        onClick = onClick
    ) {
        Column {
            Icon(
                icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(HybridSpacing.M))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = HybridColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(HybridSpacing.XXS))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = HybridColors.TextTertiary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    value: String,
    label: String,
    accentColor: androidx.compose.ui.graphics.Color
) {
    BentoCard(
        modifier = modifier,
        surfaceColor = HybridColors.Surface2,
        contentPadding = HybridSpacing.M
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                style = MaterialTheme.typography.displaySmall,
                color = accentColor
            )
            Spacer(modifier = Modifier.height(HybridSpacing.XS))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = HybridColors.TextTertiary
            )
        }
    }
}

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
