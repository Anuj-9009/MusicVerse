package com.hybridmusic.player.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hybridmusic.player.ui.theme.HybridColors
import com.hybridmusic.player.ui.theme.HybridSpacing

/**
 * Bento Grid 2.0 Card — Core building block
 *
 * Smooth rounded rectangle with subtle press animation
 * and optional border glow on focus. No muddy drop-shadows.
 */
@Composable
fun BentoCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    surfaceColor: Color = HybridColors.Surface2,
    borderColor: Color = Color.Transparent,
    contentPadding: Dp = HybridSpacing.CardPadding,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptics = LocalHapticFeedback.current

    // Subtle scale-down on press (Teenage Engineering mechanical feel)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bento_press_scale"
    )

    val animatedBorder by animateColorAsState(
        targetValue = if (isPressed) HybridColors.ElectricBlue.copy(alpha = 0.3f) else borderColor,
        animationSpec = tween(150),
        label = "bento_border_glow"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .clip(MaterialTheme.shapes.medium)
            .background(surfaceColor)
            .border(
                width = 1.dp,
                color = animatedBorder,
                shape = MaterialTheme.shapes.medium
            )
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

/**
 * Bento Grid Row — Horizontal row of cards with consistent gap.
 */
@Composable
fun BentoRow(
    modifier: Modifier = Modifier,
    gap: Dp = HybridSpacing.BentoGap,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(gap),
        content = content
    )
}

/**
 * Section Header — Clash Grotesk aggressive header with optional action.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                horizontal = HybridSpacing.ScreenMargin,
                vertical = HybridSpacing.S
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = HybridColors.TextPrimary
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = HybridColors.TextTertiary
                )
            }
        }
        action?.invoke()
    }
}

/**
 * Audiophile Badge — Semi-transparent pill with glow effect.
 */
@Composable
fun AudiophileBadge(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = HybridColors.BadgeLossless,
    textColor: Color = HybridColors.ElectricBlue
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(backgroundColor)
            .padding(horizontal = HybridSpacing.S, vertical = HybridSpacing.XS),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor
        )
    }
}

/**
 * Gradient Scrim — Overlay for text legibility on album art.
 */
@Composable
fun GradientScrim(
    modifier: Modifier = Modifier,
    colors: List<Color> = HybridColors.GradientSurfaceFade,
    startY: Float = 0f,
    endY: Float = Float.POSITIVE_INFINITY
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = colors,
                    startY = startY,
                    endY = endY
                )
            )
    )
}

/**
 * Thin Divider — Industrial hairline separator.
 */
@Composable
fun HybridDivider(
    modifier: Modifier = Modifier,
    color: Color = HybridColors.Divider,
    thickness: Dp = 0.5.dp
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
            .background(color)
    )
}
