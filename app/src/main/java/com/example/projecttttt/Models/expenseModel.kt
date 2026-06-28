package com.example.projecttttt.Models

import java.sql.Date
import java.time.LocalDate

data class Expense(
    val _id: String,
    val amount: Int,
    val currency: String,
    val merchant: String,
    val category: String,
    val isRecurring: Boolean,
    val recurringId: String?,
    val date: String,
    val search: String,
    val sortBy: String,
    val paymentMethod: String
)