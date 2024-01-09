package com.hoxtonislah.hoxtonprayertimeapp

import android.app.Application
import com.hoxtonislah.hoxtonprayertimeapp.data.source.local.PrayerBeginningTimesDatabase
import com.hoxtonislah.hoxtonprayertimeapp.data.source.cloud.CloudDataSource
import com.hoxtonislah.hoxtonprayertimeapp.data.source.local.LocalDataSource
import com.hoxtonislah.hoxtonprayertimeapp.data.source.remote.RemoteDataSource
import com.hoxtonislah.hoxtonprayertimeapp.data.source.remote.PrayersApi
import com.hoxtonislah.hoxtonprayertimeapp.data.source.Repository
import com.hoxtonislah.hoxtonprayertimeapp.ui.prayer.PrayerViewModel
import com.google.firebase.firestore.FirebaseFirestore
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
            single { CloudDataSource(FirebaseFirestore.getInstance()) }
            single { Repository(get(),get(), get()) }
            viewModel { PrayerViewModel(get()) }
        }

        startKoin {
            androidContext(this@PrayerApplication)
            modules(appModule)
        }
    }
}