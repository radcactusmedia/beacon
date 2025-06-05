package com.example.tvapp

import kotlinx.serialization.Serializable

@Serializable
data class MessageData(
    val version: Int,
    val message: String
)

@Serializable
data class VideoItem(
    val id: String,
    val title: String,
    val url: String
)

@Serializable
data class Playlist(
    val version: Int,
    val items: List<VideoItem>
)

data class UiState(
    val message: String = "",
    val playlist: Playlist = Playlist(0, emptyList())
)
