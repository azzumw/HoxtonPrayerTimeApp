package com.example.hoxtonprayertimeapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayers: LondonPrayersBeginningTimes)

    @Query("select * from london_prayers_beginning_times where date =:today")
    fun getTodayPrayers(today:String): Flow<LondonPrayersBeginningTimes>

    @Query("update london_prayers_beginning_times set magribJamaah = :magribJamaahTime where date = :todayDate ")
    suspend fun updateMaghribJamaah(magribJamaahTime:String,todayDate:String)
    @Query("delete from london_prayers_beginning_times where date = :yesterday")
    suspend fun deleteYesterdayPrayers(yesterday:String)
    @Query("delete from london_prayers_beginning_times")
    fun clear()
}