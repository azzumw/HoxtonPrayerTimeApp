package com.example.hoxtonprayertimeapp.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

data class Week(
    val fridayDate: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val fajar: String? = null,
    val dhuhr: String = "",
    val asr: String = "",
    val maghrib: String = "",
    val isha: String = "",
    val firstJummah: String = "",
    val secondJummah: String? = null,
) {
}


fun convert(time: Timestamp?): String {
    return if (time != null) {
        val toDate = time.toDate().time
        SimpleDateFormat("hh:mm a").format(toDate)
    } else "00:00"
}

fun fromStringToDateTime(time: String): String {

    val formatter = SimpleDateFormat("hh:mm a")
    val date = formatter.parse(time)
    return formatter.format(date)
}

fun fromStringToDateTimeObj(timeStr: String?): Date {
    val today = Calendar.getInstance().time
    val formatter2 = SimpleDateFormat("dd/MM/yyyy hh:mm a")
    val formatter = SimpleDateFormat("dd/MM/yyyy")
    val fDate = formatter.format(today)
    val dateString = "$fDate $timeStr"

    return formatter2.parse(dateString)

}

//val time = Timestamp.now().toDate()
//val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
//val f = df.format(time)
//Timber.e("time: $f")
