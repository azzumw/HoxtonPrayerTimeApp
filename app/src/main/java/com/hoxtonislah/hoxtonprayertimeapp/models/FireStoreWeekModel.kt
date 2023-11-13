package com.hoxtonislah.hoxtonprayertimeapp.models

import com.hoxtonislah.hoxtonprayertimeapp.utils.fromLocalTimeToString
import com.hoxtonislah.hoxtonprayertimeapp.utils.fromStringToLocalTime

data class FireStoreWeekModel(
    val fridayDate: String? = null,
    val fajar: String? = null,
    val dhuhr: String? = null,
    val asr: String? = null,
    val isha: String? = null,
    val weekendIsha: String? = null,
    val winterTime: Boolean? = false,
    val firstJummah: String? = null,
    val secondJummah: String? = null,
) {
    fun to12hour(time: String?) = fromLocalTimeToString(fromStringToLocalTime(time), "hh:mm a")

}