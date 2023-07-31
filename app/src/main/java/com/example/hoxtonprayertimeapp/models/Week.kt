package com.example.hoxtonprayertimeapp.models

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Calendar
import java.util.Date
import java.util.SimpleTimeZone
import com.google.firebase.Timestamp
import java.time.LocalTime

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