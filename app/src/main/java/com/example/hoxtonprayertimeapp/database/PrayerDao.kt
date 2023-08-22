package com.example.hoxtonprayertimeapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.hoxtonprayertimeapp.network.LondonPrayersBeginningTimes
import kotlinx.coroutines.flow.Flow

@Dao
interface PrayerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayers: LondonPrayersBeginningTimes)

    @Query("select * from london_prayers_beginning_times where date =:today")
    fun getTodayPrayers(today:String): Flow<LondonPrayersBeginningTimes>

    @Query("delete from london_prayers_beginning_times")
    fun clear()
}