package com.example.tvapp

import android.content.Context
import android.net.Uri
import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadManager
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.ui.PlayerView
import java.io.File

@Composable
fun PlayerView(viewModel: TvViewModel) {
    val context = LocalContext.current
    val player = remember { ExoPlayer.Builder(context).build() }
    AndroidView(factory = {
        PlayerView(context).apply {
            this.player = player
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    })

    val uiState = viewModel.uiState.collectAsState().value
    val current = uiState.playlist.items.firstOrNull()
    if (current != null) {
        val mediaItem = MediaItem.fromUri(Uri.parse(current.url))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = true
        cacheVideo(context, current.url)
    }
}

private fun cacheVideo(context: Context, url: String) {
    val downloadDir = File(context.filesDir, "downloads").apply { mkdirs() }
    val dm = DownloadManager(context, downloadDir, DefaultDownloadIndex(NoopDatabaseProvider()), DefaultCacheDataSource.Factory().setCache(SimpleCache(downloadDir, NoOpCacheEvictor()), DefaultDataSource.Factory(context)))
    val request = DownloadRequest.Builder(url, Uri.parse(url)).build()
    dm.addDownload(request)
}
