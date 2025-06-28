package com.example.wanderbee.screens.itinerary

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.wanderbee.data.remote.models.AI.ItineraryItem
import com.example.wanderbee.utils.GeneratedItineraryTopBar
import com.example.wanderbee.R
import com.example.wanderbee.data.remote.models.AI.TimeSlot
import com.example.wanderbee.navigation.WanderBeeScreens


@SuppressLint("UnrememberedGetBackStackEntry")
@Composable
fun ItineraryDayScreen(
    navController: NavController,
    dayIndex: Int,
    city: String,
    dest: String
) {
    val parentEntry = remember { navController.getBackStackEntry(WanderBeeScreens.PlanItineraryScreen.name + "/{city}/{dest}") }
    val viewModel: AiViewModel = hiltViewModel(parentEntry)

    val aiState by viewModel.aiState.collectAsState()
    Log.d("ItineraryScreen", "Collected aiState: $aiState")

    var showDialog by remember { mutableStateOf(false) }

    when (aiState) {
        is AiState.Loading, is AiState.Idle -> {

            Box(modifier = Modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.secondary)
            }
        }

        is AiState.Success -> {
            val itinerary = (aiState as AiState.Success).itinerary
            val currentDay = itinerary.days.getOrNull(dayIndex)

            if (currentDay == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = "Day not found", color = MaterialTheme.colorScheme.error)
                }
                return
            }
            Surface(modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background) {

                Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                    // Top Bar
                    GeneratedItineraryTopBar(navController, dest = city, onPrintClicked = {})

                    Spacer(modifier = Modifier.height(8.dp))

                    // Day Navigation Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (showDialog) {
                            DiscardConfirmationDialog(
                                onConfirm = {
                                    navController.navigate("${WanderBeeScreens.PlanItineraryScreen.name}/$city/$dest")
                                    showDialog = false
                                },
                                onDismiss = {
                                    showDialog = false
                                }
                            )
                        }
                        IconButton(
                            onClick = {
                                if (dayIndex > 0) {
                                    navController.navigate("${WanderBeeScreens.ItineraryDayScreen.name}/${dayIndex - 1}/$city/$dest"){
                                    }
                                }
                            },
                            enabled = dayIndex > 0
                        ) {
                            Icon(
                                Icons.Outlined.ArrowBackIosNew,
                                contentDescription = "Previous Day",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Box(modifier = Modifier.height(32.dp).width(60.dp)
                            .border(width = 1.dp, color = MaterialTheme.colorScheme.secondary).clip(shape = RoundedCornerShape(12.dp))){
                            Column(modifier = Modifier.matchParentSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally){
                                Text(
                                    text = "Day ${currentDay.dayNumber}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                        IconButton(
                            onClick = {
                                if (dayIndex < itinerary.days.size - 1) {
                                    navController.navigate("${WanderBeeScreens.ItineraryDayScreen.name}/${dayIndex + 1}/$city/$dest")
                                }
                            },
                            enabled = dayIndex < itinerary.days.size - 1
                        ) {
                            Icon(
                                Icons.Outlined.ArrowForwardIos,
                                contentDescription = "Next Day",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(onClick = {
                                      showDialog = true
                        }) {
                            Icon(
                                painter = painterResource(R.drawable.rounded_edit_square_24),
                                contentDescription = "Delete Itinerary",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth().padding(start = 16.dp,end = 16.dp), horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically) {

                        Text(
                            text = currentDay.date,
                            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        Text(
                            text = currentDay.totalCost,
                            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Time Slot Cards
                    LazyColumn {
                        items(currentDay.timeSlots) { slot ->
                            Log.d("ItineraryScreen", "Rendering slot: $slot")
                             ItineraryCard(slot = slot)
                        }
                    }
                }
            }
        }

        is AiState.Error -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: ${(aiState as AiState.Error).message}", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun ItineraryCard(slot: TimeSlot) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = slot.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = slot.activity,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Location: ${slot.location}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Cost: ${slot.cost}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Transport: ${slot.transportation}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Dining: ${slot.dining}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun DiscardConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Delete Itinerary?",
                fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Text(
                text = "Your current itinerary will be lost if you proceed. Are you sure?",
                fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    "Confirm",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    "Cancel",
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    )
}


