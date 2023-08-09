package com.example.hoxtonprayertimeapp.models

data class Week(
    val fridayDate: String? = null,
    val fajar: String? = null,
    val dhuhr: String? = null,
    val asr: String? = null,
    val maghrib: String? = null,
    val isha: String? = null,
    val firstJummah: String? = null,
    val secondJummah: String? = null,
)


//fun convert(time: Timestamp?): String {
//    return if (time != null) {
//        val toDate = time.toDate().time
//        SimpleDateFormat("hh:mm a").format(toDate)
//    } else "00:00"
//}

//val time = Timestamp.now().toDate()
//val df = SimpleDateFormat("hh:mm a", Locale.getDefault())
//val f = df.format(time)
//Timber.e("time: $f")
