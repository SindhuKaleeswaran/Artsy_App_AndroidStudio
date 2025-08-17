package com.example.notsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notsure.data.remote.ApiService


class ArtistDetailViewModelFactory(
    private val apiService: ApiService,
    private val userEmail: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ArtistDetailViewModel(apiService, userEmail) as T
    }
}
