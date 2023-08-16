package com.example.hoxtonprayertimeapp.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Query

const val QUERY_FORMAT_JSON = "json"
const val KEY = "f149aaef-856d-421a-8e1c-0b2f80c11174"
private const val BASE_URL = "https://www.londonprayertimes.com/api/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface PrayersApiService{
    @GET("times")
    suspend fun getTodaysPrayerBeginningTimes(@Query("format") format:String = QUERY_FORMAT_JSON, @Query("key") key:String = KEY):LondonPrayersBeginningTimes
}

object PrayersApi{
    val retrofitService : PrayersApiService by lazy {
        retrofit.create(PrayersApiService::class.java)
    }
}
