package com.musicverse.player.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicverse.player.ui.theme.MusicVerseColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * ScrubBar — Interactive Seek Bar with Spring Physics
 *
 * A premium scrub/seek bar with:
 *   - Elapsed / Remaining time labels
 *   - Progress track with gradient fill
 *   - Draggable thumb with spring-physics bounce
 *   - Tap-to-seek support
 *   - Scale animation on touch
 *
 * @param currentPositionMs Current playback position in milliseconds
 * @param durationMs Total track duration in milliseconds
 * @param accentColor Dynamic accent color from album palette
 * @param onSeek Callback when user finishes seeking (position in ms)
 */
@Composable
fun ScrubBar(
    currentPositionMs: Long,
    durationMs: Long,
    accentColor: Color,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var dragProgress by remember { mutableFloatStateOf(0f) }
    var trackWidth by remember { mutableFloatStateOf(0f) }

    // Spring-animated thumb scale
    val thumbScale = remember { Animatable(1f) }

    // Calculate effective progress
    val progress = if (isDragging) {
        dragProgress
    } else {
        if (durationMs > 0) (currentPositionMs.toFloat() / durationMs).coerceIn(0f, 1f) else 0f
    }

    // Elapsed and remaining labels
    val elapsed = if (isDragging) {
        formatTime((dragProgress * durationMs).toLong())
    } else {
        formatTime(currentPositionMs)
    }
    val remaining = "-${formatTime(durationMs - currentPositionMs)}"

    Column(modifier = modifier.fillMaxWidth()) {
        // ── Seek Track ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .onSizeChanged { trackWidth = it.width.toFloat() }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val tappedProgress = (offset.x / trackWidth).coerceIn(0f, 1f)
                        onSeek((tappedProgress * durationMs).toLong())
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragStart = { offset ->
                            isDragging = true
                            dragProgress = (offset.x / trackWidth).coerceIn(0f, 1f)
                            scope.launch {
                                thumbScale.animateTo(
                                    1.8f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessHigh
                                    )
                                )
                            }
                        },
                        onDragEnd = {
                            isDragging = false
                            onSeek((dragProgress * durationMs).toLong())
                            scope.launch {
                                thumbScale.animateTo(
                                    1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessMedium
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            isDragging = false
                            scope.launch { thumbScale.animateTo(1f) }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            dragProgress = (dragProgress + dragAmount / trackWidth)
                                .coerceIn(0f, 1f)
                        }
                    )
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // Background track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(MusicVerseColors.Surface3)
                    .align(Alignment.Center)
            )

            // Filled progress track
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .align(Alignment.CenterStart)
            )

            // Thumb
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = ((progress * trackWidth) - 6.dp.toPx()).roundToInt(),
                            y = 0
                        )
                    }
                    .scale(thumbScale.value)
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(accentColor)
                    .align(Alignment.CenterStart)
            )
        }

        Spacer(Modifier.height(4.dp))

        // ── Time Labels ─────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = elapsed,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = remaining,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = MusicVerseColors.TextTertiary
            )
        }
    }
}

/**
 * Format milliseconds into mm:ss string.
 */
private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
