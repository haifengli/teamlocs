package com.yellowtree.teamlocs.api

import android.content.Context
import com.yellowtree.teamlocs.model.GeoCodeResult
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface GeoCodeService {
    @GET("findAddressCandidates?maxLocations=1&f=json&token=AAPK07a7e19cb108489b8c1eb8b5048fcd8boodFCZqLwztHNGijSuiHbf90hHfUosesZibMp7lSrorTW0g2V_KINZhRlDiRvUtm")
    fun getLocation(@Query("address") address: String) : Call<GeoCodeResult>

    companion object {
        @Volatile
        private var INSTANCE: GeoCodeService? = null

        fun getInstance(context: Context): GeoCodeService = INSTANCE ?: synchronized(this) {
            Retrofit.Builder()
                .baseUrl("https://geocode-api.arcgis.com/arcgis/rest/services/World/GeocodeServer/")
                .addConverterFactory(
                    GsonConverterFactory.create()
                )
                .build()
                .create(GeoCodeService::class.java)
        }
    }
}