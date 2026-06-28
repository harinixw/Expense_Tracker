package com.example.projecttttt.Navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.projecttttt.API.Controllers.TokenManager
import com.example.projecttttt.API.retrofitInstance.api
import com.example.projecttttt.Screens.AuthScreen
import com.example.projecttttt.Screens.dashboardScreen
import com.example.projecttttt.Screens.goalsScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    var loading by remember { mutableStateOf(true) }
    var startDestination by remember { mutableStateOf("auth") }

    LaunchedEffect(Unit) {
        val token = TokenManager.gettoken()
        println(token)
        if(token != null){
            try {
                val response = api.loginWithToken("Bearer $token")
                println(response)

                if(response.isSuccessful){
                    startDestination = "dashboard"
                }
                else {
                    TokenManager.cleartoken()
                    startDestination = "auth"
                }
            } catch (e: Exception) {
                println(e)
                startDestination = "auth"
            }
        } else {
            startDestination = "auth"
        }
        loading = false
    }
    if (loading) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

    } else {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {

            composable("auth") {

                AuthScreen(
                    onAuthSuccess = {
                        navController.navigate("dashboard") {
                            popUpTo("auth") {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable("dashboard") {
                dashboardScreen(navController)
            }

            composable("goals") {
                goalsScreen()
            }
        }
    }
}