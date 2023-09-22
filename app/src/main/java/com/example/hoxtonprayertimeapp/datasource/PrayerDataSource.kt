package com.example.hoxtonprayertimeapp.datasource

import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import java.time.LocalDate

interface PrayerDataSource {

    suspend fun getPrayerBeginningTimesFromRemoteNetwork(localDate:LocalDate):LondonPrayersBeginningTimes

    suspend fun insertTodayPrayerToLocalDataSource(todayPrayerFromApi: LondonPrayersBeginningTimes)

    suspend fun deleteYesterdayPrayerFromLocalDataSource(yesterdayDate:String)

    suspend fun updateMaghribJamaahTimeInLocalDataSource(maghribJamaahTime: String?, todayLocalDate: String)

}