package com.example.projecttttt.API.Controllers

import com.example.projecttttt.API.retrofitInstance.api
import com.example.projecttttt.Models.goalrequest
import com.example.projecttttt.Models.goalresponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

fun addgoals(
    type: String,
    targetamount: Int,
    category: String?,
    endDate: String?,
    title: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
){
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val token = TokenManager.gettoken()
            val response = api.addgoals("Bearer $token", goalrequest(type=type, title=title, category=category, targetAmount=targetamount, endDate=endDate))
            if(response.isSuccessful){
                onSuccess()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                onError("Server error")
            }
        }
    }

}