package com.musicverse.player.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * PlaybackService — Foreground Media3 Session Service
 *
 * Runs in the foreground so playback continues when the user leaves the app.
 * Exposes a [MediaSession] to the OS so external controls (lock screen,
 * Bluetooth headphones, Android Auto) work seamlessly.
 *
 * Declared in AndroidManifest.xml under the <service> tag.
 */
@AndroidEntryPoint
class PlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        // Build a dedicated ExoPlayer for the MediaSession
        // (This is separate from MusicVersePlayer's dual-player engine;
        //  it serves the OS-level controls and notification.)
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus= */ true)
            .setHandleAudioBecomingNoisy(true) // Pause on headphone unplug
            .build()

        mediaSession = MediaSession.Builder(this, player)
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}
