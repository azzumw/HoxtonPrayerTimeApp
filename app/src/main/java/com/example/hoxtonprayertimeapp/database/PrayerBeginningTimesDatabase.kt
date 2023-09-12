package com.example.hoxtonprayertimeapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.hoxtonprayertimeapp.models.LondonPrayersBeginningTimes

@Database([LondonPrayersBeginningTimes::class], version = 1, exportSchema = false)
abstract class PrayerBeginningTimesDatabase:RoomDatabase() {

    abstract val prayerDao : PrayerDao

    companion object{
        @Volatile
        private var INSTANCE: PrayerBeginningTimesDatabase? = null

        fun getDatabase(context: Context):PrayerBeginningTimesDatabase{
            return INSTANCE?: synchronized(this){
                val instance = Room.databaseBuilder(
                    context,
                    PrayerBeginningTimesDatabase::class.java,
                    "prayer_begin_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance

                instance
            }
        }
    }
}