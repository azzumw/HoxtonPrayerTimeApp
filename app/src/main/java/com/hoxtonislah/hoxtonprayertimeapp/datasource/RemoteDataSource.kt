package com.hoxtonislah.hoxtonprayertimeapp.datasource

import com.hoxtonislah.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.network.PrayersApi
import java.time.LocalDate

class RemoteDataSource(private val prayersApiService: PrayersApi) : PrayerDataSource {

    override suspend fun getPrayerBeginTimesFromRemoteApi(localDate: LocalDate): LondonPrayersBeginningTimes {
        return prayersApiService.retrofitService.getTodaysPrayerBeginningTimes(
            date = localDate.toString()
        )
    }

    override suspend fun insertTodayPrayerToLocalDataSource(todayPrayerFromApi: LondonPrayersBeginningTimes) {
        TODO("Not required")
    }

    override suspend fun deleteYesterdayPrayerFromLocalDataSource(yesterdayDate: String) {
        TODO("Not required")
    }

    override suspend fun updateMaghribJamaahTimeInLocalDataSource(
        maghribJamaahTime: String?,
        todayLocalDate: String
    ) {
        TODO("Not required")
    }

    override fun getTodayJamaahTimesFromCloud(workoutNextJamaah: () -> Unit) {
        TODO("Not required")
    }

    override fun writeJamaahTimesToCloud() {
        TODO("Not required")
    }

}