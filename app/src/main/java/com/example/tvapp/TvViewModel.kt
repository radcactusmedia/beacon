package com.example.tvapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TvViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = ContentRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val message = repo.getMessage()
            val playlist = repo.getPlaylist()
            _uiState.value = UiState(message.message, playlist)
        }
    }

    fun play(item: VideoItem) {
        _uiState.value = _uiState.value.copy()
        // Player state handled in PlayerView
    }
}
