package com.example.hoxtonprayertimeapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.hoxtonprayertimeapp.database.PrayerDao
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.network.PrayersApi
import com.example.hoxtonprayertimeapp.utils.getTodayDate
import com.example.hoxtonprayertimeapp.utils.getYesterdayDate
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class Repository(
    private val prayerDao: PrayerDao,
    val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val todaysBeginningTimesFromDB : LiveData<LondonPrayersBeginningTimes?> =
        prayerDao.getTodayPrayers(
            getTodayDate(LocalDate.now())
        ).asLiveData()

    suspend fun getPrayerBeginningTimesFromLondonApi(localDate: LocalDate){
        val apiResult = PrayersApi.retrofitService.getTodaysPrayerBeginningTimes( date = getTodayDate(localDate))
    }

    suspend fun deleteYesterdayPrayer(yesterdayDate: String = getYesterdayDate()) {
        prayerDao.deleteYesterdayPrayers(yesterdayDate)
    }

    suspend fun insertTodayPrayer(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        withContext(ioDispatcher) {
            prayerDao.insertPrayer(todayPrayerFromApi)
        }
    }

    suspend fun updateMaghribJamaahTime(maghribJamaahTime: String?, todayLocalDate: String) {
        prayerDao.updateMaghribJamaah(
            magribJamaahTime = maghribJamaahTime,
            todayDate = todayLocalDate
        )
    }

    fun getJamaahTimesFromFireStore() {

    }
}