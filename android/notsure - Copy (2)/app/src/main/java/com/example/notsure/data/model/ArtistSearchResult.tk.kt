package com.example.notsure.data.model

data class ArtistSearchResult(
    val id: String,
    val name: String,
    val image:String? = null,
    var isFavourite: Boolean  =false
)