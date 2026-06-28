package com.example.projecttttt.API

import com.example.projecttttt.Models.Expense
import com.example.projecttttt.Models.Goal
import com.example.projecttttt.Models.LoginRequest
import com.example.projecttttt.Models.LoginResponse
import com.example.projecttttt.Models.RegisterResponse
import com.example.projecttttt.Models.UserResponse
import com.example.projecttttt.Models.goalrequest
import com.example.projecttttt.Models.goalresponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.LocalDate
import java.util.Date

interface ApiService {

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponse>

    @POST("api/auth/register")
    suspend fun register(
        @Body request: LoginRequest
    ): Response<RegisterResponse>

    @GET("api/auth/logwtok")
    suspend fun loginWithToken(
        @Header("Authorization") token: String
    ): Response<UserResponse>

    @GET("api/expense")
    suspend fun getexpense(
        @Header("Authorization") token: String,
        @Query("category") category: String?=null,
        @Query("startDate") startDate: LocalDate?=null,
        @Query("endDate") endDate: LocalDate?=null,
        @Query("paymentMethod") paymentMethod: String?=null,
        @Query("search") search: String?=null,
        @Query("isRecurring") isRecurring: Boolean?=null,
        @Query("sortBy") sortBy: String?=null
    ): Response<List<Expense>>

    @GET("api/goals")
    suspend fun getgoals(
        @Header("Authorization") token: String,
    ): Response<List<Goal>>

    @POST("api/goals")
    suspend fun addgoals(
        @Header("Authorization") token: String,
        @Body goal: goalrequest
    ): Response<goalresponse>

    @GET("api/goals/progress")
    suspend fun getprogress(
        @Header("Authorization") token: String,
    ): Response<List<Goal>>
}