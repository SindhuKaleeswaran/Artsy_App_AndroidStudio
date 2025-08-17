package com.example.notsure.data.remote


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory



object RetrofitInstance {

    lateinit var api: ApiService

    fun initialize(baseUrl: String) {
        println("retrooooo")
        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(CookieHandler.client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        api = retrofit.create(ApiService::class.java)
    }
}

