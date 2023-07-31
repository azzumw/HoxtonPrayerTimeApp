package com.example.hoxtonprayertimeapp.utils

import android.icu.util.Calendar
import android.icu.util.IslamicCalendar
import android.icu.util.ULocale
import android.os.Build
import androidx.annotation.RequiresApi
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

private const val GREGORIAN_DATE_FORMAT = "EEE dd MMM yyyy"
private const val DATE_PATTERN =  "ddMMyyyy"
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

fun getLastFridayDate():String{
    val calendar  = java.util.Calendar.getInstance(Locale.getDefault())

    val actualCurrentDay = calendar.get(java.util.Calendar.DAY_OF_WEEK)

    var tempDay = actualCurrentDay

    while(tempDay != java.util.Calendar.FRIDAY){

        if(tempDay == 0){
            //set to saturday
            tempDay = 7
        }
        tempDay--
        calendar.set(java.util.Calendar.DAY_OF_WEEK,tempDay)
    }

    //reduce the week by 1 to get the last friday date because we have moved to this weeks friday in future
    if(actualCurrentDay != java.util.Calendar.SATURDAY){
        calendar.set(java.util.Calendar.WEEK_OF_YEAR,calendar.get(java.util.Calendar.WEEK_OF_YEAR) - 1)
    }

    val df = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
    return df.format(calendar.time)

}

fun getTodayDate():String{
    val date = Timestamp.now().toDate()

//    val calendar = java.util.Calendar.getInstance().time
    val df = SimpleDateFormat(DATE_PATTERN, Locale.getDefault())
    return df.format(date)
}

fun getFridayDate() :String = if(isFridayToday()){
    getTodayDate()
}else{
    getLastFridayDate()
}

fun getLastWeek(calender:java.util.Calendar):Int {
    calender.add(java.util.Calendar.WEEK_OF_YEAR,-1)
    return calender.get(java.util.Calendar.WEEK_OF_YEAR)
}


fun createDocumentReferenceIDForLastWeek(calender: java.util.Calendar) = getLastWeek(calender).toString()

fun isFridayToday() = java.util.Calendar.getInstance(Locale.getDefault()).get(java.util.Calendar.DAY_OF_WEEK) == java.util.Calendar.FRIDAY
