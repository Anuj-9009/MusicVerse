package com.musicverse.player.di

import android.content.Context
import android.os.Looper
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

/**
 * AudioModule — Unified Media3 Dependency Injection
 *
 * Ensures that the exact same ExoPlayer instances are used by the background
 * PlaybackService and the UI's MusicVersePlayer engine. This prevents audio
 * routing bugs, memory leaks, and out-of-sync playback states.
 */
@Module
@InstallIn(SingletonComponent::class)
object AudioModule {

    @Provides
    @Singleton
    fun provideBandwidthMeter(@ApplicationContext context: Context): DefaultBandwidthMeter {
        return DefaultBandwidthMeter.Builder(context).build()
    }

    @Provides
    @Singleton
    fun provideCustomLoadControl(): DefaultLoadControl {
        return DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                500, // Quick start (bufferForPlaybackMs)
                1000 // Buffer before re-buffering (bufferForPlaybackAfterRebufferMs)
            )
            .build()
    }

    @Provides
    @Singleton
    @Named("PrimaryPlayer")
    fun providePrimaryExoPlayer(
        @ApplicationContext context: Context,
        bandwidthMeter: DefaultBandwidthMeter,
        customLoadControl: DefaultLoadControl
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setLooper(Looper.getMainLooper())
            .setBandwidthMeter(bandwidthMeter)
            .setLoadControl(customLoadControl)
            .build()
    }

    @Provides
    @Singleton
    @Named("GhostPlayer")
    fun provideGhostExoPlayer(
        @ApplicationContext context: Context,
        bandwidthMeter: DefaultBandwidthMeter,
        customLoadControl: DefaultLoadControl
    ): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setLooper(Looper.getMainLooper())
            .setBandwidthMeter(bandwidthMeter)
            .setLoadControl(customLoadControl)
            .build()
    }
}
