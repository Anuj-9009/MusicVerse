package com.musicverse.player.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.local.TrackEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Sort options for the library.
 */
enum class LibrarySortOption(val label: String) {
    RECENTLY_ADDED("Recent"),
    TITLE_ASC("A → Z"),
    TITLE_DESC("Z → A"),
    ARTIST("Artist")
}

/**
 * UI State for the Library screen.
 */
data class LibraryUiState(
    val searchQuery: String = "",
    val sortOption: LibrarySortOption = LibrarySortOption.RECENTLY_ADDED,
    val isSearchActive: Boolean = false
)

/**
 * ViewModel for the Library screen.
 *
 * Provides:
 *   - Reactive search with 300ms debounce
 *   - Multi-sort options (recent, A-Z, Z-A, by artist)
 *   - Real-time track count
 *   - Filtered + sorted track list
 */
@OptIn(FlowPreview::class)
@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val trackDao: TrackDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    /**
     * All tracks from Room, filtered and sorted reactively.
     */
    val tracks: StateFlow<List<TrackEntity>> = combine(
        trackDao.getAllTracks(),
        _searchQuery.debounce(300),
        _uiState.map { it.sortOption }
    ) { allTracks, query, sortOption ->
        var filtered = allTracks

        // Apply search filter
        if (query.isNotBlank()) {
            val lowerQuery = query.lowercase()
            filtered = filtered.filter { track ->
                track.title.lowercase().contains(lowerQuery) ||
                        track.artist.lowercase().contains(lowerQuery) ||
                        track.album.lowercase().contains(lowerQuery) ||
                        (track.isrc?.lowercase()?.contains(lowerQuery) == true)
            }
        }

        // Apply sort
        when (sortOption) {
            LibrarySortOption.RECENTLY_ADDED -> filtered.sortedByDescending { it.importedAt }
            LibrarySortOption.TITLE_ASC -> filtered.sortedBy { it.title.lowercase() }
            LibrarySortOption.TITLE_DESC -> filtered.sortedByDescending { it.title.lowercase() }
            LibrarySortOption.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Total track count (unfiltered).
     */
    val totalTrackCount: StateFlow<Int> = trackDao.getTrackCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    /**
     * Filtered result count.
     */
    val filteredCount: StateFlow<Int> = tracks.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun updateSortOption(option: LibrarySortOption) {
        _uiState.value = _uiState.value.copy(sortOption = option)
    }

    fun toggleSearch() {
        val newActive = !_uiState.value.isSearchActive
        _uiState.value = _uiState.value.copy(isSearchActive = newActive)
        if (!newActive) {
            updateSearchQuery("")
        }
    }

    fun clearSearch() {
        updateSearchQuery("")
    }
}
