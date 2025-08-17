package com.example.notsure.data.model

data class ArtistDetails (
    val id: String,
    val name: String?,
    val artistName:String,
    val birthday: String?,
    val deathday: String?,
    val nationality:String?,
    val biography: String?,
    val thumbnail:String?,
    val similarArtists: List<SimilarArtist>?,
    val isFavourite: Boolean  =false
)