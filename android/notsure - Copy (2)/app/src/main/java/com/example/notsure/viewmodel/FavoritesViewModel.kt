//package com.example.notsure.viewmodel
//
//import androidx.lifecycle.ViewModel
//import androidx.lifecycle.viewModelScope
//import com.example.notsure.data.model.FavoriteArtist
//import com.example.notsure.data.remote.ApiService
//import com.example.notsure.data.remote.ArtsyService
//import com.example.notsure.data.repository.FavoritesRepository
//import kotlinx.coroutines.flow.MutableStateFlow
//import kotlinx.coroutines.flow.StateFlow
//import kotlinx.coroutines.launch
//
//sealed class FavoritesUiState {
//    object Loading : FavoritesUiState()
//    data class Success(val favorites: List<FavoriteArtist>) : FavoritesUiState()
//    data class Error(val message: String) : FavoritesUiState()
//    object Empty : FavoritesUiState()
//}
//
//class FavoritesViewModel(private val repository: FavoritesRepository) : ViewModel() {
//
//    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
//    val uiState: StateFlow<FavoritesUiState> = _uiState
//
//    fun loadFavorites() {
//        viewModelScope.launch {
//            _uiState.value = FavoritesUiState.Loading
//            val result = repository.getFavorites()
//            if (result.isSuccessful) {
//                val favorites = result.body() ?: emptyList()
//                _uiState.value = if (favorites.isEmpty()) FavoritesUiState.Empty
//                else FavoritesUiState.Success(favorites)
//            } else {
//                _uiState.value = FavoritesUiState.Error("Failed to load favorites")
//            }
//        }
//    }
//
//    fun toggleFavorite(artistID: String, isCurrentlyFavorite: Boolean) {
//        viewModelScope.launch {
//            val flag = if (isCurrentlyFavorite) 0 else 1
//            repository.modifyFavorite(artistID, flag)
//            loadFavorites()
//        }
//    }
//
//}
package com.example.notsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notsure.data.model.FavoriteArtist
import com.example.notsure.data.model.FavoriteRequestWithEmail
import com.example.notsure.data.remote.ApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class FavoritesUiState {
    object Loading : FavoritesUiState()
    object Empty : FavoritesUiState()
    data class Success(val favorites: List<FavoriteArtist>) : FavoritesUiState()
    data class Error(val message: String) : FavoritesUiState()
}
class FavoritesViewModel(
    private val apiService: ApiService,
    private val emailID: String
) : ViewModel() {

    private val _uiState = MutableStateFlow<FavoritesUiState>(FavoritesUiState.Loading)
    val uiState: StateFlow<FavoritesUiState> = _uiState

    fun loadFavorites() {
        viewModelScope.launch {
            _uiState.value = FavoritesUiState.Loading
            try {
                val response = apiService.getFavorites()
                if (response.isSuccessful) {
                    val favorites = response.body()?.favorites ?: emptyList()
                    _uiState.value = if (favorites.isEmpty()) {
                        FavoritesUiState.Empty
                    } else {
                        FavoritesUiState.Success(favorites)
                    }
                } else {
                    _uiState.value = FavoritesUiState.Error("Failed to load favorites")
                }
            } catch (e: Exception) {
                _uiState.value = FavoritesUiState.Error("Exception: ${e.localizedMessage}")
            }
        }
    }
}

