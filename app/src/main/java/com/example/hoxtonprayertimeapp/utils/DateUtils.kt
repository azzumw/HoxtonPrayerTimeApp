package com.example.hoxtonprayertimeapp.utils

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val GREGORIAN_DATE_FORMAT = "EEE dd MMM yyyy"
const val DATE_PATTERN = "yyyy-MM-dd"
private const val UMAL_QURAH_CALENDER = "@calendar=islamic-umalqura"
fun getCurrentGregorianDate(): String = SimpleDateFormat(
    GREGORIAN_DATE_FORMAT,
    Locale.getDefault()
).format(java.util.Calendar.getInstance().time) //or use getDateInstance()


fun getCurrentIslamicDate(): String {
    val locale = ULocale(UMAL_QURAH_CALENDER)
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
        IslamicCalendar.JUMADA_1 -> "Jumaad ul Awal"
        IslamicCalendar.JUMADA_2 -> "Jumaad uth Thani"
        IslamicCalendar.RAJAB -> "Rajab"
        IslamicCalendar.SHABAN -> "Shabaan"
        IslamicCalendar.RAMADAN -> "Ramadaan"
        IslamicCalendar.SHAWWAL -> "Shawwaal"
        IslamicCalendar.DHU_AL_QIDAH -> "Dhul Qidah"
        IslamicCalendar.DHU_AL_HIJJAH -> "Dhul Hijjah"
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
    calender.add(java.util.Calendar.DAY_OF_WEEK, -1)
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
    "${getLastWeek(calender)}_${calender.get(Calendar.YEAR)}"

fun isTodayFriday() = java.util.Calendar.getInstance(Locale.getDefault())
    .get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY


fun fromStringToLocalTime(timeinString: String?, plusMinutes: Long = 0L) = timeinString?.let {
        LocalTime.parse(timeinString).plusMinutes(plusMinutes)
    }


fun fromLocalTimeToString(time: LocalTime?) = time?.format(DateTimeFormatter.ofPattern("hh:mm a"))
    ?.lowercase()
