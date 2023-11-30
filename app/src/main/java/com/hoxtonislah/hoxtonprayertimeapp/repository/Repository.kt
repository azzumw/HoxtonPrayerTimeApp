package com.hoxtonislah.hoxtonprayertimeapp.repository

import androidx.lifecycle.LiveData
import com.hoxtonislah.hoxtonprayertimeapp.datasource.CloudDataSource
import com.hoxtonislah.hoxtonprayertimeapp.datasource.LocalDataSource
import com.hoxtonislah.hoxtonprayertimeapp.datasource.RemoteDataSource
import com.hoxtonislah.hoxtonprayertimeapp.models.JamaahTimeCloudModel
import com.hoxtonislah.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.utils.getYesterdayDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class Repository(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource,
    private val cloudDataSource: CloudDataSource,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    val jamaahTimeCloudModel: LiveData<JamaahTimeCloudModel?> = cloudDataSource.jamaahTimeCloudModel

    val todayPrayerBeginTimesFromLocal: LiveData<LondonPrayersBeginningTimes?> =
        localDataSource.todayPrayersFromLocalDataSource

    suspend fun getBeginPrayerTimesFromRemote(localDate: LocalDate): LondonPrayersBeginningTimes {
        return remoteDataSource.getPrayerBeginTimesFromRemoteApi(localDate)
    }

//    suspend fun getBeginPrayerTimesFromLocal(localDate: LocalDate):LondonPrayersBeginningTimes?{
//        return localDataSource.getTodayPrayerFromLocalDataSource(localDate)
//    }

    suspend fun deleteYesterdayBeginPrayerTimesFromLocal(yesterdayDate: LocalDate) {
        withContext(ioDispatcher) {
            localDataSource.deleteYesterdayPrayerFromLocalDataSource(getYesterdayDate(yesterdayDate))
        }
    }

    suspend fun clearLocalData(){
        withContext(ioDispatcher){
            localDataSource.clearDatabase()
        }
    }

    suspend fun insertTodayBeginPrayerTimesIntoLocal(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        withContext(ioDispatcher) {
            localDataSource.insertTodayPrayerToLocalDataSource(todayPrayerFromApi)
        }
    }

    suspend fun updateMaghribJamaahTimeForTodayPrayerLocal(maghribJamaahTime: String?, todayLocalDate: String) {
        withContext(ioDispatcher) {
            localDataSource.updateMaghribJamaahTimeInLocalDataSource(
                maghribJamaahTime = maghribJamaahTime,
                todayLocalDate = todayLocalDate
            )
        }
    }

    fun getJamaahTimesFromCloud(workoutNextJamaah: () -> Unit) =
        cloudDataSource.getTodayJamaahTimesFromCloud(workoutNextJamaah)


    fun writeJamaahTimesToCloud() =
        cloudDataSource.writeJamaahTimesToCloud()

    fun clear() = cloudDataSource.clear()

}
