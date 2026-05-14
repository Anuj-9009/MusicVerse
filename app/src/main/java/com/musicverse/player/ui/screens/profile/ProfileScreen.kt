package com.musicverse.player.ui.screens.profile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicverse.player.ui.theme.EditorialSerif
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.MusicVerseMotion
import com.musicverse.player.ui.theme.SpaceMono
import com.musicverse.player.ui.theme.bouncingClickable
import kotlinx.coroutines.delay

/**
 * Profile Screen — matching the Stitch design.
 *
 * Features:
 *   - User avatar and name
 *   - "PREMIUM MEMBER" badge
 *   - Listening stats (hours, favorite source)
 *   - Audio Preferences (lossless toggle, equalizer)
 *   - Integrations (Spotify connected, YouTube link)
 */
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    var losslessEnabled by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(100)
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MusicVerseColors.DeepCharcoal)
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 120.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            // ── Top Bar ──
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MusicVerseColors.TextPrimary
                        )
                    }
                    Text(
                        text = "Profile",
                        fontFamily = InterFont,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = MusicVerseColors.TextPrimary
                    )
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "Settings",
                            tint = MusicVerseColors.TextPrimary
                        )
                    }
                }
            }

            // ── Avatar & User Info ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(spring(stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS)) +
                            slideInVertically(
                                initialOffsetY = { -30 },
                                animationSpec = spring(
                                    dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                                    stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                                )
                            )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar circle
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(MusicVerseColors.Surface3)
                                .border(2.dp, MusicVerseColors.Amber.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Rounded.Person,
                                contentDescription = "Avatar",
                                tint = MusicVerseColors.Amber,
                                modifier = Modifier.size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Julian Vance",
                            fontFamily = InterFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = MusicVerseColors.TextPrimary
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Premium badge
                        Text(
                            text = "PREMIUM MEMBER",
                            fontFamily = SpaceMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.5.sp,
                            color = MusicVerseColors.SunsetOrange
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Curating since October 2022",
                            fontFamily = InterFont,
                            fontSize = 13.sp,
                            color = MusicVerseColors.TextTertiary
                        )
                    }
                }
            }

            // ── Listening Stats ──
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(spring(stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS)) +
                            slideInVertically(
                                initialOffsetY = { 30 },
                                animationSpec = spring(
                                    dampingRatio = MusicVerseMotion.ENTRANCE_DAMPING,
                                    stiffness = MusicVerseMotion.ENTRANCE_STIFFNESS
                                )
                            )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Hours Listened
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Timer,
                            iconColor = MusicVerseColors.SunsetOrange,
                            label = "Hours\nListened",
                            value = "1,248",
                            subtitle = "↗ +12% this month"
                        )

                        // Favorite Source
                        StatCard(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Rounded.Favorite,
                            iconColor = MusicVerseColors.Amber,
                            label = "Favorite\nSource",
                            value = "Lo-fi\nBeats",
                            subtitle = "Top genre: Ambient Jazz"
                        )
                    }
                }
            }

            // ── Audio Preferences ──
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SectionTitle("Audio Preferences")
            }

            item {
                // Lossless Audio Toggle
                SettingsRow(
                    icon = Icons.Rounded.GraphicEq,
                    iconColor = MusicVerseColors.Amber,
                    title = "Lossless Audio",
                    subtitle = "Hi-Res 24-bit/192kHz streaming",
                    trailing = {
                        Switch(
                            checked = losslessEnabled,
                            onCheckedChange = { losslessEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MusicVerseColors.TextOnAccent,
                                checkedTrackColor = MusicVerseColors.SunsetOrange,
                                uncheckedThumbColor = MusicVerseColors.TextTertiary,
                                uncheckedTrackColor = MusicVerseColors.Surface3
                            )
                        )
                    }
                )
            }

            item {
                // Equalizer
                SettingsRow(
                    icon = Icons.Rounded.Equalizer,
                    iconColor = MusicVerseColors.SunsetOrange,
                    title = "Equalizer",
                    subtitle = "Custom profile: Warm Bass Boost",
                    trailing = {
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MusicVerseColors.TextTertiary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                )
            }

            // ── Integrations ──
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SectionTitle("Integrations")
            }

            item {
                IntegrationRow(
                    icon = Icons.Rounded.MusicNote,
                    iconColor = MusicVerseColors.Success,
                    title = "Spotify",
                    subtitle = "Connected as @jvance_studio",
                    actionText = "Disconnect",
                    actionColor = MusicVerseColors.TextTertiary
                )
            }

            item {
                IntegrationRow(
                    icon = Icons.Rounded.PlayCircle,
                    iconColor = Color(0xFFFF4444),
                    title = "YouTube Music",
                    subtitle = "Not connected",
                    actionText = "Link Account",
                    actionColor = MusicVerseColors.SunsetOrange
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontFamily = InterFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = MusicVerseColors.TextPrimary,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
    )
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    iconColor: Color,
    label: String,
    value: String,
    subtitle: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MusicVerseColors.Surface2)
            .padding(16.dp)
    ) {
        Column {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontFamily = InterFont,
                fontSize = 12.sp,
                color = MusicVerseColors.TextSecondary,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = InterFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MusicVerseColors.TextPrimary,
                lineHeight = 26.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                fontFamily = SpaceMono,
                fontSize = 10.sp,
                color = MusicVerseColors.SunsetOrange,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MusicVerseColors.TextPrimary
            )
            Text(
                text = subtitle,
                fontFamily = InterFont,
                fontSize = 12.sp,
                color = MusicVerseColors.TextTertiary
            )
        }

        trailing()
    }
}

@Composable
private fun IntegrationRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    actionText: String,
    actionColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontFamily = InterFont,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = MusicVerseColors.TextPrimary
            )
            Text(
                text = subtitle,
                fontFamily = InterFont,
                fontSize = 12.sp,
                color = if (subtitle.contains("Connected")) MusicVerseColors.Success else MusicVerseColors.TextTertiary
            )
        }

        Text(
            text = actionText,
            fontFamily = InterFont,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = actionColor,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(actionColor.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .bouncingClickable { /* Action */ }
        )
    }
}
