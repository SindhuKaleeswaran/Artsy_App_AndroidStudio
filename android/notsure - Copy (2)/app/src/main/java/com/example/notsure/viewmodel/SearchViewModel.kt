package com.example.notsure.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notsure.data.model.ArtistSearchResult
import com.example.notsure.data.model.FavoriteRequestWithEmail
import com.example.notsure.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SearchViewModel(
    private val apiService: ApiService,
    private val userEmail: String?
) : ViewModel() {

    private val _searchResults = MutableStateFlow<List<ArtistSearchResult>>(emptyList())
    val searchResults: StateFlow<List<ArtistSearchResult>> = _searchResults

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun search(query: String) {
        viewModelScope.launch {
            try {
                val results = apiService.searchArtists(query)

                val enriched = if (!userEmail.isNullOrBlank()) {
                    val favs = apiService.getFavorites().body()?.favorites?.map { it.artistId } ?: emptyList()
                    results.map { artist ->
                        artist.copy(isFavourite = favs.contains(artist.id))
                    }
                } else results

                _searchResults.value = enriched
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Search failed: ${e.message}"
            }
        }
    }

    fun clearSearch() {
        _searchResults.value = emptyList()
        _errorMessage.value = null
    }
    fun toggleFavorite(
        artist: ArtistSearchResult,
        userEmail: String?,
        onGlobalFavoriteUpdate: (artistId: String, isNowFavorite: Boolean) -> Unit
    ) {
        if (userEmail.isNullOrBlank()) return

        val updated = artist.copy(isFavourite = !artist.isFavourite)
        _searchResults.value = _searchResults.value.map {
            if (it.id == artist.id) updated else it
        }

        viewModelScope.launch {
            val flag = if (updated.isFavourite) 1 else 0
            try {
                val response = apiService.modifyFavorite(
                    FavoriteRequestWithEmail(
                        artistID = artist.id,
                        flag = flag,
                        emailID = userEmail
                    )
                )
                if (response.isSuccessful) {
                    onGlobalFavoriteUpdate(artist.id, updated.isFavourite)
                }
            } catch (e: Exception) {
                Log.e("TOGGLE", "API failed: ${e.localizedMessage}")
            }
        }
    }


}
