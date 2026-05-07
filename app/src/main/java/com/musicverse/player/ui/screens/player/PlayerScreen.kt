@file:OptIn(ExperimentalSharedTransitionApi::class)

package com.musicverse.player.ui.screens.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.musicverse.player.ui.theme.EditorialHeavy
import com.musicverse.player.ui.theme.SpaceMono
import com.musicverse.player.ui.theme.bouncingClickable

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.PlayerScreen(
    animatedVisibilityScope: androidx.compose.animation.AnimatedVisibilityScope,
    albumArtUrl: String,
    title: String,
    artist: String,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    onCloseClick: () -> Unit
) {
    // Pinterest Inspiration: Adaptive Cinematic Gradient (Simulated based on album art)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF8E2DE2), Color(0xFF121212)),
        startY = 0f,
        endY = Float.POSITIVE_INFINITY
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        // Close button (Ruthless Omission: hidden behind minimal icon)
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.run {
                    androidx.compose.material.icons.Icons.Filled.run {
                        androidx.compose.material.icons.filled.KeyboardArrowDown
                    }
                },
                contentDescription = "Close",
                tint = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Shared Element: Album Art
            AsyncImage(
                model = albumArtUrl,
                contentDescription = "Album Art",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .sharedElement(
                        state = rememberSharedContentState(key = "album_art_$albumArtUrl"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            spring(
                                dampingRatio = Spring.DampingRatioNoBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        }
                    )
                    .clip(RoundedCornerShape(24.dp))
                    // Pinterest Inspiration: Subtle shadow/blur
                    .graphicsLayer {
                        shadowElevation = 30f
                        shape = RoundedCornerShape(24.dp)
                        clip = true
                    }
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Liquid Glass Container for Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(32.dp))
                    .blur(60.dp) // Liquid Glass Deep Blur
                    .background(Color.White.withOpacity(0.05f))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Brutalist Typography
                    Text(
                        text = title.uppercase(),
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        maxLines = 1
                    )
                    
                    Text(
                        text = artist.uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Oversized Hardware Controls (Teenage Engineering)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .bouncingClickable { /* Prev */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("<", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        }

                        // Play/Pause (Oversized)
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(Color.White)
                                .bouncingClickable { onPlayPauseClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (isPlaying) "||" else ">",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color.Black
                            )
                        }

                        // Next
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White.copy(alpha = 0.1f))
                                .bouncingClickable { /* Next */ },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(">", style = MaterialTheme.typography.headlineMedium, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
