package com.example.notsure.data.remote

import com.example.notsure.data.model.ArtsyArtist
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface ArtsyService {
    @GET("artists/{id}")
    suspend fun getArtistById(
        @Header("X-Xapp-Token") token: String,
        @Path("id") id: String
    ): Response<ArtsyArtist>
}