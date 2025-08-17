package com.example.notsure.data.model

data class SimilarArtist(
    val id: String,
    val name:String,
    val thumbnail: String,
    val isFavourite: Boolean=  false
)