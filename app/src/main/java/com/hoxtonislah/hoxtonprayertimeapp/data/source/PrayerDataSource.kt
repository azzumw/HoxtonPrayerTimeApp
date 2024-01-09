package com.hoxtonislah.hoxtonprayertimeapp.data.source

import com.hoxtonislah.hoxtonprayertimeapp.data.source.remote.LondonPrayersBeginningTimes
import java.time.LocalDate

interface PrayerDataSource {

    suspend fun getPrayerBeginTimesFromRemoteApi(localDate:LocalDate): LondonPrayersBeginningTimes

    suspend fun insertTodayPrayerToLocalDataSource(todayPrayerFromApi: LondonPrayersBeginningTimes)

    suspend fun deleteYesterdayPrayerFromLocalDataSource(yesterdayDate:String)

    suspend fun updateMaghribJamaahTimeInLocalDataSource(maghribJamaahTime: String?, todayLocalDate: String)

    fun getTodayJamaahTimesFromCloud(workoutNextJamaah: () -> Unit)

    fun writeJamaahTimesToCloud()
}