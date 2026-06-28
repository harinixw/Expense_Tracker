package com.example.projecttttt.Screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.projecttttt.API.Controllers.TokenManager
import com.example.projecttttt.API.retrofitInstance.api
import com.example.projecttttt.Models.Expense
import java.time.LocalDate

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun dashboardScreen(navController: NavHostController) {
    var loading by remember { mutableStateOf(true) }
    val expenses = remember { mutableStateListOf<Expense>() }
    var monthspending by remember { mutableStateOf(0) }
    var currency by remember { mutableStateOf("INR") }

    var selectedItem by remember { mutableStateOf(0) }

    val items = listOf("Home", "Goals", "Profile")
    LaunchedEffect(Unit) {
        val token = TokenManager.gettoken()

        try {
            val response = api.getexpense("Bearer $token", startDate = LocalDate.now().withDayOfMonth(1), endDate = LocalDate.now())
            if(response.isSuccessful){
                val expenseResponse = response.body()
                if (expenseResponse != null) {
                    for (expense in expenseResponse){
                        expenses.add(expense)
                        monthspending+=expense.amount
                    }
                }
            }
            val response1 = api.loginWithToken("Bearer $token")

            if(response1.isSuccessful){
                currency = response1.body()!!.user.currency
            }

        } catch (e: Exception) {
            println(e)
        }
        loading = false
    }
    Scaffold(

        // TOP BAR
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Expense Tracker",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Welcome back 👋",
                            fontSize = 14.sp
                        )
                    }
                }
            )
        },

        // BOTTOM NAVIGATION BAR
        bottomBar = {
            NavigationBar {

                NavigationBarItem(
                    selected = selectedItem == 0,
                    onClick = {
                        selectedItem = 0
                        navController.navigate("home")
                    },
                    icon = {
                        Icon(Icons.Default.Home, contentDescription = "Home")
                    },
                    label = {
                        Text("Home")
                    }
                )

                NavigationBarItem(
                    selected = selectedItem == 1,
                    onClick = {
                        selectedItem = 1
                        navController.navigate("goals")
                    },
                    icon = {
                        Icon(Icons.Default.Star, contentDescription = "Goals")
                    },
                    label = {
                        Text("Goals")
                    }
                )

                NavigationBarItem(
                    selected = selectedItem == 2,
                    onClick = {
                        selectedItem = 2
                        navController.navigate("profile")
                    },
                    icon = {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    },
                    label = {
                        Text("Profile")
                    }
                )
            }
        }

    ) { padding ->

        // MAIN SCREEN CONTENT
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if(loading){
                CircularProgressIndicator()
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Column {
                            Text(
                                text = "This Month",
                                fontSize = 14.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "$currency $monthspending",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Total Expenses",
                                fontSize = 14.sp
                            )
                        }

                        Button(
                            onClick = {
                                navController.navigate("addExpense")
                            }
                        ) {
                            Text("+ Add")
                        }
                    }
                }
            }
            // EXPENSE SUMMARY CARD

            Spacer(modifier = Modifier.height(20.dp))

            // INSIGHTS SECTION
            Text(
                text = "Insights",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {

                    Text(
                        text = "⚠️ You are close to your monthly budget",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "💸 Highest spending is on Food",
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "💡 You could save ₹2000 this month",
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}
