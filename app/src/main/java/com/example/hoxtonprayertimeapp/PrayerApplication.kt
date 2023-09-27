package com.example.hoxtonprayertimeapp

import android.app.Application
import com.example.hoxtonprayertimeapp.database.PrayerBeginningTimesDatabase
import com.example.hoxtonprayertimeapp.datasource.FireStoreDataSource
import com.example.hoxtonprayertimeapp.datasource.LocalDataSource
import com.example.hoxtonprayertimeapp.datasource.RemoteDataSource
import com.example.hoxtonprayertimeapp.network.PrayersApi
import com.example.hoxtonprayertimeapp.repository.Repository
import com.example.hoxtonprayertimeapp.ui.prayer.PrayerViewModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import timber.log.Timber

class PrayerApplication : Application() {

    private  val database  by lazy {
        PrayerBeginningTimesDatabase.getDatabase(this)
    }

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())

        val appModule = module {

            single { RemoteDataSource(PrayersApi) }
            single { LocalDataSource(database.prayerDao) }
            single { FireStoreDataSource(Firebase.firestore) }
            single { Repository(get(),get(), get()) }
            viewModel { PrayerViewModel(get()) }
        }

        startKoin {
            androidContext(this@PrayerApplication)
            modules(appModule)
        }
    }
}