package com.example.notsure.data.remote

import com.example.notsure.data.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("/search")
    suspend fun  searchArtists(@Query("artistName")name: String): List<ArtistSearchResult>

    @GET("/artist_id")
    suspend fun getArtistDetails(@Query("artistID") id:String): ArtistDetails

    @GET("/artworks")
    suspend fun getArtwork(@Query("artist_id")name:String): List<Artwork>

    @GET("/categories")
    suspend fun getCategories(@Query("artwork_id")artworkId: String):List<Category>

    @POST("/login")
    suspend fun loginUser(@Body credentials: Map<String,String>):Response<LoginResponse>

    @POST("/register")
    suspend fun registerUser(@Body data: Map<String,String>):Response<AuthResponse>

    @HTTP(method="DELETE", path="/user", hasBody=true)
    suspend fun deleteUser(
        @Body request: Map<String, String>
    ):Response<ResponseBody>



    @GET("/fav")
    suspend fun getFavorites():Response<FavoritesResponse>

    @POST("/fav")
    suspend fun modifyFavorite(@Body request: FavoriteRequestWithEmail): Response<GenericResponse>

    @GET("/me")
    suspend fun getCurrentUser():Response<LoginResponse>


}



// Request body to add/remove favorite
//data class FavoriteRequest(
//    val emailID: String,
//    val artistID: String,
//    val flag: Int
//)
//
//// Expected response from GET /fav
//data class FavoriteResponse(
//    val favorites: List<FavoriteArtist>
//)
//
data class GenericResponse(
    val message: String
)