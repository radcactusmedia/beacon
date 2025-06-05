package com.example.tvapp

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class RefreshWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val repo = ContentRepository(applicationContext)
        val message = repo.fetchMessage()
        val cachedMessage = repo.getMessage()
        if (message.version > cachedMessage.version) {
            repo.saveMessage(message)
        }
        val playlist = repo.fetchPlaylist()
        val cachedPlaylist = repo.getPlaylist()
        if (playlist.version > cachedPlaylist.version) {
            repo.savePlaylist(playlist)
        }
        return Result.success()
    }

    companion object {
        private const val WORK_NAME = "content_refresh"
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<RefreshWorker>(30, TimeUnit.MINUTES).build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
