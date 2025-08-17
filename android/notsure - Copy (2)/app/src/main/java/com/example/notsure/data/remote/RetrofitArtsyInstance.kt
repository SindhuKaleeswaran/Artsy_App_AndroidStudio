package com.example.notsure.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitArtsyInstance {

    private const val BASE_URL = "https://api.artsy.net/api/"

    val api: ArtsyService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ArtsyService::class.java)
    }
}
