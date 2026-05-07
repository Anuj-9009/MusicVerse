package com.musicverse.player.ui.screens.player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicverse.player.audio.MusicVersePlayer
import com.musicverse.player.audio.PlayerState
import com.musicverse.player.audio.PlaybackSource
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.data.local.VersionDao
import com.musicverse.player.data.local.VersionEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val musicVersePlayer: MusicVersePlayer,
    private val trackDao: TrackDao,
    private val versionDao: VersionDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val trackId: String = savedStateHandle["trackId"] ?: ""

    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    val playerState: StateFlow<PlayerState> = musicVersePlayer.playerState

    // Queue: all tracks in the library, for skip next/prev
    private var trackQueue: List<TrackEntity> = emptyList()
    private var currentQueueIndex: Int = -1

    init {
        loadTrack()
        loadQueue()
        // Observe player state changes
        musicVersePlayer.playerState
            .onEach { state ->
                _uiState.value = _uiState.value.copy(
                    isPlaying = state.isPlaying,
                    currentSource = state.source,
                    ghostReady = state.ghostReady
                )
            }
            .launchIn(viewModelScope)

        // Start position polling for the scrub bar
        startPositionPolling()
    }

    private fun loadQueue() {
        viewModelScope.launch {
            trackQueue = trackDao.getAllTracks().first()
            currentQueueIndex = trackQueue.indexOfFirst { it.id == trackId }
        }
    }

    private fun loadTrack() {
        viewModelScope.launch {
            val track = trackDao.getTrackById(trackId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                trackTitle = track.title,
                trackArtist = track.artist,
                albumArtUrl = track.albumArtUrl,
                spotifyUri = track.spotifyUri,
                currentTrackId = track.id
            )

            // Load track into primary player
            track.spotifyUri?.let { uri ->
                musicVersePlayer.playSpotifyTrack(uri, track.title, track.artist)
            }

            // Fetch AI-discovered versions and pre-load the best one
            versionDao.getVersionsForTrack(trackId)
                .onEach { versions ->
                    _uiState.value = _uiState.value.copy(discoveredVersions = versions)
                    preloadTopVersion(versions)
                }
                .launchIn(this)
        }
    }

    private fun loadTrackById(newTrackId: String) {
        viewModelScope.launch {
            val track = trackDao.getTrackById(newTrackId) ?: return@launch
            _uiState.value = _uiState.value.copy(
                trackTitle = track.title,
                trackArtist = track.artist,
                albumArtUrl = track.albumArtUrl,
                spotifyUri = track.spotifyUri,
                currentTrackId = track.id,
                ghostReady = false,
                discoveredVersions = emptyList(),
                topVersion = null,
                currentPositionMs = 0L,
                durationMs = 0L
            )

            track.spotifyUri?.let { uri ->
                musicVersePlayer.playSpotifyTrack(uri, track.title, track.artist)
            }

            versionDao.getVersionsForTrack(newTrackId)
                .onEach { versions ->
                    _uiState.value = _uiState.value.copy(discoveredVersions = versions)
                    preloadTopVersion(versions)
                }
                .launchIn(this)
        }
    }

    private fun preloadTopVersion(versions: List<VersionEntity>) {
        val topVersion = versions.maxByOrNull { it.aiVibeScore } ?: return
        topVersion.youtubeVideoId?.let { videoId ->
            val streamUrl = "https://www.youtube.com/watch?v=$videoId"
            musicVersePlayer.preloadYouTubeVersion(streamUrl, videoId)
            _uiState.value = _uiState.value.copy(topVersion = topVersion)
        }
    }

    /**
     * Poll the player position every 250ms for smooth scrub bar updates.
     */
    private fun startPositionPolling() {
        viewModelScope.launch {
            while (true) {
                val position = musicVersePlayer.getCurrentPosition()
                val duration = musicVersePlayer.getDuration()
                if (duration > 0) {
                    _uiState.value = _uiState.value.copy(
                        currentPositionMs = position.coerceAtLeast(0),
                        durationMs = duration.coerceAtLeast(0)
                    )
                }
                delay(250L)
            }
        }
    }

    fun onPlayPause() {
        if (_uiState.value.isPlaying) {
            musicVersePlayer.pause()
        } else {
            musicVersePlayer.play()
        }
    }

    fun onSeek(positionMs: Long) {
        musicVersePlayer.seekTo(positionMs)
        _uiState.value = _uiState.value.copy(currentPositionMs = positionMs)
    }

    fun onSwitchSource() {
        when (_uiState.value.currentSource) {
            PlaybackSource.SPOTIFY -> musicVersePlayer.switchToYouTubeVersion()
            PlaybackSource.YOUTUBE -> musicVersePlayer.switchToSpotifyVersion()
        }
    }

    /**
     * Skip to the next track in the queue.
     */
    fun onSkipNext() {
        if (trackQueue.isEmpty()) return
        currentQueueIndex = (currentQueueIndex + 1) % trackQueue.size
        val nextTrack = trackQueue[currentQueueIndex]
        loadTrackById(nextTrack.id)
    }

    /**
     * Skip to the previous track in the queue.
     */
    fun onSkipPrevious() {
        if (trackQueue.isEmpty()) return
        // If we're past 3 seconds, restart current track instead
        if (musicVersePlayer.getCurrentPosition() > 3000) {
            musicVersePlayer.seekTo(0)
            _uiState.value = _uiState.value.copy(currentPositionMs = 0)
            return
        }
        currentQueueIndex = if (currentQueueIndex <= 0) trackQueue.size - 1 else currentQueueIndex - 1
        val prevTrack = trackQueue[currentQueueIndex]
        loadTrackById(prevTrack.id)
    }

    override fun onCleared() {
        super.onCleared()
        musicVersePlayer.pause()
    }
}

data class PlayerUiState(
    val trackTitle: String = "",
    val trackArtist: String = "",
    val albumArtUrl: String? = null,
    val spotifyUri: String? = null,
    val currentTrackId: String = "",
    val isPlaying: Boolean = false,
    val currentSource: PlaybackSource = PlaybackSource.SPOTIFY,
    val ghostReady: Boolean = false,
    val discoveredVersions: List<VersionEntity> = emptyList(),
    val topVersion: VersionEntity? = null,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L
)
