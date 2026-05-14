package com.musicverse.player.ui.components

import android.media.audiofx.Equalizer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.musicverse.player.audio.MusicVersePlayer
import com.musicverse.player.ui.theme.EditorialHeavy
import com.musicverse.player.ui.theme.InterFont
import com.musicverse.player.ui.theme.MusicVerseColors
import com.musicverse.player.ui.theme.SpaceMono

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerBottomSheet(
    musicVersePlayer: MusicVersePlayer,
    onDismissRequest: () -> Unit
) {
    val modalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var equalizer by remember { mutableStateOf<Equalizer?>(null) }
    var bands by remember { mutableStateOf<List<Short>>(emptyList()) }
    var minLevel by remember { mutableStateOf<Short>(0) }
    var maxLevel by remember { mutableStateOf<Short>(0) }

    LaunchedEffect(Unit) {
        val eq = musicVersePlayer.getEqualizer()
        equalizer = eq
        if (eq != null) {
            val numBands = eq.numberOfBands
            val bandRange = eq.bandLevelRange
            minLevel = bandRange[0]
            maxLevel = bandRange[1]

            val bandList = mutableListOf<Short>()
            for (i in 0 until numBands) {
                bandList.add(i.toShort())
            }
            bands = bandList
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = modalBottomSheetState,
        containerColor = MusicVerseColors.Surface1,
        dragHandle = { BottomSheetDefaults.DragHandle(color = MusicVerseColors.TextTertiary) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "AUDIO QUALITY",
                fontFamily = EditorialHeavy,
                fontSize = 24.sp,
                color = MusicVerseColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Adjust hardware frequency bands.",
                fontFamily = InterFont,
                fontSize = 14.sp,
                color = MusicVerseColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(32.dp))

            if (equalizer == null || bands.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Equalizer not available on this device or session not active.",
                        fontFamily = InterFont,
                        color = MusicVerseColors.TextTertiary
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    bands.forEach { band ->
                        val freq = equalizer!!.getCenterFreq(band) / 1000 // Convert mHz to Hz
                        val currentLevel = equalizer!!.getBandLevel(band)
                        
                        var sliderValue by remember { mutableStateOf(currentLevel.toFloat()) }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Vertical Slider (rotated horizontal slider)
                            Slider(
                                value = sliderValue,
                                onValueChange = { newValue ->
                                    sliderValue = newValue
                                    musicVersePlayer.setBandLevel(band, newValue.toInt().toShort())
                                },
                                valueRange = minLevel.toFloat()..maxLevel.toFloat(),
                                colors = SliderDefaults.colors(
                                    thumbColor = MusicVerseColors.ElectricBlue,
                                    activeTrackColor = MusicVerseColors.ElectricBlue,
                                    inactiveTrackColor = MusicVerseColors.Surface3
                                ),
                                modifier = Modifier
                                    .width(150.dp) // Becomes height after rotation
                                    .height(48.dp) // Becomes width after rotation
                                    .rotate(270f)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (freq >= 1000) "${freq / 1000}k" else "$freq",
                                fontFamily = SpaceMono,
                                fontSize = 10.sp,
                                color = MusicVerseColors.TextTertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}
