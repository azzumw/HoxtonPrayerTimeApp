package com.example.hoxtonprayertimeapp.network

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.hoxtonprayertimeapp.utils.fromStringToDateTimeObj
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


    var magribJamaat: String? = null

//    fun getMaghribJamaatTime(): String? {
//        val tempMaghrib = "$magrib pm"
//
//        val formattedDate = fromStringToDateTimeObj(tempMaghrib)
//
//        val maghribJamaatTime = Calendar.getInstance().apply {
//            time = formattedDate!!
//            add(Calendar.MINUTE, TWO_MINS)
//        }.time
//
//        magribJamaat =   SimpleDateFormat("hh:mm a").format(maghribJamaatTime).lowercase()
//        return magribJamaat
//    }


    /*
    * Use below for database approach.*/
    fun getMaghribJamaahTime(): String? {
        val tempMaghrib = "$magrib pm"

        val formattedDate = fromStringToDateTimeObj(tempMaghrib)

        val maghribJamaatTime = Calendar.getInstance().apply {
            time = formattedDate!!
            add(Calendar.MINUTE, TWO_MINS)
        }.time

        return  SimpleDateFormat("hh:mm a").format(maghribJamaatTime).lowercase()
    }
}


