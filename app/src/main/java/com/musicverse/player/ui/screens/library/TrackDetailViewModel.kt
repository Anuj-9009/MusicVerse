package com.musicverse.player.ui.screens.library

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.local.TrackEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Track Detail screen.
 * Loads a single track by ID from the Room database.
 */
@HiltViewModel
class TrackDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val trackDao: TrackDao
) : ViewModel() {

    private val trackId: String = savedStateHandle.get<String>("trackId") ?: ""

    private val _track = MutableStateFlow<TrackEntity?>(null)
    val track: StateFlow<TrackEntity?> = _track.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadTrack()
    }

    private fun loadTrack() {
        viewModelScope.launch {
            _isLoading.value = true
            _track.value = trackDao.getTrackById(trackId)
            _isLoading.value = false
        }
    }
}
