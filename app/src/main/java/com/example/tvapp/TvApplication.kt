package com.example.tvapp

import android.app.Application

class TvApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        RefreshWorker.schedule(this)
    }
}
