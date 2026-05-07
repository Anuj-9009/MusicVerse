package com.musicverse.player.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

/**
 * FluidVisualizer — Procedural Audio Waveform Visualizer
 *
 * A canvas-based composable that renders a multi-layered, sine-wave
 * animation. Each layer has a slightly different frequency, amplitude,
 * and phase offset to create an organic, fluid motion that reacts
 * to the [isPlaying] state.
 *
 * When playing:   Waves animate with full amplitude and smooth motion.
 * When paused:    Waves flatten to a still line with micro-shimmer.
 *
 * The wave colors are derived from the album art palette, creating
 * a seamless visual connection between the artwork and the visualizer.
 */
@Composable
fun FluidVisualizer(
    isPlaying: Boolean,
    accentColor: Color,
    secondaryColor: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "visualizer")

    // Primary wave phase — smooth continuous rotation
    val phase1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase1"
    )

    // Secondary wave — slower, creates interference pattern
    val phase2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase2"
    )

    // Tertiary wave — slowest, adds depth
    val phase3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase3"
    )

    // Breathing amplitude when paused
    val breatheAmplitude by infiniteTransition.animateFloat(
        initialValue = 0.15f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )

    val amplitudeMultiplier = if (isPlaying) 1f else breatheAmplitude

    // Wave layer definitions
    val layers = remember(accentColor, secondaryColor) {
        listOf(
            WaveLayer(
                frequency = 1.5f,
                amplitudeFraction = 0.35f,
                color = accentColor.copy(alpha = 0.15f),
                strokeWidth = 3f
            ),
            WaveLayer(
                frequency = 2.0f,
                amplitudeFraction = 0.25f,
                color = secondaryColor.copy(alpha = 0.25f),
                strokeWidth = 2.5f
            ),
            WaveLayer(
                frequency = 2.8f,
                amplitudeFraction = 0.18f,
                color = accentColor.copy(alpha = 0.4f),
                strokeWidth = 2f
            ),
            WaveLayer(
                frequency = 1.2f,
                amplitudeFraction = 0.4f,
                color = accentColor.copy(alpha = 0.08f),
                strokeWidth = 4f,
                filled = true
            )
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        val centerY = size.height / 2f
        val phases = listOf(phase1, phase2, phase3, phase1 * 0.7f)

        layers.forEachIndexed { index, layer ->
            drawWaveLayer(
                layer = layer,
                phase = phases[index],
                centerY = centerY,
                amplitudeMultiplier = amplitudeMultiplier
            )
        }

        // Center glow line — the "heartbeat" line
        drawLine(
            brush = Brush.horizontalGradient(
                colors = listOf(
                    Color.Transparent,
                    accentColor.copy(alpha = 0.3f * amplitudeMultiplier),
                    accentColor.copy(alpha = 0.5f * amplitudeMultiplier),
                    accentColor.copy(alpha = 0.3f * amplitudeMultiplier),
                    Color.Transparent
                )
            ),
            start = Offset(0f, centerY),
            end = Offset(size.width, centerY),
            strokeWidth = 1f
        )
    }
}

/**
 * Internal data class for wave layer configuration.
 */
private data class WaveLayer(
    val frequency: Float,
    val amplitudeFraction: Float,
    val color: Color,
    val strokeWidth: Float,
    val filled: Boolean = false
)

/**
 * Draws a single sine wave layer on the canvas.
 */
private fun DrawScope.drawWaveLayer(
    layer: WaveLayer,
    phase: Float,
    centerY: Float,
    amplitudeMultiplier: Float
) {
    val path = Path()
    val amplitude = size.height * layer.amplitudeFraction * amplitudeMultiplier
    val steps = 120 // Number of points on the wave

    path.moveTo(0f, centerY)

    for (i in 0..steps) {
        val x = (i.toFloat() / steps) * size.width
        val normalizedX = (i.toFloat() / steps) * layer.frequency * 2f * PI.toFloat()
        val y = centerY + sin(normalizedX + phase) * amplitude

        if (i == 0) {
            path.moveTo(x, y)
        } else {
            path.lineTo(x, y)
        }
    }

    if (layer.filled) {
        // Close the path for the filled layer
        val fillPath = Path()
        fillPath.addPath(path)
        fillPath.lineTo(size.width, size.height)
        fillPath.lineTo(0f, size.height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(layer.color, Color.Transparent)
            )
        )
    } else {
        drawPath(
            path = path,
            color = layer.color,
            style = Stroke(
                width = layer.strokeWidth,
                cap = StrokeCap.Round
            )
        )
    }
}
