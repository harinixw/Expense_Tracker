package com.example.projecttttt.API.Controllers

import android.content.Context

object TokenManager {
    private var token: String? = null
    private const val PREF_NAME = "auth_prefs"
    private const val TOKEN_KEY = "jwt_tokens"
    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context){
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun savetoken(token: String){
        prefs.edit().putString(TOKEN_KEY, token).apply()
    }

    fun gettoken(): String? {
        return prefs.getString(TOKEN_KEY, null)
    }

    fun cleartoken(){
        prefs.edit().remove(TOKEN_KEY).apply()
    }

    fun isLoggedIn(): Boolean {
        return gettoken() != null
    }
}