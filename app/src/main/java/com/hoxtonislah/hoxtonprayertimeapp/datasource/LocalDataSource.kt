package com.hoxtonislah.hoxtonprayertimeapp.datasource

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.hoxtonislah.hoxtonprayertimeapp.database.PrayerDao
import com.hoxtonislah.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class LocalDataSource(
    private val prayerDao: PrayerDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : PrayerDataSource {

    val todayPrayersFromLocalDataSource: LiveData<LondonPrayersBeginningTimes?> =
        prayerDao.getTodayPrayers().asLiveData()

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

    suspend fun clearDatabase(){
        withContext(ioDispatcher){
            prayerDao.clear()
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

    override fun getTodayJamaahTimesFromCloud(workoutNextJamaah: () -> Unit) {
        TODO("Not Required")
    }

    override fun writeJamaahTimesToCloud() {
        TODO("Not Required")
    }

    override suspend fun getPrayerBeginTimesFromRemoteApi(localDate: LocalDate): LondonPrayersBeginningTimes {
        TODO("Not Required")
    }
}