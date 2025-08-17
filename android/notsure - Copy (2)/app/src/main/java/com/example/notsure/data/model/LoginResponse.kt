package com.example.notsure.data.model

data class LoginResponse(
    val jwtoken: String,
    val message:String,
    val fullname: String,
    val username: String,
    val favv:List<FavoriteArtist>,
    val prfpic:String
)

data class AuthResponse(
    val jwtoken: String,
    val message: String,
    val fullname: String,
    val username: String,
    val favv: List<FavoriteArtist>,
    val prfpic: String
)

data class RegisteredUser(
    val Fullname : String,
    val Username:  String,
    val userProfile:String,
    val favourites:List<FavoriteArtist> = emptyList()
)