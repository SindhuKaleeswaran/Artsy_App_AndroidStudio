package com.example.notsure.data.model


//data class FavoriteArtist(
//    val artistId: String,
//    val addedAt: String,
//    val artistName: String,
//    val image: String?
//)

//data class FavoriteArtist(
//    val artistId: String,
//    val addedAt: String
//)


data class FavoriteArtist(
    val artistId: String,
    val addedAt: String,
    val name: String? = null,
    val nationality: String? = null,
    val birthday: String? = null
)


