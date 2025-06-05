package com.example.tvapp

import android.content.Context
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

class ContentRepository(context: Context) {
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }
    private val cacheDir = File(context.filesDir, "cache").apply { mkdirs() }

    private val messageFile = File(cacheDir, "message.json")
    private val playlistFile = File(cacheDir, "playlist.json")

    suspend fun getMessage(): MessageData = withContext(Dispatchers.IO) {
        if (messageFile.exists()) {
            runCatching {
                Json.decodeFromString<MessageData>(messageFile.readText())
            }.getOrNull()?.let { return@withContext it }
        }
        fetchMessage().also { saveMessage(it) }
    }

    suspend fun getPlaylist(): Playlist = withContext(Dispatchers.IO) {
        if (playlistFile.exists()) {
            runCatching {
                Json.decodeFromString<Playlist>(playlistFile.readText())
            }.getOrNull()?.let { return@withContext it }
        }
        fetchPlaylist().also { savePlaylist(it) }
    }

    suspend fun fetchMessage(): MessageData {
        return client.get(MESSAGE_URL).body()
    }

    suspend fun fetchPlaylist(): Playlist {
        return client.get(PLAYLIST_URL).body()
    }

    fun saveMessage(data: MessageData) {
        messageFile.writeText(Json.encodeToString(MessageData.serializer(), data))
    }

    fun savePlaylist(data: Playlist) {
        playlistFile.writeText(Json.encodeToString(Playlist.serializer(), data))
    }

    companion object {
        const val MESSAGE_URL = "https://raw.githubusercontent.com/example/repo/main/message.json"
        const val PLAYLIST_URL = "https://raw.githubusercontent.com/example/repo/main/playlist.json"
    }
}
