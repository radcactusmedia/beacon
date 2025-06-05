package com.example.tvapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TvViewModel(application: Application) : AndroidViewModel(application) {
    val repo = ContentRepository(application)

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            val playlist = repo.getPlaylist()
            val shuffled = playlist.items.shuffled()
            _uiState.value = UiState(playlist = playlist.copy(items = shuffled))
        }
    }

    fun play(item: VideoItem) {
        _uiState.value = _uiState.value.copy()
        // Player state handled in PlayerView
    }
}
