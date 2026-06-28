package com.example.projecttttt.Screens

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.projecttttt.API.Controllers.TokenManager
import com.example.projecttttt.API.Controllers.addgoals
import com.example.projecttttt.API.retrofitInstance.api
import com.example.projecttttt.Models.Goal
import java.time.LocalDate

@Composable
fun goalsScreen() {
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }
    var showAddGoalDialog by remember { mutableStateOf(false) }
    val ongoingGoals = remember { mutableStateListOf<Goal>() }
    val completedGoals = remember { mutableStateListOf<Goal>() }

    LaunchedEffect(Unit, showAddGoalDialog) {
        val token = TokenManager.gettoken()

        try {
            ongoingGoals.clear()
            completedGoals.clear()
            println("milly")
            val response = api.getprogress("Bearer $token")
            if(response.isSuccessful){
                val goalresponse = response.body()
                if(goalresponse != null){
                    for(goal in goalresponse){
                        if(goal.isCompleted){completedGoals.add(goal)}
                        else {ongoingGoals.add(goal)}
                    }
                }
            }
        } catch (e: Exception) {
            println(e)
        }
        loading = false
    }

    val context = LocalContext.current
    var showAllOngoing by remember { mutableStateOf(false) }
    var showAllCompleted by remember { mutableStateOf(false) }

    if (showAddGoalDialog) {

        AddGoalDialog(

            onDismiss = {
                showAddGoalDialog = false
            },

            onAddGoal = { type,
                          title,
                          targetAmount,
                          category,
                          startDate,
                          endDate ->

                addgoals(type, targetAmount, category, endDate, title, {showAddGoalDialog = false}, { Toast.makeText(context, it, 10).show() })
                println(type)
                println(title)

                }
        )
    }

    Scaffold(

        floatingActionButton = {

            ExtendedFloatingActionButton(
                onClick = {
                    showAddGoalDialog = true
                },
                icon = {
                    Icon(Icons.Default.Add, contentDescription = null)
                },
                text = {
                    Text("Add Goal")
                }
            )
        }

    ) { paddingValues ->

        if (loading) {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    CircularProgressIndicator()

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Loading goals..."
                    )
                }
            }

        } else {

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(20.dp),

                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {

                // TITLE

                item {

                    Text(
                        text = "Goals 🎯",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )

                }

                // ---------------- ONGOING GOALS ----------------

                item {

                    SectionHeader(
                        title = "Ongoing Goals",
                        showSeeAll = ongoingGoals.size > 3,
                        expanded = showAllOngoing,
                        onSeeAllClick = {
                            showAllOngoing = !showAllOngoing
                        }
                    )

                }

                val visibleOngoingGoals =
                    if (showAllOngoing)
                        ongoingGoals
                    else
                        ongoingGoals.take(3)

                items(visibleOngoingGoals) { goal ->

                    GoalCard(goal)

                }

                // ---------------- COMPLETED GOALS ----------------

                item {

                    Spacer(modifier = Modifier.height(10.dp))

                    SectionHeader(
                        title = "Completed Goals",
                        showSeeAll = completedGoals.size > 2,
                        expanded = showAllCompleted,
                        onSeeAllClick = {
                            showAllCompleted = !showAllCompleted
                        }
                    )

                }

                val visibleCompletedGoals =
                    if (showAllCompleted)
                        completedGoals
                    else
                        completedGoals.take(2)

                items(visibleCompletedGoals) { goal ->

                    GoalCard(goal)

                }

                item {
                    Spacer(modifier = Modifier.height(90.dp))
                }
            }
        }
    }
}
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddGoalDialog(

    onDismiss: () -> Unit,

    onAddGoal: (
        type: String,
        title: String,
        targetAmount: Int,
        category: String?,
        startDate: String?,
        endDate: String?
    ) -> Unit

) {

    var selectedType by remember { mutableStateOf("savings") }
    var title by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    val goalTypes = listOf(
        "savings",
        "category_limit",
        "budget"
    )

    AlertDialog(

        onDismissRequest = {
            onDismiss()
        },

        confirmButton = {

            Button(

                onClick = {

                    if (
                        title.isNotBlank() &&
                        targetAmount.isNotBlank()
                    ) {

                        onAddGoal(
                            selectedType,
                            title,
                            targetAmount.toInt(),
                            if (selectedType == "category_limit"){
                            category.ifBlank { null }} else { null },
                            LocalDate.now().toString(),
                            endDate.ifBlank { null }
                        )
                    }
                }

            ) {

                Text("Add Goal")

            }
        },

        dismissButton = {

            OutlinedButton(
                onClick = {
                    onDismiss()
                }
            ) {

                Text("Cancel")

            }
        },

        title = {

            Text(
                text = "Create Goal 🎯"
            )

        },

        text = {

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {

                // ---------------- TYPE ----------------

                Text(
                    text = "Goal Type",
                    style = MaterialTheme.typography.titleMedium
                )

                goalTypes.forEach { type ->

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        RadioButton(
                            selected = selectedType == type,

                            onClick = {
                                selectedType = type
                            }
                        )

                        Text(
                            text = type.replace("_", " ")
                        )
                    }
                }

                // ---------------- TITLE ----------------

                OutlinedTextField(

                    value = title,

                    onValueChange = {
                        title = it
                    },

                    label = {
                        Text("Title *")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                // ---------------- TARGET AMOUNT ----------------

                OutlinedTextField(

                    value = targetAmount,

                    onValueChange = {
                        targetAmount = it
                    },

                    label = {
                        Text("Target Amount *")
                    },

                    modifier = Modifier.fillMaxWidth()
                )

                // ---------------- CATEGORY ----------------
                if(selectedType == "category_limit") {
                    OutlinedTextField(

                        value = category,

                        onValueChange = {
                            category = it
                        },

                        label = {
                            Text("Category")
                        },

                        modifier = Modifier.fillMaxWidth()
                    )
                }
                // ---------------- END DATE ----------------

                OutlinedTextField(

                    value = endDate,

                    onValueChange = {
                        endDate = it
                    },

                    label = {
                        Text("End Date")
                    },

                    placeholder = {
                        Text("YYYY-MM-DD")
                    },

                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )
}
@Composable
fun SectionHeader(
    title: String,
    showSeeAll: Boolean,
    expanded: Boolean,
    onSeeAllClick: () -> Unit
) {

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        if (showSeeAll) {

            Text(
                text = if (expanded) "Show Less" else "See All",

                color = MaterialTheme.colorScheme.primary,

                modifier = Modifier.clickable {
                    onSeeAllClick()
                }
            )
        }
    }
}
@Composable
fun GoalCard(goal: Goal) {

    var showDialog by remember {
        mutableStateOf(false)
    }

    val progress = goal.progress/100 * 1f
    val progressPercent = goal.progress

    if (showDialog) {

        GoalDetailsDialog(

            goal = goal,

            onDismiss = {
                showDialog = false
            },

            onEdit = {

                // TODO
                // edit goal

                showDialog = false
            },

            onDelete = {

                // TODO
                // delete goal

                showDialog = false
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(22.dp),

        elevation = CardDefaults.cardElevation(6.dp)
    ) {

        Column(
            modifier = Modifier.padding(18.dp)
        ) {

            Text(
                text = goal.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Target Amount: ₹${goal.targetAmount}"
            )

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$progressPercent% completed"
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (goal.isCompleted) {

                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFDFF5E1),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {

                    Text(
                        text = "Completed ✅",
                        color = Color(0xFF2E7D32)
                    )
                }

            } else {

                Box(
                    modifier = Modifier
                        .background(
                            Color(0xFFFFF4D6),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {

                    Text(
                        text = "Ongoing ⏳",
                        color = Color(0xFFB26A00)
                    )
                }

            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "See More",

                color = MaterialTheme.colorScheme.primary,

                modifier = Modifier.clickable {
                    showDialog = true
                }
            )
        }
    }
}

@Composable
fun GoalDetailsDialog(

    goal: Goal,

    onDismiss: () -> Unit,

    onEdit: () -> Unit,

    onDelete: () -> Unit

) {
    val progress = 1f * goal.progress/100
    val progressPercent = goal.progress
    AlertDialog(

        onDismissRequest = {
            onDismiss()
        },

        confirmButton = {

            Row {

                OutlinedButton(
                    onClick = {
                        onEdit()
                    }
                ) {
                    Text("Edit")
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        onDelete()
                    }
                ) {
                    Text("Delete")
                }
            }
        },

        title = {

            Text(
                text = goal.title
            )

        },

        text = {

            Column {

                Text(
                    text = "Goal Progress"
                )

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "$progressPercent% completed"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "₹${progress*goal.targetAmount} / ₹${goal.targetAmount}"
                )

                goal.category?.let {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Category: $it"
                    )
                }

                goal.endDate?.let {

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "End Date: $it"
                    )
                }
            }
        }
    )
}