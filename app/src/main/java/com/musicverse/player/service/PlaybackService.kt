package com.musicverse.player.service

import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Named

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
    
    @Inject
    @Named("PrimaryPlayer")
    lateinit var player: ExoPlayer

    override fun onCreate() {
        super.onCreate()

        // Use the globally injected primary player for the MediaSession
        // Ensures UI and notification controls manipulate the exact same stream
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        // Reconfigure the injected player for audio focus
        player.setAudioAttributes(audioAttributes, true)
        
        // This is safe to call multiple times or on an existing player
        // No need to build a new one!
        
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
