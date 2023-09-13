package com.example.hoxtonprayertimeapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hoxtonprayertimeapp.utils.fromLocalTimeToString
import com.example.hoxtonprayertimeapp.utils.fromStringToLocalTime

private const val TWO_MINS = 2L

@Entity(tableName = "london_prayers_beginning_times")
data class LondonPrayersBeginningTimes(
    @PrimaryKey
    val date: String,
    val fajr: String?,
    val sunrise: String?,
    val dhuhr: String?,
    val asr: String?,
    val magrib: String?,
    val isha: String?
) {


    var magribJamaah: String? = null

    fun getMaghribJamaahTime():String? {
        magribJamaah = fromStringToLocalTime(magrib, TWO_MINS).toString()
       return magribJamaah
    }
}

fun LondonPrayersBeginningTimes.convertTo12hour(pattern:String="hh:mm a"):LondonPrayersBeginningTimes{
    val f = fromLocalTimeToString(fromStringToLocalTime(fajr),pattern)
    val sun = fromLocalTimeToString(fromStringToLocalTime(sunrise),pattern)
    val d = fromLocalTimeToString(fromStringToLocalTime(dhuhr),pattern)
    val a = fromLocalTimeToString(fromStringToLocalTime(asr),pattern)
    val m = fromLocalTimeToString(fromStringToLocalTime(magrib),pattern)
    val i = fromLocalTimeToString(fromStringToLocalTime(isha),pattern)
//    this.magribJamaah = fromLocalTimeToString(fromStringToLocalTime(magribJamaah),pattern)
    return LondonPrayersBeginningTimes(fajr = f, sunrise = sun, dhuhr = d, asr = a, magrib = m, isha = i, date = date)
}


