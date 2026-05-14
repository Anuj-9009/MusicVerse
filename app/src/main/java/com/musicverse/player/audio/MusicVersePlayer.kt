package com.musicverse.player.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.compose.runtime.Stable
import com.musicverse.player.data.api.SponsorSegment
import com.musicverse.player.data.repository.SponsorBlockRepository
import javax.inject.Named
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * MusicVersePlayer — The Dual-Source Audio Engine
 *
 * Manages two ExoPlayer instances simultaneously:
 *   - [primaryPlayer]: Currently active playback (Spotify URI or YouTube stream)
 *   - [ghostPlayer]:   Pre-buffering the top AI-scored version in the background
 *
 * This architecture enables instantaneous, volume-crossfaded switching between
 * the two music services — the core "seamless integration" feature of MusicVerse.
 */
@Singleton
class MusicVersePlayer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sponsorBlockRepository: SponsorBlockRepository,
    @Named("PrimaryPlayer") private val primaryPlayer: ExoPlayer,
    @Named("GhostPlayer") private val ghostPlayer: ExoPlayer,
    private val offlineTrackDao: com.musicverse.player.data.local.OfflineTrackDao
) {
    companion object {
        private const val TAG = "MusicVersePlayer"
        private const val CROSSFADE_DURATION_MS = 600L
        private const val CROSSFADE_STEPS = 20
        private const val SPONSORBLOCK_CHECK_INTERVAL_MS = 500L
    }

    // ── Deep-Tech Preload Architecture (v6.0) ─────────────────────────────────
    // BandwidthMeter, LoadControl, and ExoPlayers are now managed by AudioModule
    // to ensure PlaybackService shares the exact same instances.

    // PreloadManager removed for build stability

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val handler = Handler(Looper.getMainLooper())

    // ── State ─────────────────────────────────────────────────────────────────
    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private var activeSkipSegments: List<SponsorSegment> = emptyList()
    private var isCrossfading = false

    // SponsorBlock polling runnable
    private val sponsorBlockPoller = object : Runnable {
        override fun run() {
            checkAndSkipSponsorSegments()
            handler.postDelayed(this, SPONSORBLOCK_CHECK_INTERVAL_MS)
        }
    }

    init {
        setupPlayerListeners()
        ghostPlayer.volume = 0f  // Ghost player is always silent
    }

    // ── Public API ────────────────────────────────────────────────────────────

    private var fadePollingJob: kotlinx.coroutines.Job? = null

    /**
     * Load and play a Spotify track URI on the primary player.
     * Implements "Smooth Fade" for professional audio transitions.
     */
    fun playSpotifyTrack(spotifyUri: String, trackTitle: String, artist: String) {
        Log.d(TAG, "Playing Spotify track: $trackTitle ($spotifyUri)")
        
        // INTERCEPT: ExoPlayer cannot play raw spotify: URIs. 
        // We substitute high-quality public domain streams for MVP demonstration.
        // Uses hash of track title to pick different demo tracks for variety.
        val streamUrl = if (spotifyUri.startsWith("spotify:")) {
            val songIndex = (Math.abs(trackTitle.hashCode()) % 16) + 1
            "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-$songIndex.mp3"
        } else {
            spotifyUri
        }
        
        val mediaItem = MediaItem.fromUri(streamUrl)
        primaryPlayer.apply {
            volume = 0f // Start silent for fade-in
            setMediaItem(mediaItem)
            prepare()
            play()
        }
        _playerState.value = _playerState.value.copy(
            currentTitle = trackTitle,
            currentArtist = artist,
            source = PlaybackSource.SPOTIFY,
            isPlaying = true
        )
        startSponsorBlockPolling()
        startSmoothFadePolling()
        
        // Smooth fade in (very fast, 500ms so user hears sound instantly)
        fadeVolume(primaryPlayer, 0f, 1f, 500L)
    }

    private fun startSmoothFadePolling() {
        fadePollingJob?.cancel()
        fadePollingJob = scope.launch {
            var fadingOut = false
            while (true) {
                kotlinx.coroutines.delay(500)
                if (activePlayer.duration > 0 && activePlayer.currentPosition > 0) {
                    val timeRemaining = activePlayer.duration - activePlayer.currentPosition
                    if (timeRemaining in 1..3000 && !fadingOut) {
                        fadingOut = true
                        Log.d(TAG, "Smooth Fade: Track ending soon, fading out.")
                        fadeVolume(activePlayer, activePlayer.volume, 0f, timeRemaining)
                    }
                }
            }
        }
    }

    private fun fadeVolume(player: ExoPlayer, from: Float, to: Float, durationMs: Long) {
        val steps = 20
        val stepDelay = durationMs / steps
        val volumeStep = (to - from) / steps
        
        scope.launch {
            try {
                for (i in 1..steps) {
                    kotlinx.coroutines.delay(stepDelay)
                    player.volume = from + (volumeStep * i)
                }
            } finally {
                // Guaranteed to reach the target volume even if coroutine is cancelled
                player.volume = to
            }
        }
    }

    /**
     * Pre-load a YouTube stream URL into the ghost player without playing audio.
     * This is the "Ghost Buffer" — it warms up the stream for instant switching.
     * Checks Room DB first for true offline playback.
     */
    fun preloadYouTubeVersion(streamUrl: String, videoId: String) {
        scope.launch(Dispatchers.IO) {
            // Check Room DB for true offline caching
            val cachedTrack = offlineTrackDao.getOfflineTrackById(videoId)
            val isCached = cachedTrack != null && java.io.File(cachedTrack.localFilePath).exists()
            
            val finalUri = if (isCached) {
                Log.d(TAG, "Ghost buffering offline cached track: $videoId")
                java.io.File(cachedTrack!!.localFilePath).toURI().toString()
            } else {
                Log.d(TAG, "Ghost buffering YouTube stream: $videoId")
                streamUrl
            }
            
            val mediaItem = MediaItem.fromUri(finalUri)
            
            withContext(Dispatchers.Main) {
                ghostPlayer.apply {
                    volume = 0f
                    setMediaItem(mediaItem)
                    prepare()  // Prepare but DON'T call play() — just buffer
                }
            }

            // Fetch SponsorBlock segments in the background
            activeSkipSegments = sponsorBlockRepository.getSkipSegments(videoId)
            Log.d(TAG, "Loaded ${activeSkipSegments.size} skip segments for $videoId")

            _playerState.value = _playerState.value.copy(ghostReady = true)
        }
    }

    /**
     * The "Magic Switch" — seamlessly crossfades from Spotify to YouTube.
     * Syncs position, then volume-ramps both players over [CROSSFADE_DURATION_MS].
     */
    fun switchToYouTubeVersion() {
        if (isCrossfading || !_playerState.value.ghostReady) return
        isCrossfading = true

        Log.d(TAG, "Initiating crossfade: Spotify → YouTube")

        // Sync the ghost player to current position before crossfade
        val currentPositionMs = primaryPlayer.currentPosition
        ghostPlayer.seekTo(currentPositionMs)
        ghostPlayer.play()

        // Crossfade over CROSSFADE_DURATION_MS
        val stepDelayMs = CROSSFADE_DURATION_MS / CROSSFADE_STEPS
        var step = 0

        val crossfadeRunnable = object : Runnable {
            override fun run() {
                step++
                val progress = step.toFloat() / CROSSFADE_STEPS
                primaryPlayer.volume = 1f - progress   // Fade out Spotify
                ghostPlayer.volume = progress           // Fade in YouTube

                if (step < CROSSFADE_STEPS) {
                    handler.postDelayed(this, stepDelayMs)
                } else {
                    // Crossfade complete — primary becomes YouTube
                    completeCrossfade()
                }
            }
        }
        handler.post(crossfadeRunnable)
    }

    /**
     * Seamlessly switch back from YouTube → Spotify.
     */
    fun switchToSpotifyVersion() {
        if (isCrossfading) return
        isCrossfading = true

        Log.d(TAG, "Initiating crossfade: YouTube → Spotify")

        val currentPositionMs = ghostPlayer.currentPosition
        primaryPlayer.seekTo(currentPositionMs)
        primaryPlayer.volume = 0f
        primaryPlayer.play()

        val stepDelayMs = CROSSFADE_DURATION_MS / CROSSFADE_STEPS
        var step = 0

        val crossfadeRunnable = object : Runnable {
            override fun run() {
                step++
                val progress = step.toFloat() / CROSSFADE_STEPS
                ghostPlayer.volume = 1f - progress
                primaryPlayer.volume = progress

                if (step < CROSSFADE_STEPS) {
                    handler.postDelayed(this, stepDelayMs)
                } else {
                    ghostPlayer.pause()
                    ghostPlayer.volume = 0f
                    isCrossfading = false
                    _playerState.value = _playerState.value.copy(
                        source = PlaybackSource.SPOTIFY
                    )
                }
            }
        }
        handler.post(crossfadeRunnable)
    }

    fun play() {
        activePlayer.play()
        _playerState.value = _playerState.value.copy(isPlaying = true)
    }

    fun pause() {
        activePlayer.pause()
        _playerState.value = _playerState.value.copy(isPlaying = false)
    }

    fun seekTo(positionMs: Long) {
        activePlayer.seekTo(positionMs)
    }

    fun getCurrentPosition(): Long = activePlayer.currentPosition
    fun getDuration(): Long = activePlayer.duration

    fun release() {
        handler.removeCallbacksAndMessages(null)
        primaryPlayer.release()
        ghostPlayer.release()
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private val activePlayer: ExoPlayer
        get() = if (_playerState.value.source == PlaybackSource.YOUTUBE) ghostPlayer else primaryPlayer

    private fun completeCrossfade() {
        primaryPlayer.pause()
        primaryPlayer.volume = 1f
        ghostPlayer.volume = 1f
        isCrossfading = false
        _playerState.value = _playerState.value.copy(source = PlaybackSource.YOUTUBE)
        Log.d(TAG, "Crossfade complete. Now playing YouTube version.")
    }

    private fun checkAndSkipSponsorSegments() {
        if (activeSkipSegments.isEmpty()) return
        val positionMs = activePlayer.currentPosition
        val skipTarget = sponsorBlockRepository.getSkipTarget(positionMs, activeSkipSegments)
        if (skipTarget != null) {
            Log.d(TAG, "SponsorBlock: Skipping segment to ${skipTarget}ms")
            activePlayer.seekTo(skipTarget)
        }
    }

    private fun startSponsorBlockPolling() {
        handler.removeCallbacks(sponsorBlockPoller)
        handler.post(sponsorBlockPoller)
    }

    private fun setupPlayerListeners() {
        primaryPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (_playerState.value.source == PlaybackSource.SPOTIFY) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                }
            }
        })

        primaryPlayer.addAnalyticsListener(object : androidx.media3.exoplayer.analytics.AnalyticsListener {
            override fun onAudioSessionIdChanged(
                eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                audioSessionId: Int
            ) {
                super.onAudioSessionIdChanged(eventTime, audioSessionId)
                setupEqualizer(audioSessionId)
            }
        })

        ghostPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (_playerState.value.source == PlaybackSource.YOUTUBE) {
                    _playerState.value = _playerState.value.copy(isPlaying = isPlaying)
                }
            }
        })
        
        ghostPlayer.addAnalyticsListener(object : androidx.media3.exoplayer.analytics.AnalyticsListener {
            override fun onAudioSessionIdChanged(
                eventTime: androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime,
                audioSessionId: Int
            ) {
                super.onAudioSessionIdChanged(eventTime, audioSessionId)
                setupGhostEqualizer(audioSessionId)
            }
        })
    }

    private var primaryEqualizer: android.media.audiofx.Equalizer? = null
    private var ghostEqualizer: android.media.audiofx.Equalizer? = null
    
    // Default band levels for preserving EQ across sessions
    private val currentBandLevels = mutableMapOf<Short, Short>()

    private fun setupEqualizer(audioSessionId: Int) {
        try {
            primaryEqualizer?.release()
            primaryEqualizer = android.media.audiofx.Equalizer(0, audioSessionId).apply {
                enabled = true
                // Apply any saved bands
                currentBandLevels.forEach { (band, level) ->
                    try {
                        setBandLevel(band, level)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error applying primary equalizer band: ${e.message}")
                    }
                }
            }
            Log.d(TAG, "Primary Equalizer bound to session $audioSessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind Equalizer to session $audioSessionId", e)
        }
    }

    private fun setupGhostEqualizer(audioSessionId: Int) {
        try {
            ghostEqualizer?.release()
            ghostEqualizer = android.media.audiofx.Equalizer(0, audioSessionId).apply {
                enabled = true
                currentBandLevels.forEach { (band, level) ->
                    try {
                        setBandLevel(band, level)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error applying ghost equalizer band: ${e.message}")
                    }
                }
            }
            Log.d(TAG, "Ghost Equalizer bound to session $audioSessionId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to bind Ghost Equalizer to session $audioSessionId", e)
        }
    }

    /**
     * Sets the level for a specific equalizer band across all players.
     */
    fun setBandLevel(band: Short, level: Short) {
        currentBandLevels[band] = level
        try { primaryEqualizer?.setBandLevel(band, level) } catch (e: Exception) { /* ignore */ }
        try { ghostEqualizer?.setBandLevel(band, level) } catch (e: Exception) { /* ignore */ }
    }

    fun getEqualizer(): android.media.audiofx.Equalizer? {
        return primaryEqualizer
    }
}

// ── State Models ──────────────────────────────────────────────────────────────

@Stable
data class PlayerState(
    val currentTitle: String = "",
    val currentArtist: String = "",
    val source: PlaybackSource = PlaybackSource.SPOTIFY,
    val isPlaying: Boolean = false,
    val ghostReady: Boolean = false  // True when YouTube version is pre-buffered
)

enum class PlaybackSource { SPOTIFY, YOUTUBE }
