package com.example.projecttttt.API


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.getValue

object retrofitInstance {

    private const val BASE_URL = "http://192.168.1.14:5000/"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: ApiService  by lazy {
        retrofit.create(ApiService::class.java)
    }
}