package com.example.hoxtonprayertimeapp.models

import android.util.Log
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar

data class Week(
    val fridayDate: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val fajar: String?=null,
    val dhuhr: String = "",
    val asr: String ="",
    val maghrib: String = "",
    val isha: String = "",
    val firstJummah: String = "",
    val secondJummah: String? = null,
)


fun convert(time:Timestamp?):String{
    return if(time!=null) {
        val toDate = time.toDate().time
        SimpleDateFormat("hh:mm a").format(toDate)
    } else "00:00"
}

fun fromStringToDateTime(): String? {
    val str = "04:30 am"
    val formatter = SimpleDateFormat("hh:mm a")
    val date = formatter.parse(str)
    return formatter.format(date)
}

//val time = Timestamp.now().toDate()
//val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
//val f = df.format(time)
//Timber.e("time: $f")
