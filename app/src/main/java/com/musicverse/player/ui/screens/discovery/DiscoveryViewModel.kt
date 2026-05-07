package com.musicverse.player.ui.screens.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.data.local.VersionDao
import com.musicverse.player.data.local.VersionEntity
import com.musicverse.player.data.repository.VersionDiscoveryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Discovery phases.
 */
enum class DiscoveryPhase {
    IDLE,           // Ready to start discovery
    DISCOVERING,    // AI is analyzing tracks
    COMPLETED,      // All tracks processed
    ERROR           // Something went wrong
}

/**
 * UI State for the Discovery screen.
 */
data class DiscoveryUiState(
    val phase: DiscoveryPhase = DiscoveryPhase.IDLE,
    val processedTracks: Int = 0,
    val totalTracks: Int = 0,
    val totalVersionsFound: Int = 0,
    val currentTrackName: String = "",
    val errorMessage: String? = null
)

/**
 * ViewModel for the Version Discovery screen.
 *
 * Drives the Gemini AI discovery pipeline:
 *   - Fetches all imported tracks from Room
 *   - For each track, searches YouTube and scores with Gemini
 *   - Provides real-time progress updates
 *   - Caches all results in Room
 */
@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val discoveryRepository: VersionDiscoveryRepository,
    private val trackDao: TrackDao,
    private val versionDao: VersionDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(DiscoveryUiState())
    val uiState: StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    /**
     * Top discovered versions sorted by vibe score.
     */
    val topVersions: StateFlow<List<VersionEntity>> = discoveryRepository
        .getTopVersions(30)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Total version count across all tracks.
     */
    val totalVersionCount: StateFlow<Int> = discoveryRepository
        .getTotalVersionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Start the AI discovery process for all imported tracks.
     */
    fun startDiscovery() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                phase = DiscoveryPhase.DISCOVERING,
                processedTracks = 0,
                totalVersionsFound = 0,
                errorMessage = null
            )

            try {
                // Get all imported tracks
                val tracks = mutableListOf<TrackEntity>()
                trackDao.getAllTracks().collect { trackList ->
                    tracks.addAll(trackList)
                    return@collect
                }

                if (tracks.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        phase = DiscoveryPhase.ERROR,
                        errorMessage = "No tracks imported yet. Import your Spotify library first."
                    )
                    return@launch
                }

                _uiState.value = _uiState.value.copy(totalTracks = tracks.size)

                // Process each track through the AI pipeline
                discoveryRepository.discoverVersionsForAll(
                    tracks = tracks,
                    onProgress = { processed, total, versionsFound ->
                        val currentTrack = tracks.getOrNull(processed - 1)
                        _uiState.value = _uiState.value.copy(
                            processedTracks = processed,
                            totalTracks = total,
                            totalVersionsFound = versionsFound,
                            currentTrackName = currentTrack?.let { "${it.title} — ${it.artist}" } ?: ""
                        )
                    }
                )

                _uiState.value = _uiState.value.copy(phase = DiscoveryPhase.COMPLETED)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = DiscoveryPhase.ERROR,
                    errorMessage = e.message ?: "Discovery failed"
                )
            }
        }
    }

    /**
     * Discover versions for a single track.
     */
    fun discoverForTrack(track: TrackEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                phase = DiscoveryPhase.DISCOVERING,
                currentTrackName = "${track.title} — ${track.artist}",
                totalTracks = 1,
                processedTracks = 0
            )

            try {
                val count = discoveryRepository.discoverVersionsForTrack(track)
                _uiState.value = _uiState.value.copy(
                    phase = DiscoveryPhase.COMPLETED,
                    processedTracks = 1,
                    totalVersionsFound = count
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    phase = DiscoveryPhase.ERROR,
                    errorMessage = e.message
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = DiscoveryUiState()
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(
            phase = DiscoveryPhase.IDLE,
            errorMessage = null
        )
    }
}
