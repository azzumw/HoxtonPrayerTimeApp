package com.hoxtonislah.hoxtonprayertimeapp.utils

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Locale

private const val GREGORIAN_DATE_FORMAT = "EEE dd MMM yyyy"
private const val UMAL_QURAH_CALENDER = "@calendar=islamic-umalqura"

var liveDate: LocalDate = LocalDate.now()
var liveTime: LocalTime = LocalTime.now()


fun getCurrentGregorianDate(localDate: LocalDate = liveDate): String =
    localDate.format(DateTimeFormatter.ofPattern(GREGORIAN_DATE_FORMAT))

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
        IslamicCalendar.JUMADA_1 -> "Jumaada al Ula"
        IslamicCalendar.JUMADA_2 -> "Jumaada al Akhira"
        IslamicCalendar.RAJAB -> "Rajab"
        IslamicCalendar.SHABAN -> "Shabaan"
        IslamicCalendar.RAMADAN -> "Ramadaan"
        IslamicCalendar.SHAWWAL -> "Shawwaal"
        IslamicCalendar.DHU_AL_QIDAH -> "Dhul Qidah"
        IslamicCalendar.DHU_AL_HIJJAH -> "Dhul Hijjah"
        else -> "Unknown Month"
    }
}

/**
 * This method returns the most recent friday based on today's day of the week.
 * If today day of the week is friday then returns today, else returns last weeks friday.
 * */
fun getMostRecentFriday(localDate: LocalDate = liveDate): String {
//    val todayDate = LocalDate.now(clock)
    val todayDate = localDate
    val fridayDate: LocalDate

    when (todayDate.dayOfWeek) {
        DayOfWeek.SATURDAY -> {
            fridayDate = todayDate.minusDays(1)
        }

        DayOfWeek.SUNDAY -> {
            fridayDate = todayDate.minusDays(2)
        }

        DayOfWeek.MONDAY -> {
            fridayDate = todayDate.minusDays(3)
        }

        DayOfWeek.TUESDAY -> {
            fridayDate = todayDate.minusDays(4)
        }

        DayOfWeek.WEDNESDAY -> {
            fridayDate = todayDate.minusDays(5)
        }

        DayOfWeek.THURSDAY -> {
            fridayDate = todayDate.minusDays(6)
        }

        else -> {
            fridayDate = todayDate
        }
    }

    return fridayDate.toString()
}

fun getTodayDateInString(localDate: LocalDate = liveDate) = localDate.toString()

fun getYesterdayDate(localDate: LocalDate = LocalDate.now()) = localDate.minusDays(1L).toString()

fun getLastWeek(localDate: LocalDate = LocalDate.now()): Int {
    val reduceOneWeek = localDate.minusWeeks(1)
    return reduceOneWeek.get(WeekFields.of(Locale.getDefault()).weekOfYear())
}

fun createDocumentReferenceIDForLastWeek() =
    "${getLastWeek()}_${LocalDate.now().year}"


fun isTodayFriday(localDate: LocalDate = liveDate) = localDate.dayOfWeek == DayOfWeek.FRIDAY

fun isTodayWeekend(localDate: LocalDate = liveDate):Boolean {
    return ((localDate.dayOfWeek == DayOfWeek.SATURDAY) || (localDate.dayOfWeek == DayOfWeek.SUNDAY))
}

fun fromStringToLocalTime(timeinString: String?, plusMinutes: Long = 0L) = timeinString?.let {
    LocalTime.parse(timeinString).plusMinutes(plusMinutes)
}

fun fromLocalTimeToString(time: LocalTime?,pattern:String = "hh:mm a") = time?.format(DateTimeFormatter.ofPattern(pattern))
    ?.lowercase()

