package com.example.hoxtonprayertimeapp

import android.app.Application
import com.example.hoxtonprayertimeapp.ui.prayer.PrayerViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class PrayerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        val appModule = module {
            viewModel{
                PrayerViewModel()
            }
        }

        startKoin{
            androidContext(this@PrayerApplication)
            modules(appModule)
        }
    }
}