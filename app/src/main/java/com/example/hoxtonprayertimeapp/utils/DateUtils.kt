package com.example.hoxtonprayertimeapp.utils

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.util.Log
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GREGORIAN_DATE_FORMAT = "EEE dd MMM yyyy"
const val DATE_PATTERN = "yyyy-MM-dd"
fun getCurrentGregorianDate(): String = SimpleDateFormat(
    GREGORIAN_DATE_FORMAT,
    Locale.getDefault()
).format(java.util.Calendar.getInstance().time) //or use getDateInstance()


fun getCurrentIslamicDate(): String {
    val locale = ULocale("@calendar=islamic-umalqura")
    val islamic = Calendar.getInstance(locale) as IslamicCalendar

    islamic.calculationType = IslamicCalendar.CalculationType.ISLAMIC_UMALQURA
    val todayDate = islamic.get(IslamicCalendar.DATE)
    val date = if (todayDate < 10) "0${todayDate}" else "$todayDate"
    val month = getIslamicMonth(islamic.get(IslamicCalendar.MONTH))
    val year = islamic.get(IslamicCalendar.YEAR)

    return "$date $month $year"
}


private fun getIslamicMonth(month: Int): String {
    return when (month) {
        IslamicCalendar.MUHARRAM -> "Muharram"
        IslamicCalendar.SAFAR -> "Safar"
        IslamicCalendar.RABI_1 -> "Rabi ul Awal"
        IslamicCalendar.RABI_2 -> "Rabi uth Thani"
        IslamicCalendar.JUMADA_1 -> "Jumada Awal"
        IslamicCalendar.JUMADA_2 -> "Jumada Thani"
        IslamicCalendar.RAJAB -> "Rajab"
        IslamicCalendar.SHABAN -> "Shabaan"
        IslamicCalendar.RAMADAN -> "Ramdaan"
        IslamicCalendar.SHAWWAL -> "Shawwaal"
        IslamicCalendar.DHU_AL_QIDAH -> "Dhu al Qidah"
        IslamicCalendar.DHU_AL_HIJJAH -> "Dhi al Hijjah"
        else -> "Unknown Month"
    }
}

fun getLastFridayDate(): String {
    val calendar = java.util.Calendar.getInstance(Locale.getDefault())

    val actualCurrentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)

    var tempDay = actualCurrentDay

    while (tempDay != java.util.Calendar.FRIDAY) {

        if (tempDay == 0) {
            //set to saturday
            tempDay = 7
        }
        tempDay--
        calendar.set(java.util.Calendar.DAY_OF_WEEK, tempDay)
    }

    //reduce the week by 1 to get the last friday date because we have moved to this weeks friday in future
    if (actualCurrentDay != java.util.Calendar.SATURDAY) {
        calendar.set(
            java.util.Calendar.WEEK_OF_YEAR,
            calendar.get(java.util.Calendar.WEEK_OF_YEAR) - 1
        )
    }

    val df = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
    return df.format(calendar.time)

}

fun getTodayDate(pattern: String = DATE_PATTERN): String {
    val date = Timestamp.now().toDate()
    val df = SimpleDateFormat(pattern, Locale.getDefault())
    return df.format(date)
}

fun getYesterdayDate(calender: java.util.Calendar, pattern: String = DATE_PATTERN): String {
//    val calender = Calendar.getInstance()
    calender.add(java.util.Calendar.DAY_OF_WEEK,-1)
    val yesterday = calender.time

    val df = SimpleDateFormat(pattern)

    return df.format(yesterday)
}

fun getFridayDate(): String = if (isTodayFriday()) {
    getTodayDate()
} else {
    getLastFridayDate()
}

fun getLastWeek(calender: java.util.Calendar): Int {
    calender.add(java.util.Calendar.WEEK_OF_YEAR, -1)
    return calender.get(java.util.Calendar.WEEK_OF_YEAR)
}


fun createDocumentReferenceIDForLastWeek(calender: java.util.Calendar) =
    getLastWeek(calender).toString()

fun isTodayFriday() = java.util.Calendar.getInstance(Locale.getDefault())
    .get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY

/*
* This is the String representation, converts back to String for Next Prayer
* */
fun formatTimeToString(time: Date?): String? {
    val formatter = SimpleDateFormat("hh:mma")
    if (time != null) {
        val formattedTime = formatter.format(time)
        Log.e("formatStringTime", formattedTime)

        return formattedTime.lowercase()
    }
    return null
}

/*Because the prayer jamaat times from the firestore
* are read in string, hence we need to convert them to
* today's Date objects (to avoid it being set to 1 Jan 1970) ,
* for Next Prayer feature to work.
* */
fun fromStringToDateTimeObj(timeStr: String?): Date? {

    return timeStr?.let {
        val formatter = SimpleDateFormat("dd/MM/yyyy")
        val formatter2 = SimpleDateFormat("dd/MM/yyyy hh:mm a")

        val today = java.util.Calendar.getInstance().time

        val fDate = formatter.format(today)
        val dateString = "$fDate $timeStr"
        Log.e("fromStringToDateObj: ", dateString)
        formatter2.parse(dateString)
    }
}