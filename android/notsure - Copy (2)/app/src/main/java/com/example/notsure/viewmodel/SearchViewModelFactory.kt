package com.example.notsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notsure.data.remote.ApiService
import kotlinx.coroutines.flow.StateFlow

class SearchViewModelFactory(
    private val apiService: ApiService,
    private val userEmail: String?
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(apiService, userEmail) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
