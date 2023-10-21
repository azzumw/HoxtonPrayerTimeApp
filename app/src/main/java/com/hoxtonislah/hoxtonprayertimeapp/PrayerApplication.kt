package com.hoxtonislah.hoxtonprayertimeapp

import android.app.Application
import com.hoxtonislah.hoxtonprayertimeapp.database.PrayerBeginningTimesDatabase
import com.hoxtonislah.hoxtonprayertimeapp.datasource.FireStoreDataSource
import com.hoxtonislah.hoxtonprayertimeapp.datasource.LocalDataSource
import com.hoxtonislah.hoxtonprayertimeapp.datasource.RemoteDataSource
import com.hoxtonislah.hoxtonprayertimeapp.network.PrayersApi
import com.hoxtonislah.hoxtonprayertimeapp.repository.Repository
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
            single { FireStoreDataSource(FirebaseFirestore.getInstance()) }
            single { Repository(get(),get(), get()) }
            viewModel { PrayerViewModel(get()) }
        }

        startKoin {
            androidContext(this@PrayerApplication)
            modules(appModule)
        }
    }
}