package com.musicverse.player.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.musicverse.player.ui.theme.EditorialCondensed
import com.musicverse.player.ui.theme.EditorialSerif
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseMotion
import com.musicverse.player.ui.theme.MusicVerseSpacing

// ── Spotify-Style Card ────────────────────────────────────────────────────────

/**
 * SpotifyCard — The core building block for all cards in the app.
 * Features spring-physics press animation and haptic feedback.
 */
@Composable
fun SpotifyCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    surfaceColor: Color = MusicVerseColors.Surface3,
    cornerRadius: Dp = 8.dp,
    contentPadding: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) MusicVerseMotion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = MusicVerseMotion.PRESS_DAMPING,
            stiffness = MusicVerseMotion.PRESS_STIFFNESS
        ),
        label = "card_press"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(cornerRadius))
            .background(surfaceColor)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null
                    ) {
                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                } else Modifier
            )
            .padding(contentPadding),
        content = content
    )
}

// ── Hero Grid Card (2x2 Spotify-style) ────────────────────────────────────────

/**
 * HeroGridCard — A compact, Spotify-style quick-action card for the 2x2 grid.
 * Small album art / icon on the left, title on the right.
 */
@Composable
fun HeroGridCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector? = null,
    iconTint: Color = MusicVerseColors.ElectricBlue,
    imageUrl: String? = null,
    onClick: () -> Unit
) {
    SpotifyCard(
        modifier = modifier.height(56.dp),
        onClick = onClick,
        surfaceColor = MusicVerseColors.Surface3,
        cornerRadius = 6.dp,
        contentPadding = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Left: Icon or image
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp))
                    .background(MusicVerseColors.Surface4),
                contentAlignment = Alignment.Center
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = title,
                        modifier = Modifier.size(56.dp)
                    )
                } else if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MusicVerseColors.TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }
}

// ── Filter Pill Chip ──────────────────────────────────────────────────────────

/**
 * FilterChip — A pill-shaped filter button inspired by Spotify's category pills.
 */
@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = MusicVerseMotion.PRESS_DAMPING,
            stiffness = MusicVerseMotion.PRESS_STIFFNESS
        ),
        label = "chip_press"
    )

    val bgColor by animateColorAsState(
        targetValue = if (isSelected) MusicVerseColors.ElectricBlue else MusicVerseColors.Surface3,
        label = "chip_bg"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) MusicVerseColors.TrueBlack else MusicVerseColors.TextPrimary,
        label = "chip_text"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = InterFont,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 13.sp,
            color = textColor
        )
    }
}

/**
 * FilterChipRow — A horizontal scrolling row of filter pills.
 */
@Composable
fun FilterChipRow(
    chips: List<String>,
    selectedIndex: Int,
    onChipSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chips.size) { index ->
            FilterChip(
                label = chips[index],
                isSelected = index == selectedIndex,
                onClick = { onChipSelected(index) }
            )
        }
    }
}

// ── Spotify Track Row ─────────────────────────────────────────────────────────

/**
 * TrackRow — A Spotify-style list item with album art, title, and artist.
 */
@Composable
fun TrackRow(
    title: String,
    artist: String,
    albumArtUrl: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) MusicVerseMotion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = MusicVerseMotion.PRESS_DAMPING,
            stiffness = MusicVerseMotion.PRESS_STIFFNESS
        ),
        label = "track_press"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Album art
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MusicVerseColors.Surface4)
        ) {
            albumArtUrl?.let {
                AsyncImage(
                    model = it,
                    contentDescription = title,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MusicVerseColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = artist,
                fontFamily = EditorialSerif,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
                color = MusicVerseColors.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

/**
 * EditorialSectionHeader — An ultra-condensed section title.
 */
@Composable
fun EditorialSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            fontFamily = EditorialCondensed,
            fontWeight = FontWeight.Normal,
            fontSize = 22.sp,
            letterSpacing = 1.5.sp,
            color = MusicVerseColors.TextPrimary
        )
        action?.invoke()
    }
}

// ── Glassmorphism Pill Button ─────────────────────────────────────────────────

/**
 * GlassPillButton — A frosted glass pill button for primary actions.
 */
@Composable
fun GlassPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = MusicVerseColors.ElectricBlue
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed) MusicVerseMotion.PRESS_SCALE else 1f,
        animationSpec = spring(
            dampingRatio = MusicVerseMotion.PRESS_DAMPING,
            stiffness = MusicVerseMotion.PRESS_STIFFNESS
        ),
        label = "pill_press"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(CircleShape)
            .background(accentColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            }
            .padding(horizontal = 32.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = InterFont,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            letterSpacing = 0.5.sp,
            color = MusicVerseColors.TrueBlack
        )
    }
}

// ── Legacy Aliases (keep old code compiling) ──────────────────────────────────

@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    surfaceColor: Color = MusicVerseColors.Surface2,
    borderColor: Color = Color.Transparent,
    contentPadding: Dp = MusicVerseSpacing.CardPadding,
    content: @Composable BoxScope.() -> Unit
) {
    SpotifyCard(
        modifier = modifier,
        onClick = onClick,
        surfaceColor = surfaceColor,
        contentPadding = contentPadding,
        content = content
    )
}

@Composable
fun BentoRow(
    modifier: Modifier = Modifier,
    gap: Dp = MusicVerseSpacing.BentoGap,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap)
    ) { content() }
}

@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    EditorialSectionHeader(title = title, modifier = modifier, action = action)
}

@Composable
fun AudiophileBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MusicVerseColors.BadgeLossless,
    textColor: Color = MusicVerseColors.ElectricBlue
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = EditorialCondensed,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            letterSpacing = 1.sp,
            color = textColor
        )
    }
}

@Composable
fun GradientScrim(
    modifier: Modifier = Modifier,
    colors: List<Color> = MusicVerseColors.GradientSurfaceFade,
    startY: Float = 0f,
    endY: Float = Float.POSITIVE_INFINITY
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(colors = colors, startY = startY, endY = endY)
            )
    )
}

@Composable
fun HybridDivider(
    modifier: Modifier = Modifier,
    color: Color = MusicVerseColors.Divider,
    thickness: Dp = 0.5.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
