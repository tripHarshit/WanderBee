package com.example.wanderbee.screens.itinerary

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wanderbee.R
import com.example.wanderbee.screens.home.BottomNavigationBar
import com.example.wanderbee.utils.OtherScreensTopBar
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.wanderbee.utils.SubHeading

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PlanItineraryScreen(
    viewModel: AiViewModel = hiltViewModel(),
    city: String = "Delhi",
    navController: NavController
) {
    var duration by remember { mutableStateOf("3") }
    var budget by remember { mutableStateOf("Medium") }
    var selectedPreferences by remember { mutableStateOf(setOf<String>()) }
    var selectedTab by remember { mutableStateOf("") }

    // Correctly collect AiState from AiViewModel
    val aiState = viewModel.aiState.collectAsState().value

    val preferences =
        listOf("Adventure", "Culture", "Food", "Shopping", "Nature", "History", "Nightlife")
    val budgetOptions = listOf("Low", "Medium", "High", "Luxury")

    Scaffold(
        topBar = { OtherScreensTopBar(navController = navController) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedTab,
                onItemSelected = { tab -> selectedTab = tab },
                navController = navController
            )
        }
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues = it)
        ) {
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    text = "Plan Your Trip to $city",
                    fontSize = 24.sp,
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duration (days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                SubHeading(title = "Budget Range:")
                LazyRow {
                    items(budgetOptions) { option ->
                        FilterChip(
                            onClick = { budget = option },
                            label = { Text(option) },
                            selected = budget == option,
                            modifier = Modifier.padding(end = 8.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.background,
                                selectedLabelColor = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selectedBorderColor = MaterialTheme.colorScheme.secondary,
                                borderColor = MaterialTheme.colorScheme.outline,
                                borderWidth = 1.dp,
                                selectedBorderWidth = .5.dp,
                                enabled = true,
                                selected = budget == option
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                SubHeading(title = "Travel Preferences:")
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.height(200.dp)
                ) {
                    items(preferences) { preference ->
                        FilterChip(
                            onClick = {
                                selectedPreferences = if (selectedPreferences.contains(preference)) {
                                    selectedPreferences - preference
                                } else {
                                    selectedPreferences + preference
                                }
                            },
                            label = { Text(preference) },
                            selected = selectedPreferences.contains(preference),
                            modifier = Modifier.padding(4.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.background,
                                selectedLabelColor = MaterialTheme.colorScheme.secondary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selectedBorderColor = MaterialTheme.colorScheme.secondary,
                                borderColor = MaterialTheme.colorScheme.outline,
                                borderWidth = 1.dp,
                                selectedBorderWidth = .5.dp,
                                enabled = true,
                                selected = selectedPreferences.contains(preference)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Generate Button
                Button(
                    onClick = {
                        viewModel.generateItinerary(
                            cityName = city,
                            duration = duration.toIntOrNull() ?: 3,
                            preferences = selectedPreferences.toList(),
                            budget = budget
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = aiState !is AiState.Loading
                ) {
                    if (aiState is AiState.Loading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    } else {
                        Text("Generate Itinerary")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Display Results - Fixed to use AiState instead of ItineraryState
                when (aiState) {
                    is AiState.Success -> {
                        Card(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Your Personalized Itinerary",
                                    style = MaterialTheme.typography.titleLarge,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = aiState.data,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    is AiState.Error -> {
                        Text(
                            text = "Error: ${aiState.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    AiState.Idle -> {
                        Text(
                            text = "Ready to generate your itinerary!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    AiState.Loading -> {
                        // Loading state is handled in the button
                    }
                }
            }
        }
    }
}
