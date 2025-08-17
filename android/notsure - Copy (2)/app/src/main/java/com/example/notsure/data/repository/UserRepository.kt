package com.example.notsure.data.repository


import com.example.notsure.data.model.AuthResponse
import com.example.notsure.data.remote.ApiService
import com.example.notsure.data.model.LoginResponse
import retrofit2.Response

class UserRepository(private val api: ApiService) {

    suspend fun login(email:String, password:String):Response<LoginResponse>{
        return api.loginUser(mapOf(
            "emailID" to email,
            "password" to password
        ))
    }

    suspend fun register(fullName:String, email:String, password:String):Response<AuthResponse>{
        return api.registerUser(
            mapOf(
                "fullName" to fullName,
                "emailID" to email,
                "password" to password
            )
        )
    }

    suspend fun getCurrentUser():Response<LoginResponse>{
        return api.getCurrentUser()
    }

}