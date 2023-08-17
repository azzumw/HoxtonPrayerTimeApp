package com.example.hoxtonprayertimeapp.network

import com.example.hoxtonprayertimeapp.utils.fromStringToDateTimeObj
import java.text.SimpleDateFormat
import java.util.Calendar

private const val TWO_MINS = 2

data class LondonPrayersBeginningTimes(
    val date: String,
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val magrib: String,
    val isha: String
)

fun LondonPrayersBeginningTimes.getMaghribJamaatTime(): String {
    val tempMaghrib = "$magrib PM"

    val formattedDate = fromStringToDateTimeObj(tempMaghrib)

    val maghribJamaatTime = Calendar.getInstance().apply {
        time = formattedDate!!
        add(Calendar.MINUTE, TWO_MINS)
    }.time

    return SimpleDateFormat("hh:mm a").format(maghribJamaatTime).lowercase()

}
