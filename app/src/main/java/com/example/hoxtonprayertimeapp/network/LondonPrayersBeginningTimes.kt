package com.example.hoxtonprayertimeapp.network

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hoxtonprayertimeapp.utils.formatStringToDate
import java.text.SimpleDateFormat
import java.util.Calendar

private const val TWO_MINS = 2

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

    /*
    * Use below for database approach.*/
    fun getMaghribJamaahTime(): String? {
        val tempMaghrib = "$magrib pm"

        val formattedDate = formatStringToDate(tempMaghrib)

        val maghribJamaahTime = Calendar.getInstance().apply {
            time = formattedDate!!
            add(Calendar.MINUTE, TWO_MINS)
        }.time

        return  SimpleDateFormat("hh:mm a").format(maghribJamaahTime).lowercase()
    }
}


