package com.musicverse.player.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.musicverse.player.util.ByokManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val byokManager: ByokManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // Observe saved keys and update state
        byokManager.geminiKey
            .onEach { savedKey ->
                _uiState.value = _uiState.value.copy(
                    geminiKeyInput = savedKey ?: "",
                    geminiKeySaved = savedKey != null
                )
            }.launchIn(viewModelScope)

        byokManager.youTubeKey
            .onEach { savedKey ->
                _uiState.value = _uiState.value.copy(
                    youtubeKeyInput = savedKey ?: "",
                    youtubeKeySaved = savedKey != null
                )
            }.launchIn(viewModelScope)
    }

    fun onGeminiKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(geminiKeyInput = value)
    }

    fun onYoutubeKeyChanged(value: String) {
        _uiState.value = _uiState.value.copy(youtubeKeyInput = value)
    }

    fun saveGeminiKey() = viewModelScope.launch {
        byokManager.setGeminiKey(_uiState.value.geminiKeyInput)
    }

    fun saveYoutubeKey() = viewModelScope.launch {
        byokManager.setYouTubeKey(_uiState.value.youtubeKeyInput)
    }

    fun clearGeminiKey() = viewModelScope.launch {
        byokManager.setGeminiKey("")
        _uiState.value = _uiState.value.copy(geminiKeyInput = "", geminiKeySaved = false)
    }

    fun clearYoutubeKey() = viewModelScope.launch {
        byokManager.setYouTubeKey("")
        _uiState.value = _uiState.value.copy(youtubeKeyInput = "", youtubeKeySaved = false)
    }
}

data class SettingsUiState(
    val geminiKeyInput: String = "",
    val geminiKeySaved: Boolean = false,
    val youtubeKeyInput: String = "",
    val youtubeKeySaved: Boolean = false
)
