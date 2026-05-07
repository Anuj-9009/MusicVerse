package com.musicverse.player.ui.components

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.musicverse.player.ui.theme.MusicVerseColors

/**
 * DynamicPaletteExtractor — Album Art Color Extraction
 *
 * Downloads the album art bitmap using Coil's ImageLoader, then runs
 * Android's Palette API to extract the dominant, vibrant, and muted
 * swatch colors. These are used to dynamically theme the player
 * background, scrub bar, and visualizer in real-time.
 */
data class AlbumPalette(
    val dominant: Color = MusicVerseColors.DeepCharcoal,
    val vibrant: Color = MusicVerseColors.ElectricBlue,
    val vibrantDark: Color = MusicVerseColors.Surface1,
    val muted: Color = MusicVerseColors.Surface2,
    val mutedDark: Color = MusicVerseColors.TrueBlack,
    val onDominant: Color = MusicVerseColors.TextPrimary
)

/**
 * Composable effect that extracts an [AlbumPalette] from a remote image URL.
 * Uses Coil to fetch the bitmap, then runs Palette generation off-main-thread.
 *
 * @param imageUrl The album art URL to extract colors from
 * @return The extracted [AlbumPalette] with animated-ready Color values
 */
@Composable
fun rememberAlbumPalette(imageUrl: String?): AlbumPalette {
    var palette by remember { mutableStateOf(AlbumPalette()) }
    val context = LocalContext.current

    LaunchedEffect(imageUrl) {
        if (imageUrl.isNullOrBlank()) return@LaunchedEffect

        try {
            // Use Coil to download the bitmap
            val loader = ImageLoader(context)
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .allowHardware(false) // Palette requires software bitmaps
                .size(256) // Downscale for faster extraction
                .build()

            val result = loader.execute(request)
            if (result is SuccessResult) {
                val bitmap = (result.drawable as? BitmapDrawable)?.bitmap
                    ?: return@LaunchedEffect

                // Generate the palette from the bitmap
                val p = Palette.from(bitmap).generate()

                palette = AlbumPalette(
                    dominant = p.getDominantColor(0xFF121212.toInt()).toComposeColor(),
                    vibrant = p.getVibrantColor(0xFF00BFFF.toInt()).toComposeColor(),
                    vibrantDark = p.getDarkVibrantColor(0xFF1A1A1A.toInt()).toComposeColor(),
                    muted = p.getMutedColor(0xFF1E1E1E.toInt()).toComposeColor(),
                    mutedDark = p.getDarkMutedColor(0xFF000000.toInt()).toComposeColor(),
                    onDominant = p.getDominantSwatch()?.let {
                        Color(it.titleTextColor)
                    } ?: MusicVerseColors.TextPrimary
                )
            }
        } catch (_: Exception) {
            // Fallback to default palette on any error
        }
    }

    return palette
}

/**
 * Convert an Android color int to a Compose Color.
 */
private fun Int.toComposeColor(): Color = Color(this)
