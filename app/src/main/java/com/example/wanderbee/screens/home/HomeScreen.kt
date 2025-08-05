package com.example.wanderbee.screens.home

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.WbCloudy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.wanderbee.R
import com.example.wanderbee.data.remote.apiService.JsonResponses
import com.example.wanderbee.data.remote.models.destinations.Destination
import com.example.wanderbee.data.remote.models.destinations.IndianDestination
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.utils.HomeScreenDestinationsCard
import com.example.wanderbee.utils.SubHeading
import com.example.wanderbee.utils.TripSummaryCard

import coil.compose.AsyncImage
import com.example.wanderbee.screens.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material.icons.outlined.ArrowBackIosNew
import androidx.compose.ui.draw.rotate
import androidx.compose.material3.Divider

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(
    navController: NavController,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val popularDestinations = rememberSaveable { mutableStateOf<List<Destination>>(emptyList()) }
    val indianDestinations = rememberSaveable { mutableStateOf<List<IndianDestination>>(emptyList()) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val popularDestinationsResponse = JsonResponses().popularDestinations(context)
        popularDestinations.value = popularDestinationsResponse

        val indianDestinationsResponse = JsonResponses().indianDestinations(context)
        indianDestinations.value = indianDestinationsResponse
       homeScreenViewModel.fetchUserName()
    }

    val name by homeScreenViewModel.name.collectAsState()

    val searchQuery = rememberSaveable { mutableStateOf("") }
    val isExpanded = rememberSaveable { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Home") }

    val scrollState = rememberScrollState()
    var hasScrolled by remember { mutableStateOf(false) }

    // Permanently hide greeting once scrolled
    LaunchedEffect(scrollState.value) {
        if (scrollState.value > 0) hasScrolled = true
    }

    Scaffold(
        topBar = { HomeTopAppBar(navController) },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedTab,
                onItemSelected = { tab -> selectedTab = tab },
                navController = navController
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            verticalArrangement = Arrangement.Top
        ) {
            AnimatedVisibility(visible = !hasScrolled) {
                Column {
                    Text(
                        text = "Hi $name !",
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(start = 32.dp)
                    )

                    Text(
                        text = "Let's wander around the world...",
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                        fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                        modifier = Modifier.padding(top = 8.dp, start = 32.dp, bottom = 8.dp)
                    )
                }
            }

            // Search Bar
            HomeSearchBar(searchQuery, isExpanded, navController, homeScreenViewModel)

            Spacer(modifier = Modifier.height(16.dp))

            HomeScreenContent(
                modifier = Modifier,
                popularDestinations = popularDestinations,
                indianDestinations = indianDestinations,
                scrollState = scrollState,
                navController = navController
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    modifier: Modifier,
    popularDestinations: MutableState<List<Destination>>,
    indianDestinations: MutableState<List<IndianDestination>>,
    scrollState: ScrollState,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    navController: NavController
) {
    Surface(
        modifier = modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            SubHeading(title = "AI Trip Suggestion")
            AiTripSuggestionCard()
            Spacer(modifier = Modifier.height(34.dp))

            SubHeading(title = "Explore India")
            IndianDestinationCardsRow(indianDestinations, homeScreenViewModel, navController)
            Spacer(modifier = Modifier.height(34.dp))

            SubHeading(title = "Upcoming Trips")
            TripSummaryCard()
            Spacer(modifier = Modifier.height(34.dp))

            SubHeading(title = "Popular Destinations")
            PopularDestinationCardsRow(popularDestinations, homeScreenViewModel,  navController )
            Spacer(modifier = Modifier.height(34.dp))

            SubHeading(title = "Weather and Packing Tips")
            HomeWeatherAndPackingRow()
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(
    searchQuery: MutableState<String>,
    isExpanded: MutableState<Boolean>,
    navController: NavController,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val searchResults by homeScreenViewModel.searchResults.collectAsState()
    val isLoading by homeScreenViewModel.isSearchLoading.collectAsState()
    val error by homeScreenViewModel.error.collectAsState()
    var showDropdown by remember { mutableStateOf(false) }

    // Log state changes
    LaunchedEffect(searchQuery.value) {
        android.util.Log.d("HomeSearchBar", "Search query changed to: '${searchQuery.value}'")
    }
    
    LaunchedEffect(searchResults) {
        android.util.Log.d("HomeSearchBar", "Search results updated: ${searchResults.size} results")
        searchResults.forEach { city ->
            android.util.Log.d("HomeSearchBar", "Result: ${city.name}, ${city.country}")
        }
        // Show dropdown when we have results
        if (searchResults.isNotEmpty()) {
            showDropdown = true
            android.util.Log.d("HomeSearchBar", "Setting showDropdown to true because we have ${searchResults.size} results")
        }
    }
    
    LaunchedEffect(isLoading) {
        android.util.Log.d("HomeSearchBar", "Loading state changed: $isLoading")
        // Show dropdown when loading
        if (isLoading) {
            showDropdown = true
            android.util.Log.d("HomeSearchBar", "Setting showDropdown to true because loading")
        }
    }
    
    LaunchedEffect(error) {
        if (error != null) {
            android.util.Log.e("HomeSearchBar", "Search error: $error")
            // Show dropdown when there's an error
            showDropdown = true
            android.util.Log.d("HomeSearchBar", "Setting showDropdown to true because of error")
        }
    }

    // Clear search state when navigating back to home
    LaunchedEffect(Unit) {
        android.util.Log.d("HomeSearchBar", "HomeSearchBar composable launched, clearing search state")
        homeScreenViewModel.clearSearchResults()
        showDropdown = false
        searchQuery.value = ""
    }

    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        expanded = isExpanded.value || showDropdown, // Keep expanded when dropdown is shown
        onExpandedChange = { 
            isExpanded.value = it
            if (!it) {
                showDropdown = false // Close dropdown when search bar is collapsed
                homeScreenViewModel.clearSearchResults() // Clear search results when collapsed
                searchQuery.value = "" // Clear search query when collapsed
            }
            android.util.Log.d("HomeSearchBar", "Search bar expanded: $it")
        },
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        windowInsets = SearchBarDefaults.windowInsets,

        inputField = {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = {
                    searchQuery.value = it
                    android.util.Log.d("HomeSearchBar", "Text changed to: '$it'")
                    if (it.isNotBlank()) {
                        android.util.Log.d("HomeSearchBar", "Calling searchCities with: '$it'")
                        homeScreenViewModel.searchCities(it)
                        showDropdown = true
                    } else {
                        android.util.Log.d("HomeSearchBar", "Clearing search results")
                        showDropdown = false
                        homeScreenViewModel.clearSearchResults()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                placeholder = {
                    Text(
                        text = "Search Destinations (min 2 characters)",
                        color = MaterialTheme.colorScheme.outline,
                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                        fontSize = 14.sp
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                    cursorColor = MaterialTheme.colorScheme.primary
                ),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search Icon",
                        tint = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.value.isNotEmpty()) {
                        IconButton(onClick = {
                            android.util.Log.d("HomeSearchBar", "Clear button clicked")
                            searchQuery.value = ""
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            showDropdown = false
                            homeScreenViewModel.clearSearchResults()
                            isExpanded.value = false // Also collapse the search bar
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            )
        }
    ) {
        android.util.Log.d("HomeSearchBar", "Dropdown content - showDropdown: $showDropdown, results: ${searchResults.size}, loading: $isLoading, error: $error")
        
        // Show dropdown if we have results, are loading, have an error, or have a query
        if (showDropdown && (searchResults.isNotEmpty() || isLoading || error != null || searchQuery.value.isNotBlank())) {
            android.util.Log.d("HomeSearchBar", "Showing dropdown with ${searchResults.size} results")
            
            // Beautiful animated dropdown
            androidx.compose.animation.AnimatedVisibility(
                visible = true,
                enter = androidx.compose.animation.fadeIn(
                    animationSpec = androidx.compose.animation.core.tween(200)
                ) + androidx.compose.animation.slideInVertically(
                    animationSpec = androidx.compose.animation.core.tween(200),
                    initialOffsetY = { -it / 2 }
                ),
                exit = androidx.compose.animation.fadeOut(
                    animationSpec = androidx.compose.animation.core.tween(150)
                ) + androidx.compose.animation.slideOutVertically(
                    animationSpec = androidx.compose.animation.core.tween(150),
                    targetOffsetY = { -it / 2 }
                )
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 12.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Searching destinations...",
                                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else if (error != null) {
                        // Beautiful error state
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Close,
                                    contentDescription = "Error",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Search Error",
                                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = error ?: "Unknown error occurred",
                                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        }
                    } else if (searchResults.isNotEmpty()) {
                        Column {
                            // Beautiful header with results count
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                                        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
                                    )
                                    .padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${searchResults.size} destination(s) found",
                                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                            
                            // Scrollable search results
                            androidx.compose.foundation.lazy.LazyColumn(
                                modifier = Modifier.heightIn(max = 300.dp), // Limit height and make scrollable
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
                            ) {
                                items(searchResults.size) { index ->
                                    val city = searchResults[index]
                                    androidx.compose.animation.AnimatedVisibility(
                                        visible = true,
                                        enter = androidx.compose.animation.fadeIn(
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = 200 + (index * 50)
                                            )
                                        ) + androidx.compose.animation.slideInHorizontally(
                                            animationSpec = androidx.compose.animation.core.tween(
                                                durationMillis = 200 + (index * 50)
                                            ),
                                            initialOffsetX = { it / 2 }
                                        )
                                    ) {
                                        Column {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        android.util.Log.d("HomeSearchBar", "City selected: ${city.name}, ${city.country}")
                                                        showDropdown = false
                                                        searchQuery.value = ""
                                                        keyboardController?.hide()
                                                        focusManager.clearFocus()
                                                        homeScreenViewModel.clearSearchResults() // Clear search state
                                                        navController.navigate("${WanderBeeScreens.InfoDetailsScreen.name}/${city.name}/${city.country}")
                                                    }
                                                    .padding(horizontal = 20.dp, vertical = 16.dp)
                                                    .background(
                                                        if (index % 2 == 0) 
                                                            MaterialTheme.colorScheme.surface 
                                                        else 
                                                            MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Beautiful location icon
                                                Box(
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape)
                                                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.LocationOn,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                // City information
                                                Column(
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(
                                                        text = city.name,
                                                        fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                                                        fontSize = 16.sp,
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )
                                                    Text(
                                                        text = city.country,
                                                        fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                                                        fontSize = 14.sp,
                                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                                    )
                                                }
                                                
                                                // Arrow icon
                                                Icon(
                                                    imageVector = Icons.Outlined.ArrowBackIosNew,
                                                    contentDescription = "View details",
                                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .rotate(180f)
                                                )
                                            }
                                            
                                            // Divider between items (except for last item)
                                            if (index < searchResults.size - 1) {
                                                Divider(
                                                    modifier = Modifier.padding(horizontal = 20.dp),
                                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                                    thickness = 1.dp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else if (searchQuery.value.isNotBlank()) {
                        // Beautiful "no results" state
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Search,
                                        contentDescription = "No results",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "No destinations found",
                                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Try searching for a different location",
                                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(navController: NavController) {
    val profileViewModel: ProfileViewModel = hiltViewModel()
    val profileData by profileViewModel.profileData.collectAsState()
    TopAppBar(
        title = {
            Row(horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "WanderBee",
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    fontFamily = FontFamily(Font(R.font.coustard_regular)))

                Spacer(modifier = Modifier.weight(1f))
            }
        },
        actions ={
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.Notifications,
                    contentDescription = "Notification Icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
            IconButton(onClick = { navController.navigate(WanderBeeScreens.ProfileScreen.name) }) {
                if (profileData.profilePictureUrl.isNotEmpty()) {
                    AsyncImage(
                        model = profileData.profilePictureUrl,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Person2,
                        contentDescription = "Profile Icon",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            MaterialTheme.colorScheme.background
        )
    )
}

@Composable
fun AiTripSuggestionCard(){
    Card(modifier = Modifier
        .fillMaxWidth()
        .height(160.dp)
        .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(8.dp)) {
        Row (modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
            horizontalArrangement = Arrangement.Start){
            Column(verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.width(250.dp)) {
                Text(text = "Let our AI plan a perfect trip",
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 17.sp,
                    modifier = Modifier.padding(bottom = 8.dp),
                    maxLines = 1)

                Text(text = "Personalized itineraries based on your preferences",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 12.dp),
                    minLines = 2)

                Button(onClick = {},
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)) {
                    Text(text = "Plan Now",
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(R.drawable.travel),
                    contentDescription = "google image",
                    contentScale = ContentScale.Crop,
                    alignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                )
            }

        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun PopularDestinationCardsRow(
    popularDestinations: MutableState<List<Destination>>,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    navController: NavController
){
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(popularDestinations.value.shuffled().take(25), key = { it.name }) { destination ->

            LaunchedEffect(destination.name) {
                homeScreenViewModel.loadCityCoverImage(destination.name)
            }

            val imageUrl = homeScreenViewModel.imageUrls[destination.name]

            val isLoading = imageUrl == null

            HomeScreenDestinationsCard(
                city = destination.name,
                place = destination.country,
                imageUrl = imageUrl,
                isLoading = isLoading,
                navController = navController
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun IndianDestinationCardsRow(
    indianDestinations: MutableState<List<IndianDestination>>,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel(),
    navController: NavController
){
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(indianDestinations.value.shuffled().take(25), key = {it.name}) { destination ->
            LaunchedEffect(destination.name) {
                homeScreenViewModel.loadCityCoverImage(destination.name)
            }

            val imageUrl = homeScreenViewModel.imageUrls[destination.name]
            val isLoading = imageUrl == null

            HomeScreenDestinationsCard(
                city = destination.name,
                place = destination.state,
                imageUrl = imageUrl,
                isLoading = isLoading,
                navController = navController
            )
        }
    }
}

@Composable
fun HomeWeatherAndPackingRow(){
    Row(modifier = Modifier.fillMaxWidth()
    ){
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(16.dp))
            .height(130.dp).weight(1f)
            .clip(RoundedCornerShape(16.dp))){
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.matchParentSize()) {

                Icon(imageVector = Icons.Outlined.WbCloudy,
                    contentDescription = "cloud",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp).size(40.dp))

                Text(text = "Weather Forecast",
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        Spacer(modifier = Modifier.width(20.dp))
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant,
            RoundedCornerShape(16.dp))
            .height(130.dp).weight(1f)
            .clip(RoundedCornerShape(16.dp))){
            Column(verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.matchParentSize()) {

                Icon(imageVector = Icons.Outlined.Backpack,
                    contentDescription = "calender",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 12.dp).size(40.dp))

                Text(text = "Packing Tips",
                    fontFamily = FontFamily(Font(R.font.istokweb_bold)),
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

