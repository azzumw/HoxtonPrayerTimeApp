package com.example.hoxtonprayertimeapp.models

import com.example.hoxtonprayertimeapp.utils.fromLocalTimeToString
import com.example.hoxtonprayertimeapp.utils.fromStringToLocalTime

data class FireStoreWeekModel(
    val fridayDate: String? = null,
    val fajar: String? = null,
    val dhuhr: String? = null,
    val asr: String? = null,
    val isha: String? = null,
    val firstJummah: String? = null,
    val secondJummah: String? = null,
){
    fun to12hour(time:String?) = fromLocalTimeToString(fromStringToLocalTime(time))

}


