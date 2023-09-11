package com.example.hoxtonprayertimeapp.network

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hoxtonprayertimeapp.utils.fromStringToLocalTime
import timber.log.Timber

private const val TWO_MINS = 2L

@Entity(tableName = "london_prayers_beginning_times")
data class LondonPrayersBeginningTimes(
    @PrimaryKey
    val date: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val magrib: String,
    val isha: String
) {


    var magribJamaah: String? = null

    fun getMaghribJamaahTime():String {
        val tempMaghrib = "$magrib:00"
       return fromStringToLocalTime(tempMaghrib, TWO_MINS).toString()
    }
}


