package com.example.hoxtonprayertimeapp.repository

import androidx.lifecycle.LiveData
import com.example.hoxtonprayertimeapp.datasource.FireStoreDataSource
import com.example.hoxtonprayertimeapp.datasource.LocalDataSource
import com.example.hoxtonprayertimeapp.datasource.RemoteDataSource
import com.example.hoxtonprayertimeapp.models.FireStoreWeekModel
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.example.hoxtonprayertimeapp.utils.getYesterdayDate
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

    fun getJamaahTimesFromFireStore(func: () -> Unit) =
        fireStoreDataSource.getTodayJamaahTimes(func)


    fun writeJamaahTimesToFireStore() =
        fireStoreDataSource.writeJamaahTimes()

    fun clear() = fireStoreDataSource.clear()

}
