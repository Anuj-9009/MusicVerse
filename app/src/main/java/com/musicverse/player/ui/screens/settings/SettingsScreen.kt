package com.musicverse.player.ui.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicverse.player.ui.theme.MusicVerseColors

/**
 * SettingsScreen — BYOK (Bring Your Own Key) Panel
 *
 * Allows power users to supply their own Gemini and YouTube API keys.
 * When a custom key is saved, it overrides the app's embedded key
 * and routes all API calls through the user's own free quota.
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MusicVerseColors.DeepCharcoal)
            .padding(horizontal = 20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // ── Top Bar ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back", tint = MusicVerseColors.TextPrimary)
            }
            Text(
                text = "SETTINGS",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 3.sp,
                color = MusicVerseColors.TextPrimary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // ── BYOK Section ──────────────────────────────────────────────────
        SectionHeader(title = "BRING YOUR OWN KEY", subtitle = "Override app keys to use your personal API quota")

        Spacer(Modifier.height(16.dp))

        ApiKeyField(
            label = "Gemini API Key",
            hint = "AIzaSy...",
            value = uiState.geminiKeyInput,
            isSaved = uiState.geminiKeySaved,
            onValueChange = viewModel::onGeminiKeyChanged,
            onSave = viewModel::saveGeminiKey,
            onClear = viewModel::clearGeminiKey
        )

        Spacer(Modifier.height(12.dp))

        ApiKeyField(
            label = "YouTube Data API Key",
            hint = "AIzaSy...",
            value = uiState.youtubeKeyInput,
            isSaved = uiState.youtubeKeySaved,
            onValueChange = viewModel::onYoutubeKeyChanged,
            onSave = viewModel::saveYoutubeKey,
            onClear = viewModel::clearYoutubeKey
        )

        Spacer(Modifier.height(24.dp))

        // ── Info Card ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MusicVerseColors.Surface2)
                .padding(16.dp)
        ) {
            Text(
                text = "ⓘ  HOW BYOK WORKS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MusicVerseColors.ElectricBlue
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "When you provide your own API key, all requests for that service are routed through your personal quota. " +
                       "This prevents hitting the shared free-tier limits during heavy usage. " +
                       "Get a free Gemini key at aistudio.google.com and a YouTube key at console.cloud.google.com.",
                fontSize = 13.sp,
                color = MusicVerseColors.TextSecondary,
                lineHeight = 20.sp
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}

@Composable
private fun SectionHeader(title: String, subtitle: String) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = MusicVerseColors.SunsetOrange
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 13.sp,
            color = MusicVerseColors.TextTertiary
        )
    }
}

@Composable
private fun ApiKeyField(
    label: String,
    hint: String,
    value: String,
    isSaved: Boolean,
    onValueChange: (String) -> Unit,
    onSave: () -> Unit,
    onClear: () -> Unit
) {
    var showKey by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MusicVerseColors.Surface2)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.Key,
                    contentDescription = null,
                    tint = if (isSaved) MusicVerseColors.Success else MusicVerseColors.TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "  $label",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MusicVerseColors.TextPrimary
                )
            }
            AnimatedVisibility(visible = isSaved) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Rounded.Check,
                        contentDescription = "Saved",
                        tint = MusicVerseColors.Success,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        " ACTIVE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MusicVerseColors.Success
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text(hint, color = MusicVerseColors.TextTertiary, fontSize = 13.sp)
            },
            visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                IconButton(onClick = { showKey = !showKey }) {
                    Icon(
                        imageVector = if (showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                        contentDescription = if (showKey) "Hide" else "Show",
                        tint = MusicVerseColors.TextSecondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MusicVerseColors.ElectricBlue,
                unfocusedBorderColor = MusicVerseColors.Border,
                focusedTextColor = MusicVerseColors.TextPrimary,
                unfocusedTextColor = MusicVerseColors.TextPrimary,
                cursorColor = MusicVerseColors.ElectricBlue
            ),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Save Button
            androidx.compose.material3.Button(
                onClick = onSave,
                enabled = value.isNotBlank(),
                shape = RoundedCornerShape(8.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                    containerColor = MusicVerseColors.ElectricBlue,
                    contentColor = MusicVerseColors.TrueBlack
                )
            ) {
                Text("Save", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }

            // Clear Button (only show if saved)
            AnimatedVisibility(visible = isSaved) {
                androidx.compose.material3.OutlinedButton(
                    onClick = onClear,
                    shape = RoundedCornerShape(8.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MusicVerseColors.Error
                    )
                ) {
                    Text("Clear", fontSize = 13.sp)
                }
            }
        }
    }
}
