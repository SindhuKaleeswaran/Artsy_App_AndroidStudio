package com.example.notsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notsure.data.remote.ApiService
import com.example.notsure.data.remote.ArtsyService
import com.example.notsure.data.repository.FavoritesRepository

class FavoritesViewModelFactory(
    private val apiService: ApiService,
    private val emailID: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
            return FavoritesViewModel(apiService, emailID) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
