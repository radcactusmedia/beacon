package com.example.tvapp

import android.content.Context
import com.amplifyframework.core.Amplify
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.util.Locale

class ContentRepository(private val context: Context) {
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
        val local = loadLocalPlaylist()
        if (local.items.isNotEmpty()) return@withContext local
        if (playlistFile.exists()) {
            runCatching {
                Json.decodeFromString<Playlist>(playlistFile.readText())
            }.getOrNull()?.let { return@withContext it }
        }
        fetchPlaylist().also { savePlaylist(it) }
    }

    private suspend fun download(key: String, file: File) {
        suspendCancellableCoroutine<Unit> { cont ->
            Amplify.Storage.downloadFile(
                key,
                file,
                { cont.resume(Unit) },
                { error -> cont.resumeWithException(error) }
            )
        }
    }

    suspend fun fetchMessage(): MessageData {
        download(MESSAGE_KEY, messageFile)
        return Json.decodeFromString(messageFile.readText())
    }

    suspend fun fetchPlaylist(): Playlist {
        download(PLAYLIST_KEY, playlistFile)
        return Json.decodeFromString(playlistFile.readText())
    }

    fun saveMessage(data: MessageData) {
        messageFile.writeText(Json.encodeToString(MessageData.serializer(), data))
    }

    fun savePlaylist(data: Playlist) {
        playlistFile.writeText(Json.encodeToString(Playlist.serializer(), data))
    }

    private fun loadLocalPlaylist(): Playlist {
        val dir = File(context.getExternalFilesDir(null), "media")
        if (!dir.exists()) return Playlist(0, emptyList())
        val items = dir.listFiles()?.filter { file ->
            val name = file.name.lowercase(Locale.getDefault())
            name.endsWith(".mp4") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")
        }?.map { file ->
            VideoItem(
                id = file.name,
                title = file.name,
                url = file.absolutePath
            )
        } ?: emptyList()
        return Playlist(1, items)
    }

    fun getSplashFile(): File? {
        val f = File(context.getExternalFilesDir(null), "splash")
        return if (f.exists()) f else null
    }

    companion object {
        // Public GitHub repository containing the source code
        const val REPO_URL = "https://github.com/radcactusmedia/beacon.git"
        const val MESSAGE_KEY = "message.json"
        const val PLAYLIST_KEY = "playlist.json"
    }
}
