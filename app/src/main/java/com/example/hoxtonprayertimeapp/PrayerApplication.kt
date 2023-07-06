package com.example.hoxtonprayertimeapp

import android.app.Application
import timber.log.Timber

class PrayerApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.asTree())
    }
}