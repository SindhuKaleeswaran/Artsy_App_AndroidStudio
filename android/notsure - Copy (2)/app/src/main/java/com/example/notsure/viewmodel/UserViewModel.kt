package com.example.notsure.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notsure.data.model.FavoriteArtist
import com.example.notsure.data.model.User
import com.example.notsure.data.remote.CookieHandler
import com.example.notsure.data.repository.UserRepository
import com.example.notsure.data.remote.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserViewModel : ViewModel() {

    private val repository = UserRepository(RetrofitInstance.api)

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    private val _favourites = MutableStateFlow<List<FavoriteArtist>>(emptyList())
    val favourites: StateFlow<List<FavoriteArtist>> = _favourites

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private val _justLoggedIn = MutableStateFlow(false)
    val justLoggedIn: StateFlow<Boolean> = _justLoggedIn

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)

                if (response.isSuccessful && response.body() != null) {
                    val loginData = response.body()!!
                    _user.value = User(
                        fullname = loginData.fullname,
                        username = loginData.username,
                        prfpic = loginData.prfpic
                    )
                    _favourites.value = loginData.favv
                    _isLoggedIn.value = true
                    _errorMessage.value = null
                    _justLoggedIn.value = true
                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: "Login failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Network error: ${e.localizedMessage}")
            }
        }
    }

    fun markLoginSnackbarShown() {
        _justLoggedIn.value = false
    }

    fun register(
        fullName: String,
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val response = repository.register(fullName, email, password)
                if (response.isSuccessful && response.body() != null) {
                    val data = response.body()!!

                    _user.value = User(
                        fullname = data.fullname,
                        username = data.username,
                        prfpic = data.prfpic
                    )
                    _favourites.value = data.favv
                    _isLoggedIn.value = true
                    _errorMessage.value = null

                    onSuccess()
                } else {
                    onError(response.errorBody()?.string() ?: "Registration failed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Network error: ${e.localizedMessage}")
            }
        }
    }



    private val _justLoggedOut = MutableStateFlow(false)
    val justLoggedOut: StateFlow<Boolean> = _justLoggedOut

    fun logout() {
        _user.value = null
        _favourites.value = emptyList()
        _isLoggedIn.value = false
        _errorMessage.value = null
        CookieHandler.cookieJar.clear()
        _justLoggedOut.value = true
    }
    fun markLogoutToastShown() {
        _justLoggedOut.value = false
    }
    fun deleteUser(email: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.deleteUser(mapOf("emailID" to email))
                if (response.isSuccessful) {
                    onResult(true)
                } else {
                    Log.e("DELETE_USER", "Failed: ${response.errorBody()?.string()}")
                    onResult(false)
                }
            } catch (e: Exception) {
                Log.e("DELETE_USER", "Error: ${e.localizedMessage}")
                onResult(false)
            }
        }
    }

    fun updateFavoriteLocally(artistId: String, isNowFavorite: Boolean) {
        _favourites.value = if (isNowFavorite) {
            _favourites.value + FavoriteArtist(artistId = artistId, addedAt = getCurrentTime())
        } else {
            _favourites.value.filterNot { it.artistId == artistId }
        }
    }

    private fun getCurrentTime(): String {
        return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(Date())
    }
    fun restoreSession(onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = repository.getCurrentUser()
                if (response.isSuccessful && response.body() != null) {
                    val userData = response.body()!!
                    _user.value = User(
                        fullname = userData.fullname,
                        username = userData.username,
                        prfpic = userData.prfpic
                    )
                    _favourites.value = userData.favv
                    _isLoggedIn.value = true
                    _errorMessage.value = null
                    onComplete(true)
                } else {
                    onComplete(false)
                }
            } catch (e: Exception) {
                onComplete(false)
            }
        }
    }


    val emailID: String?
        get() = _user.value?.username
}
