package com.example.notsure


import android.app.Application
import com.example.notsure.data.remote.CookieHandler

class NotsureApp : Application() {
    override fun onCreate() {
        super.onCreate()
        CookieHandler.initialize(this)
    }
}
