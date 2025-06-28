package com.example.wanderbee.screens.itinerary

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.util.Log
import android.widget.Space
import androidx.annotation.RequiresApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wanderbee.R
import com.example.wanderbee.utils.OtherScreensTopBar
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.screens.authentication.AuthViewModel
import com.example.wanderbee.screens.details.ItineraryState
import com.example.wanderbee.ui.theme.WanderBeeTheme
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.utils.ItineraryTopBar
import com.example.wanderbee.utils.SubHeading
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@RequiresApi(Build.VERSION_CODES.Q)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PlanItineraryScreen(
    viewModel: AiViewModel = hiltViewModel(),
    city: String,
    dest: String,
    navController: NavController
) {
    var days by remember { mutableStateOf("3 Days") }
    var travelers by remember { mutableStateOf("1") }
    var budget by remember { mutableStateOf("Budget Preferences") }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var finalPreferenceString by remember { mutableStateOf("") }

    var selectedTab by remember { mutableStateOf("") }
    val aiState = viewModel.aiState.collectAsState().value
    Log.d("DefaultAiRepository", "aiState: $aiState")
    // Reset state when this screen is shown
    LaunchedEffect(Unit) {
        Log.d("DefaultAiRepository", "Reset Triggered before navigation")
        viewModel.resetState()
    }

    LaunchedEffect(aiState) {
        if (aiState is AiState.Success) {
            Log.d("DefaultAiRepository", "triggering navigation to ItineraryDayScreen")
            navController.navigate("${WanderBeeScreens.ItineraryDayScreen}/0/$city/$dest")

        }
    }

    Scaffold(
        topBar = { ItineraryTopBar(navController = navController, city = city, dest = dest) },
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
            val daysOptions = (1..10).map { "$it Days" } + "2 Weeks"
            val travelerOptions = (1..10).map { "$it" }
            val budgetOptions = listOf("Low", "Mid", "High", "Expensive", "Luxury")

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Plan Your Trip",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                    )

                    Text(
                        text = "Let's create an amazing itinerary for you!",
                        fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                        fontSize = 14.sp,
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {

                    SubHeading(title = "Days")

                    CustomDropdown(
                        options = daysOptions,
                        selectedOption = days,
                        onOptionSelected = { days = it },
                    )

                    Spacer(modifier = Modifier.height(30.dp))

                    StartEndDateSelector(
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateSelected = { startDate = it },
                        onEndDateSelected = { endDate = it }
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                SubHeading(title = "No. of Travellers")

                CustomDropdown(
                    options = travelerOptions,
                    selectedOption = travelers,
                    onOptionSelected = { travelers = it }
                )

                Spacer(modifier = Modifier.height(30.dp))

                SubHeading(title = "Budget")

                CustomDropdown(
                    options = budgetOptions,
                    selectedOption = budget,
                    onOptionSelected = { budget = it }
                )

                Spacer(modifier = Modifier.height(30.dp))

                var selectedPrefs by remember { mutableStateOf(listOf<String>()) }
                var customPrefsText by remember { mutableStateOf("") }
                SubHeading(title = "Travel Preferences")
                TravelPreferenceMultiSelector(
                    selectedPreferences = selectedPrefs,
                    onPreferenceChange = { selectedPrefs = it },
                    customPreferences = customPrefsText,
                    onCustomPreferencesChanged = { customPrefsText = it }
                )

                val finalPreferenceList: List<String> = buildList {
                    addAll(selectedPrefs.filter { it != "Other" })
                    if (selectedPrefs.contains("Other") && customPrefsText.isNotBlank()) {
                        addAll(customPrefsText.split(",").map { it.trim() }.filter { it.isNotBlank() })
                    }
                }

                finalPreferenceString = finalPreferenceList.joinToString(", ")

                Spacer(modifier = Modifier.height(30.dp))

                Button(
                    onClick = {
                        viewModel.getGeneratedItinerary(
                            destination = city,
                            startDate = startDate.toString(),
                            endDate = endDate.toString(),
                            duration = days.split(" ")[0].toInt(),
                            preferences = finalPreferenceList,
                            travellers = travelers.toInt(),
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

                if (aiState is AiState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Error: ${(aiState as AiState.Error).message}",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}


@Composable
fun CustomDropdown(
    options: List<String>,
    selectedOption: String,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { expanded = true }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedOption,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown Arrow",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.secondary)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option, color = MaterialTheme.colorScheme.onBackground) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StartEndDateSelector(
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit
) {
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DateDropdown(
            label = "Start Date",
            selectedDate = startDate,
            onDateSelected = onStartDateSelected,
            modifier = Modifier.weight(1f)
        )

        DateDropdown(
            label = "End Date",
            selectedDate = endDate,
            onDateSelected = onEndDateSelected,
            modifier = Modifier.weight(1f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateDropdown(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val formattedDate = selectedDate?.format(DateTimeFormatter.ofPattern("dd MMM yyyy")) ?: "dd-mm-yyy"

    Column(modifier = modifier) {
        Row {
            Spacer(modifier = Modifier.width(6.dp))
            SubHeading(title = label)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = .5.dp,
                    color = MaterialTheme.colorScheme.secondary,
                    shape = RoundedCornerShape(8.dp)
                )
                .clickable { showDatePicker = true }
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = formattedDate,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                androidx.compose.material3.Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Calendar Icon",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        if (showDatePicker) {
            val today = Calendar.getInstance()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    val pickedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    onDateSelected(pickedDate)
                    showDatePicker = false
                },
                today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TravelPreferenceMultiSelector(
    selectedPreferences: List<String>,
    onPreferenceChange: (List<String>) -> Unit,
    customPreferences: String,
    onCustomPreferencesChanged: (String) -> Unit
) {
    val allPreferences = listOf(
        "Adventure", "Culture", "Food", "Shopping",
        "Nature", "History", "Nightlife", "Other"
    )

    Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
     LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 300.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(allPreferences) { pref ->
                val isSelected = selectedPreferences.contains(pref)

                Box(
                    modifier = Modifier
                        .height(38.dp)
                        .border(
                            width = 1.dp,
                            color = if (isSelected)
                                MaterialTheme.colorScheme.secondary
                            else
                                MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .background(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            else
                                MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .padding(horizontal = 12.dp)
                        .clickable {
                            val updated = if (isSelected) {
                                selectedPreferences - pref
                            } else {
                                selectedPreferences + pref
                            }
                            // Clear custom preferences if "Other" is deselected
                            if (!updated.contains("Other")) {
                                onCustomPreferencesChanged("")
                            }
                            onPreferenceChange(updated)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pref,
                        fontSize = 14.sp,
                        color = if (isSelected)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (selectedPreferences.contains("Other")) {
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                shape = RoundedCornerShape(16.dp),
                value = customPreferences,
                onValueChange = onCustomPreferencesChanged,
                label = { Text("Other preferences (comma separated)",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp)},
                modifier = Modifier
                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    cursorColor = MaterialTheme.colorScheme.primary,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f)
                )
            )
        }
    }
}




