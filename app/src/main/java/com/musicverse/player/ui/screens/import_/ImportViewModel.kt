package com.musicverse.player.ui.screens.import_

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.data.repository.SpotifyImportRepository
import com.musicverse.player.util.SpotifyAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for the Import Screen.
 */
data class ImportUiState(
    val phase: ImportPhase = ImportPhase.NOT_CONNECTED,
    val importedCount: Int = 0,
    val totalCount: Int = 0,
    val currentPlaylistName: String = "",
    val recentlyImported: List<TrackEntity> = emptyList(),
    val errorMessage: String? = null,
    val userDisplayName: String? = null,
    // Search
    val searchQuery: String = "",
    val searchResults: List<TrackEntity> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null
)

enum class ImportPhase {
    NOT_CONNECTED,    // No Spotify auth
    READY,            // Authenticated, ready to import
    IMPORTING,        // Currently fetching pages
    COMPLETED,        // Import done
    ERROR             // Something went wrong
}

/**
 * ViewModel driving the Smart Importer screen.
 *
 * Manages Spotify authentication state, triggers paginated import
 * of Liked Songs + all playlists, and exposes reactive progress to the UI.
 */
@HiltViewModel
class ImportViewModel @Inject constructor(
    private val authManager: SpotifyAuthManager,
    private val repository: SpotifyImportRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportUiState())
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    /**
     * Observe imported tracks from Room (live updates).
     */
    val importedTracks = repository.getAllTracks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val trackCount = repository.getTrackCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        // Check auth state on creation
        viewModelScope.launch {
            authManager.isAuthenticated.collect { authenticated ->
                if (authenticated) {
                    val name = authManager.userDisplayName
                    name.collect { displayName ->
                        _uiState.value = _uiState.value.copy(
                            phase = if (_uiState.value.phase == ImportPhase.IMPORTING ||
                                _uiState.value.phase == ImportPhase.COMPLETED)
                                _uiState.value.phase else ImportPhase.READY,
                            userDisplayName = displayName
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(phase = ImportPhase.NOT_CONNECTED)
                }
            }
        }
    }

    /**
     * Get the Spotify login intent.
     */
    fun getSpotifyAuthIntent(): Intent = authManager.buildAuthIntent()

    /**
     * Handle the OAuth callback deep link.
     */
    fun handleAuthCallback(uri: android.net.Uri) {
        viewModelScope.launch {
            val success = authManager.handleAuthCallback(uri)
            if (success) {
                _uiState.value = _uiState.value.copy(phase = ImportPhase.READY)
            } else {
                _uiState.value = _uiState.value.copy(
                    phase = ImportPhase.ERROR,
                    errorMessage = "Spotify authentication failed. Please try again."
                )
            }
        }
    }

    /**
     * Start full library import: Liked Songs + All Playlists.
     */
    fun startImport() {
        if (_uiState.value.phase == ImportPhase.IMPORTING) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                phase = ImportPhase.IMPORTING,
                importedCount = 0,
                totalCount = 0,
                currentPlaylistName = "Liked Songs",
                errorMessage = null
            )

            val result = repository.importFullLibrary { phase, imported, total ->
                _uiState.value = _uiState.value.copy(
                    importedCount = imported,
                    totalCount = total,
                    currentPlaylistName = phase
                )
            }

            result.fold(
                onSuccess = { count ->
                    _uiState.value = _uiState.value.copy(
                        phase = ImportPhase.COMPLETED,
                        importedCount = count,
                        totalCount = count
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        phase = ImportPhase.ERROR,
                        errorMessage = error.message ?: "Import failed"
                    )
                }
            )
        }
    }

    /**
     * Re-import: clear cache and start fresh.
     */
    fun reImport() {
        viewModelScope.launch {
            repository.clearAllTracks()
            startImport()
        }
    }

    /**
     * Disconnect Spotify account.
     */
    fun disconnect() {
        viewModelScope.launch {
            authManager.logout()
            repository.clearAllTracks()
            _uiState.value = ImportUiState(phase = ImportPhase.NOT_CONNECTED)
        }
    }

    /**
     * Dismiss error and return to appropriate state.
     */
    fun dismissError() {
        viewModelScope.launch {
            authManager.isAuthenticated.collect { auth ->
                _uiState.value = _uiState.value.copy(
                    phase = if (auth) ImportPhase.READY else ImportPhase.NOT_CONNECTED,
                    errorMessage = null
                )
            }
        }
    }

    /**
     * Update the search query text.
     */
    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    /**
     * Search the Spotify catalog.
     */
    fun searchSpotify(query: String) {
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            val result = repository.searchTracks(query)
            result.fold(
                onSuccess = { tracks ->
                    _uiState.value = _uiState.value.copy(
                        searchResults = tracks,
                        isSearching = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        searchError = error.message ?: "Search failed",
                        isSearching = false
                    )
                }
            )
        }
    }

    /**
     * Import a specific search result track into the local library.
     */
    fun importSearchResult(track: TrackEntity) {
        viewModelScope.launch {
            repository.importTrack(track)
        }
    }
}
