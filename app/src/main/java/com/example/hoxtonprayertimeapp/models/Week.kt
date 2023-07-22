package com.example.hoxtonprayertimeapp.models

import java.util.Calendar

data class Week(
    val fridayDate: String = "",
    val year: Int = Calendar.getInstance().get(Calendar.YEAR),
    val fajar: String = "",
    val dhuhr: String = "",
    val asr: String = "",
    val maghrib: String = "",
    val isha: String = "",
    val firstJummah: String = "",
    val secondJummah: String? = null,
)
