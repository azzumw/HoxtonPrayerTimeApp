package com.example.hoxtonprayertimeapp.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.example.hoxtonprayertimeapp.database.PrayerDao
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.utils.getTodayDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class LocalDataSource(private val prayerDao: PrayerDao,private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO):PrayerDataSource {

    val todayPrayersFromLocalDataSource : LiveData<LondonPrayersBeginningTimes?> = prayerDao.getTodayPrayers(
        getTodayDate(LocalDate.now())
    ).asLiveData()

    override suspend fun insertTodayPrayerToLocalDataSource(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        withContext(ioDispatcher) {
            prayerDao.insertPrayer(todayPrayerFromApi)
        }
    }

    override suspend fun deleteYesterdayPrayerFromLocalDataSource(yesterdayDate: String) {
        withContext(ioDispatcher) {
            prayerDao.deleteYesterdayPrayers(yesterdayDate)
        }
    }

    override suspend fun updateMaghribJamaahTimeInLocalDataSource(
        maghribJamaahTime: String?,
        todayLocalDate: String
    ) {
        withContext(ioDispatcher) {
            prayerDao.updateMaghribJamaah(
                magribJamaahTime = maghribJamaahTime,
                todayDate = todayLocalDate
            )
        }
    }

    override fun getTodayJamaahTimesFromFireStore() {
        TODO("Not Required")
    }

    override fun writeJamaahTimesToFireStore() {
        TODO("Not Required")
    }

    override suspend fun getPrayerBeginningTimesFromRemoteNetwork(localDate: LocalDate): LondonPrayersBeginningTimes {
        TODO("Not Required")
    }

}