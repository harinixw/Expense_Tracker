package com.example.projecttttt.Models

import android.os.Message

data class Goal(
    val _id: String? = null,
    val type: String,
    val title: String,
    val targetAmount: Int,
    val progress: Float = 0f,
    val category: String? = null,
    val startDate: String? = null,
    val endDate: String? = null,
    val isCompleted: Boolean = false
)

data class goalresponse(
    val msg: String,
    val goal: Goal
)

data class goalrequest(
    val type: String,
    val title: String,
    val category: String?=null,
    val targetAmount: Int,
    val endDate: String?=null
)