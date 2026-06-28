package com.example.projecttttt.API.Controllers

import com.example.projecttttt.API.retrofitInstance.api
import com.example.projecttttt.Models.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun loginUser(
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = api.login(
                LoginRequest(username, password)
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Save token (important)
                    val token = response.body()?.token

                    if (token != null) {
                        TokenManager.savetoken(token)
                        onSuccess()
                    } else {
                        onError("Invalid response from server")
                    }

                } else {
                    onError("Invalid credentials")
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Server error")
            }
        }
    }
}

fun registerUser(
    username: String,
    password: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = api.register(
                LoginRequest(username, password)
            )
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {

                    // Save token (important)
                    val msg = response.body()?.msg

                    if (msg != null) {
                        onSuccess()
                    } else {
                        onError("Invalid response from server")
                    }

                } else {
                    onError("Invalid credentials")
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Server error")
                println(e)
            }
        }
    }
}