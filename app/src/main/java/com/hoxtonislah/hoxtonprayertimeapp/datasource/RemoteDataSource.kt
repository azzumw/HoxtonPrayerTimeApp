package com.hoxtonislah.hoxtonprayertimeapp.datasource

import com.hoxtonislah.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import com.hoxtonislah.hoxtonprayertimeapp.network.PrayersApi
import com.hoxtonislah.hoxtonprayertimeapp.utils.getTodayDate
import java.time.LocalDate

class RemoteDataSource(private val prayersApiService: PrayersApi) : PrayerDataSource {

    override suspend fun getPrayerBeginningTimesFromRemoteNetwork(localDate: LocalDate): LondonPrayersBeginningTimes {
        return prayersApiService.retrofitService.getTodaysPrayerBeginningTimes(
            date = getTodayDate(
               localDate
            )
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

    override fun getTodayJamaahTimes(func: () -> Unit) {
        TODO("Not required")
    }

    override fun writeJamaahTimes() {
        TODO("Not required")
    }

}