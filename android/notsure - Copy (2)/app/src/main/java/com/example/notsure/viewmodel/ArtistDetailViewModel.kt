package com.example.notsure.viewmodel


import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notsure.data.model.ArtistDetails
import com.example.notsure.data.model.Artwork
import com.example.notsure.data.model.Category
import com.example.notsure.data.model.FavoriteRequestWithEmail
import com.example.notsure.data.model.SimilarArtist
import com.example.notsure.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.notsure.data.remote.ApiService


//class ArtistDetailViewModel : ViewModel() {
//    private val _artist = MutableStateFlow<ArtistDetails?>(null)
//    val artist: StateFlow<ArtistDetails?> = _artist
//
//    private val _artworks = MutableStateFlow<List<Artwork>>(emptyList())
//    val artworks: StateFlow<List<Artwork>> = _artworks
//
//    fun load(artistId: String) {
//        _artist.value = null              // Clear previous artist
//        _artworks.value = emptyList()
//        viewModelScope.launch {
//            try {
//                val artistResponse = RetrofitInstance.api.getArtistDetails(artistId)
//
//                val artworkResponse = RetrofitInstance.api.getArtwork(artistId)
//
//                _artist.value = artistResponse
//                _artworks.value = artworkResponse
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    private val _categories = MutableStateFlow<List<Category>>(emptyList())
//    val categories: StateFlow<List<Category>> = _categories
//
//    private val _isLoadingCategories = MutableStateFlow(false)
//    val isLoadingCategories: StateFlow<Boolean> = _isLoadingCategories
//
//    fun loadCategories(artworkId: String) {
//        viewModelScope.launch {
//            _isLoadingCategories.value = true
//            try {
//                val response = RetrofitInstance.api.getCategories(artworkId)
//                _categories.value = response
//            } catch (e: Exception) {
//                e.printStackTrace()
//                _categories.value = emptyList()
//            } finally {
//                _isLoadingCategories.value = false
//            }
//        }
//    }
//
//
//}

class ArtistDetailViewModel(
    private val apiService: ApiService,
    private val userEmail: String?
) : ViewModel() {

    private val _artist = MutableStateFlow<ArtistDetails?>(null)
    val artist: StateFlow<ArtistDetails?> = _artist

    private val _artworks = MutableStateFlow<List<Artwork>>(emptyList())
    val artworks: StateFlow<List<Artwork>> = _artworks

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _isLoadingCategories = MutableStateFlow(false)
    val isLoadingCategories: StateFlow<Boolean> = _isLoadingCategories

    private val _similarArtists = MutableStateFlow<List<SimilarArtist>>(emptyList())
    val similarArtists: StateFlow<List<SimilarArtist>> = _similarArtists

    fun load(artistId: String) {
        _artist.value = null
        _artworks.value = emptyList()

        viewModelScope.launch {
            try {
                val artistResponse = apiService.getArtistDetails(artistId)
                val artworkResponse = apiService.getArtwork(artistId)

                // 🟨 Enrich similar artists (already done)
                val favIDs = if (!userEmail.isNullOrBlank()) {
                    apiService.getFavorites().body()?.favorites?.map { it.artistId } ?: emptyList()
                } else emptyList()

                val enrichedSimilar = artistResponse.similarArtists?.map {
                    it.copy(isFavourite = favIDs.contains(it.id))
                } ?: emptyList()

                // 🟩 Enrich main artist
                val isMainFav = favIDs.contains(artistResponse.id)

                _artist.value = artistResponse.copy(
                    similarArtists = enrichedSimilar,
                    isFavourite = isMainFav
                )

                _artworks.value = artworkResponse

            } catch (e: Exception) {
                Log.e("DETAIL_LOAD", "Failed to load artist details: ${e.localizedMessage}")
            }
        }
    }





    fun loadCategories(artworkId: String) {
        viewModelScope.launch {
            _isLoadingCategories.value = true
            try {
                _categories.value = apiService.getCategories(artworkId)
            } catch (e: Exception) {
                _categories.value = emptyList()
            } finally {
                _isLoadingCategories.value = false
            }
        }
    }

    fun setSimilarArtists(artists: List<SimilarArtist>, favIds: List<String>) {
        _similarArtists.value = artists.map {
            it.copy(isFavourite = favIds.contains(it.id))
        }
    }

    fun toggleFavorite(artist: SimilarArtist) {
        val updated = artist.copy(isFavourite = !artist.isFavourite)
        val updatedList = _artist.value?.similarArtists?.map {
            if (it.id == artist.id) updated else it
        } ?: return

        _artist.value = _artist.value?.copy(similarArtists = updatedList)

        viewModelScope.launch {
            try {
                val flag = if (updated.isFavourite) 1 else 0
                apiService.modifyFavorite(
                    FavoriteRequestWithEmail(
                        artistID = updated.id,
                        flag = flag,
                        emailID = userEmail!!
                    )
                )
            } catch (e: Exception) {
                Log.e("TOGGLE_FAVORITE", "Error updating favorite: ${e.localizedMessage}")
            }
        }
    }

    fun toggleFavoriteFromDetails() {
        val currentArtist = _artist.value ?: return
        val updatedArtist = currentArtist.copy(isFavourite = !currentArtist.isFavourite)
        _artist.value = updatedArtist

        viewModelScope.launch {
            try {
                val flag = if (updatedArtist.isFavourite) 1 else 0
                apiService.modifyFavorite(
                    FavoriteRequestWithEmail(
                        artistID = updatedArtist.id,
                        flag = flag,
                        emailID = userEmail!!
                    )
                )
            } catch (e: Exception) {
                Log.e("DETAIL_FAV", "Error toggling favorite: ${e.localizedMessage}")
            }
        }
    }
}
