package com.yellowtree.teamlocs.api

import com.yellowtree.teamlocs.model.TimeZoneInfo
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call

interface TimeZoneService {

    @GET("get-time-zone?key=2XD2FY9897J1&format=json&by=position")
    fun getTimeZone(@Query("lat") latitude: Double, @Query("lng") longitude: Double) : Call<TimeZoneInfo>

    companion object {
        @Volatile
        private var INSTANCE: TimeZoneService? = null

        fun getInstance() : TimeZoneService = INSTANCE ?: synchronized(this) {
            Retrofit.Builder()
                .baseUrl("https://api.timezonedb.com/v2.1/")
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()
                .create(TimeZoneService::class.java)
                .apply {
                    INSTANCE = this
                }
        }
    }
}