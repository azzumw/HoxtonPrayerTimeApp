package com.example.hoxtonprayertimeapp

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.SimpleDateFormat
import java.util.Locale

private const val GREGORIAN_DATE_FORMAT = "EEE dd MMM yyyy"
fun getCurrentGregorianDate(): String = SimpleDateFormat(
    GREGORIAN_DATE_FORMAT,
    Locale.getDefault()
).format(java.util.Calendar.getInstance().time) //or use getDateInstance()

@RequiresApi(Build.VERSION_CODES.N)
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
        IslamicCalendar.SHAWWAL -> "Shawwal"
        IslamicCalendar.DHU_AL_QIDAH -> "Dhu al Qidah"
        IslamicCalendar.DHU_AL_HIJJAH -> "Dhi al Hijjah"
        else -> "Unknown Month"
    }
}

