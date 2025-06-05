package com.example.tvapp

import android.net.Uri
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.SubcomposeAsyncImage
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerView
import kotlinx.coroutines.delay
import java.io.File
import kotlin.random.Random

@Composable
fun SlideshowPlayer(viewModel: TvViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val splashFile = remember { viewModel.repo.getSplashFile() }
    var showSplash by remember { mutableStateOf(splashFile != null) }
    var showLoading by remember { mutableStateOf(false) }
    var playlist by remember { mutableStateOf(uiState.playlist.items) }
    var index by remember { mutableStateOf(0) }
    val player = remember { ExoPlayer.Builder(context).build() }

    LaunchedEffect(uiState.playlist) {
        playlist = uiState.playlist.items
        index = 0
    }

    LaunchedEffect(showSplash) {
        if (showSplash) {
            delay(10_000)
            showSplash = false
            showLoading = true
        }
    }

    LaunchedEffect(showLoading) {
        if (showLoading) {
            delay(3000)
            showLoading = false
        }
    }

    fun shuffle(prevId: String?): List<VideoItem> {
        if (uiState.playlist.items.isEmpty()) return emptyList()
        var newList = uiState.playlist.items.shuffled(Random(System.currentTimeMillis()))
        if (prevId != null && newList.size > 1 && newList.first().id == prevId) {
            val m = newList.toMutableList()
            val tmp = m[0]
            m[0] = m[1]
            m[1] = tmp
            newList = m
        }
        return newList
    }

    fun next() {
        val prev = playlist.getOrNull(index)
        index++
        if (index >= playlist.size) {
            playlist = shuffle(prev?.id)
            index = 0
        }
    }

    when {
        showSplash && splashFile != null -> {
            MediaItemView(file = splashFile, player = player)
        }
        showLoading -> {
            LoadingScreen()
        }
        playlist.isNotEmpty() -> {
            val current = playlist[index]
            MediaItemView(file = File(current.url), player = player, onFinish = { next() })
        }
    }
}

@Composable
private fun MediaItemView(file: File, player: ExoPlayer, onFinish: (() -> Unit)? = null) {
    val isVideo = file.extension.lowercase() == "mp4"
    if (isVideo) {
        var listener: Player.Listener? = null
        AndroidView(factory = {
            PlayerView(it).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                this.player = player
            }
        }, update = {
            val item = MediaItem.fromUri(Uri.fromFile(file))
            player.setMediaItem(item)
            player.prepare()
            player.playWhenReady = true
            listener?.let { player.removeListener(it) }
            listener = object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) {
                        onFinish?.invoke()
                    }
                }
            }
            player.addListener(listener!!)
        })
    } else {
        SubcomposeAsyncImage(model = file, contentDescription = null, modifier = Modifier.fillMaxSize())
        LaunchedEffect(file) {
            delay(10_000)
            onFinish?.invoke()
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
