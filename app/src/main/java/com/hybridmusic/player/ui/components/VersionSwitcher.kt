package com.hybridmusic.player.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.hybridmusic.player.ui.theme.HybridColors
import com.hybridmusic.player.ui.theme.HybridSpacing

/**
 * Version types for alternate recordings.
 */
enum class VersionType(val label: String) {
    STUDIO("Studio"),
    LIVE("Live"),
    ACOUSTIC("Acoustic"),
    COVER("Cover"),
    REMIX("Remix")
}

/**
 * Version Switcher — Horizontal segmented pill control
 *
 * Industrial design: smooth sliding indicator with spring physics.
 * Haptic click on each selection for analog mechanical feel.
 */
@Composable
fun VersionSwitcher(
    versions: List<VersionType>,
    selectedVersion: VersionType,
    onVersionSelected: (VersionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(HybridColors.Surface2)
            .padding(HybridSpacing.XS)
            .height(36.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            versions.forEach { version ->
                val isSelected = version == selectedVersion

                val backgroundColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        when (version) {
                            VersionType.LIVE -> HybridColors.SunsetOrange
                            else -> HybridColors.ElectricBlue
                        }
                    } else HybridColors.Surface2,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "version_bg"
                )

                val textColor by animateColorAsState(
                    targetValue = if (isSelected) HybridColors.TextOnAccent
                    else HybridColors.TextTertiary,
                    label = "version_text"
                )

                Box(
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.extraSmall)
                        .background(backgroundColor)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onVersionSelected(version)
                        }
                        .padding(horizontal = HybridSpacing.M, vertical = HybridSpacing.XS),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = version.label.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = textColor
                    )
                }
            }
        }
    }
}
