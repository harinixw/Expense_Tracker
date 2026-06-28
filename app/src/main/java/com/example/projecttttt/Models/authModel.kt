package com.example.projecttttt.Models

import android.icu.util.Currency

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String
)

data class RegisterResponse(
    val msg: String
)

data class UserResponse(
    val user: User
)

data class User(
    val _id: String,
    val username: String,
    val budget: Int,
    val currency: String
)