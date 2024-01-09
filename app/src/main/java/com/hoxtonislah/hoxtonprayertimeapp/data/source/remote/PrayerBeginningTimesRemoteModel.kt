package com.hoxtonislah.hoxtonprayertimeapp.data.source.remote

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.hoxtonislah.hoxtonprayertimeapp.utils.fromLocalTimeToString
import com.hoxtonislah.hoxtonprayertimeapp.utils.fromStringToLocalTime

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
    val isha: String?,
) {


    var magribJamaah: String? = null

    fun getMaghribJamaahTime(): String? {
        magribJamaah = fromStringToLocalTime(magrib, TWO_MINS).toString()
        return magribJamaah
    }
}

fun LondonPrayersBeginningTimes.convertTo12hour(): LondonPrayersBeginningTimes {
    val fajrTo12HourFormat = fromLocalTimeToString(fromStringToLocalTime(fajr))
    val sunriseTo12HourFormat = fromLocalTimeToString(fromStringToLocalTime(sunrise))
    val dhuhrTo12HourFormat = fromLocalTimeToString(fromStringToLocalTime(dhuhr))
    val asrTo12HourFormat = fromLocalTimeToString(fromStringToLocalTime(asr))
    val magribTo12HourFormat = fromLocalTimeToString(fromStringToLocalTime(magrib))
    val ishaTo12HourFormat = fromLocalTimeToString(fromStringToLocalTime(isha))
    val magribJamaahTo12HourFormat = fromLocalTimeToString(fromStringToLocalTime(magribJamaah))

    val obj = LondonPrayersBeginningTimes(
        date,
        fajr = fajrTo12HourFormat,
        dhuhr = dhuhrTo12HourFormat,
        asr = asrTo12HourFormat,
        magrib = magribTo12HourFormat,
        isha = ishaTo12HourFormat,
        sunrise = sunriseTo12HourFormat
    )
    obj.magribJamaah = magribJamaahTo12HourFormat

    return obj
}


