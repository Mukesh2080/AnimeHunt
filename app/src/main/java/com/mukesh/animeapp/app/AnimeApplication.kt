package com.mukesh.animeapp.app

import android.app.Application
import com.mukesh.animeapp.util.NetworkMonitor

class AnimeApplication : Application() {

    lateinit var networkMonitor: NetworkMonitor
        private set

    override fun onCreate() {
        super.onCreate()
        networkMonitor = NetworkMonitor(this)
    }
}