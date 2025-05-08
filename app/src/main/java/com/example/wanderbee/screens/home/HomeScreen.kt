package com.example.wanderbee.screens.home

import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Backpack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
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
import com.example.wanderbee.data.remote.models.City
import com.example.wanderbee.data.remote.models.Destination
import com.example.wanderbee.data.remote.models.IndianDestination
import com.example.wanderbee.utils.CustomLinearProgressBar
import com.example.wanderbee.utils.HomeScreenDestinationsCard
import com.example.wanderbee.utils.SubHeading
import com.example.wanderbee.utils.TripSummaryCard
import java.nio.file.WatchEvent

@RequiresApi(Build.VERSION_CODES.S)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(navController: NavController,
               homeScreenViewModel: HomeScreenViewModel = hiltViewModel()){
    val popularDestinations = rememberSaveable { mutableStateOf<List<Destination>>(emptyList()) }
    val indianDestinations = rememberSaveable { mutableStateOf<List<IndianDestination>>(emptyList()) }

    val context = LocalContext.current
    LaunchedEffect(Unit) {
        val popularDestinationsResponse = JsonResponses().popularDestinations(context)
        popularDestinations.value = popularDestinationsResponse

        val indianDestinationsResponse = JsonResponses().indianDestinations(context)
        indianDestinations.value = indianDestinationsResponse

    }

    val name by homeScreenViewModel.name.collectAsState()
    val searchQuery = rememberSaveable { mutableStateOf("") }
    val isExpanded = rememberSaveable { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Home") }

    Scaffold(topBar = {
        HomeTopAppBar()
    },
        bottomBar = {
            BottomNavigationBar(
                selectedItem = selectedTab,
                onItemSelected = { tab -> selectedTab = tab })
        }) { paddingValues->

        Column(modifier = Modifier.padding(paddingValues),
            verticalArrangement = Arrangement.Top) {

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
                modifier = Modifier.padding(top = 8.dp, start = 32.dp)
            )

            HomeSearchBar(searchQuery, isExpanded)

            Spacer(modifier = Modifier.height(34.dp))

            HomeScreenContent(
                modifier  = Modifier,
                popularDestinations,
                indianDestinations
            )
        }

    }
}
@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(modifier: Modifier,
                     popularDestinations: MutableState<List<Destination>>,
                      indianDestinations: MutableState<List<IndianDestination>>,
                      homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
){
    val scrollState = rememberScrollState()
    Surface(modifier = modifier
        .fillMaxSize()
        .background(color = MaterialTheme.colorScheme.background)
        .verticalScroll(scrollState)) {

        Column(modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Top) {

            //AI trip suggestion
            SubHeading(title = "AI Trip Suggestion")

            AiTripSuggestionCard()

            Spacer(modifier = Modifier.height(34.dp))

            //places near you
            SubHeading(title = "Explore India")

            IndianDestinationCardsRow(indianDestinations,homeScreenViewModel)

            Spacer(modifier = Modifier.height(34.dp))

            //upcoming trips
            SubHeading(title = "Upcoming Trips")

            TripSummaryCard()

            Spacer(modifier = Modifier.height(34.dp))

            //popular destinations
            SubHeading(title = "Popular Destinations")

            PopularDestinationCardsRow(popularDestinations,homeScreenViewModel)

            Spacer(modifier = Modifier.height(34.dp))

            //weather and packing tips
            SubHeading(title = "Weather and Packing Tips")

            HomeWeatherAndPackingRow()


        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeSearchBar(searchQuery: MutableState<String>,
                  isExpanded: MutableState<Boolean>){
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        expanded = isExpanded.value,
        onExpandedChange = { isExpanded.value = it },
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        colors = SearchBarDefaults.colors(containerColor = MaterialTheme.colorScheme.background),
        windowInsets = SearchBarDefaults.windowInsets,

        inputField = {
            OutlinedTextField(
                value = searchQuery.value,
                onValueChange = { searchQuery.value = it},
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent),
                placeholder = {
                    Text(
                        text = "Search Destinations (e.g. Paris)",
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
                        IconButton(onClick = { searchQuery.value = ""
                        keyboardController?.hide()
                        focusManager.clearFocus()}) {
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
        // Dropdown content goes here (e.g., suggestions)
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeTopAppBar(){
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
            IconButton(onClick = {}) {
                Icon(imageVector = Icons.Outlined.Person2,
                    contentDescription = "Notification Icon",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier
                        .size(28.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
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
        colors = CardDefaults.cardColors(Color.DarkGray),
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
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
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
                isLoading = isLoading
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun IndianDestinationCardsRow(
    indianDestinations: MutableState<List<IndianDestination>>,
    homeScreenViewModel: HomeScreenViewModel = hiltViewModel()
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
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun HomeWeatherAndPackingRow(){
    Row(modifier = Modifier.fillMaxWidth()
    ){
        Box(modifier = Modifier.background(Color.DarkGray,
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
        Box(modifier = Modifier.background(Color.DarkGray,
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

@Composable
fun BottomNavigationBar(
    selectedItem: String = "Home",
    onItemSelected: (String) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background),
        modifier = Modifier.height(85.dp).fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(0.5.dp, color = Color.White.copy(alpha = .1f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Home Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Home") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Home") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Home",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Home") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            // Chat Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Chat") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Chat,
                    contentDescription = "Chat",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Chat") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Chat",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Chat") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            // Events Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Events") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Event,
                    contentDescription = "Event",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Events") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Events",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Events") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }

            // Saved Item
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(4.dp)
                    .clickable { onItemSelected("Saved") },
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Saved",
                    modifier = Modifier.size(30.dp).padding(4.dp),
                    tint = if (selectedItem == "Saved") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )

                Text(
                    text = "Saved",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = if (selectedItem == "Saved") MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}