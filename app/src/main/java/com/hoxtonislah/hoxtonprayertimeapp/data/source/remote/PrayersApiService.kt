package com.hoxtonislah.hoxtonprayertimeapp.data.source.remote

import com.hoxtonislah.hoxtonprayertimeapp.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Query

const val QUERY_FORMAT_JSON = "json"
private const val BASE_URL = "https://www.londonprayertimes.com/api/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val retrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface PrayersApiService {
    @GET("times")
    suspend fun getTodaysPrayerBeginningTimes(
        @Query("format") format: String = QUERY_FORMAT_JSON,
        @Query("key") key: String = BuildConfig.londonPrayerApiKey,
        @Query("24hours")timeFormat : Boolean =  true,
        @Query("date") date: String
    ): LondonPrayersBeginningTimes
}

object PrayersApi {
    val retrofitService: PrayersApiService by lazy {
        retrofit.create(PrayersApiService::class.java)
    }
}
