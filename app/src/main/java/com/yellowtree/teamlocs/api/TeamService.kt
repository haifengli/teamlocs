package com.yellowtree.teamlocs.api

import android.content.Context
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.yellowtree.teamlocs.model.Team
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface TeamService {
    @get:GET("teams.json")
    val teams: Call<List<Team>>

    companion object {
        @Volatile
        private var INSTANCE: TeamService? = null

        fun getInstance(context: Context): TeamService = INSTANCE ?: synchronized(this) {
            Retrofit.Builder()
                .baseUrl("https://api")
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder()
                            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                            .create()
                    )
                )
                .client(
                    OkHttpClient.Builder()
                        .addInterceptor(RequestInterceptor(context.assets))
                        .build())
                .build().create(TeamService::class.java)
        }
    }

}