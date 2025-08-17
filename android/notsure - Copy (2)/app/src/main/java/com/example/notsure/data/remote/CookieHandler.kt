//package com.example.notsure.data.remote
//
//
//import android.content.Context
//import com.franmontiel.persistentcookiejar.ClearableCookieJar
//import com.franmontiel.persistentcookiejar.PersistentCookieJar
//import com.franmontiel.persistentcookiejar.cache.SetCookieCache
//import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
//import okhttp3.OkHttpClient
//
//object CookieHandler {
//    lateinit var cookieJar: ClearableCookieJar
//    lateinit var client: OkHttpClient
//
//    fun initialize(context: Context) {
//        println(">>> Initializing CookieHandler")
//        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))
//        client = OkHttpClient.Builder().cookieJar(cookieJar).build()
//    }
//}
//5/8 5.300am brff

package com.example.notsure.data.remote

import android.content.Context
import com.franmontiel.persistentcookiejar.ClearableCookieJar
import com.franmontiel.persistentcookiejar.PersistentCookieJar
import com.franmontiel.persistentcookiejar.cache.SetCookieCache
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor
import okhttp3.OkHttpClient

object CookieHandler {

    lateinit var cookieJar:ClearableCookieJar
    lateinit var client:OkHttpClient

    fun initialize(context: Context) {
        cookieJar = PersistentCookieJar(SetCookieCache(), SharedPrefsCookiePersistor(context))

        client = OkHttpClient.Builder().cookieJar(cookieJar).build()
    }
}

