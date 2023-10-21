package com.hoxtonislah.hoxtonprayertimeapp.repository

import androidx.lifecycle.LiveData
import com.hoxtonislah.hoxtonprayertimeapp.datasource.FireStoreDataSource
import com.hoxtonislah.hoxtonprayertimeapp.datasource.LocalDataSource
import com.hoxtonislah.hoxtonprayertimeapp.datasource.RemoteDataSource
import com.hoxtonislah.hoxtonprayertimeapp.models.FireStoreWeekModel
import com.hoxtonislah.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.utils.getYesterdayDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class Repository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val fireStoreDataSource: FireStoreDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val fireStoreWeekModel: LiveData<FireStoreWeekModel?> = fireStoreDataSource.fireStoreWeekModel

    val todaysBeginningTimesFromDB: LiveData<LondonPrayersBeginningTimes?> =
        localDataSource.todayPrayersFromLocalDataSource

    suspend fun getPrayerBeginningTimesFromLondonApi(localDate: LocalDate): LondonPrayersBeginningTimes {
        return remoteDataSource.getPrayerBeginningTimesFromRemoteNetwork(localDate)
    }

    suspend fun deleteYesterdayPrayer(yesterdayDate: String = getYesterdayDate()) {
        withContext(ioDispatcher) {
            localDataSource.deleteYesterdayPrayerFromLocalDataSource(yesterdayDate)
        }
    }

    suspend fun insertTodayPrayer(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        withContext(ioDispatcher) {
            localDataSource.insertTodayPrayerToLocalDataSource(todayPrayerFromApi)
        }
    }

    suspend fun updateMaghribJamaahTime(maghribJamaahTime: String?, todayLocalDate: String) {
        withContext(ioDispatcher) {
            localDataSource.updateMaghribJamaahTimeInLocalDataSource(
                maghribJamaahTime = maghribJamaahTime,
                todayLocalDate = todayLocalDate
            )
        }
    }

    fun getJamaahTimesFromFireStore(workoutNextJamaah: () -> Unit) =
        fireStoreDataSource.getTodayJamaahTimes(workoutNextJamaah)


    fun writeJamaahTimesToFireStore() =
        fireStoreDataSource.writeJamaahTimes()

    fun clear() = fireStoreDataSource.clear()

}
