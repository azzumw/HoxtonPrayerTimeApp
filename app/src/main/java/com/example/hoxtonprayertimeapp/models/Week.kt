package com.example.hoxtonprayertimeapp.models

import android.os.Build
import androidx.annotation.RequiresApi
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.SimpleTimeZone

data class Week(
    val fridayDate: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val fajar: String = "",
    val dhuhr: String = "",
    val asr: String ="",
    val maghrib: String = "",
    val isha: String = "",
    val firstJummah: String = "",
    val secondJummah: String? = null,
)

@RequiresApi(Build.VERSION_CODES.O)
fun Week.toTime():String{
    val s = "04:00"
    val hours = s.substringBefore(":")
    val mins = s.substringAfter(":")

    val date = Timestamp.from(Instant.now())
    val df = SimpleDateFormat("hh:mm a").format(date)

    return df.toString()
}
