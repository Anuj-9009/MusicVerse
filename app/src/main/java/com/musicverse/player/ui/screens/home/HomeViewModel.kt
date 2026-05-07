package com.musicverse.player.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.data.local.VersionDao
import com.musicverse.player.data.local.VersionEntity
import com.musicverse.player.util.SpotifyAuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isAuthenticated: Boolean = false,
    val userDisplayName: String? = null,
    val trackCount: Int = 0,
    val versionCount: Int = 0,
    val avgVibeScore: Int = 0,
    val selectedFilter: Int = 0, // 0=All, 1=Imported, 2=Discovered
    val recentTracks: List<TrackEntity> = emptyList(),
    val topVersions: List<VersionEntity> = emptyList()
)

/**
 * HomeViewModel — Drives the home screen with auth-aware state,
 * live stats from Room, and filtered content for tab groups.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val authManager: SpotifyAuthManager,
    private val trackDao: TrackDao,
    private val versionDao: VersionDao
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(0)

    val uiState: StateFlow<HomeUiState> = combine(
        authManager.isAuthenticated,
        authManager.userDisplayName,
        trackDao.getTrackCount(),
        versionDao.getTotalVersionCount(),
        versionDao.getTopVersions(20),
        trackDao.getAllTracks().map { it.take(20) },
        _selectedFilter
    ) { values ->
        val isAuth = values[0] as Boolean
        val displayName = values[1] as? String
        val trackCount = values[2] as Int
        val versionCount = values[3] as Int
        @Suppress("UNCHECKED_CAST")
        val topVersions = values[4] as List<VersionEntity>
        @Suppress("UNCHECKED_CAST")
        val recentTracks = values[5] as List<TrackEntity>
        val filter = values[6] as Int

        val avgVibe = if (topVersions.isNotEmpty())
            topVersions.map { it.aiVibeScore }.average().toInt() else 0

        HomeUiState(
            isAuthenticated = isAuth,
            userDisplayName = displayName,
            trackCount = trackCount,
            versionCount = versionCount,
            avgVibeScore = avgVibe,
            selectedFilter = filter,
            recentTracks = recentTracks,
            topVersions = topVersions
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    fun setFilter(index: Int) {
        _selectedFilter.value = index
    }
}
