package com.example.wanderbee.screens.details

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.wanderbee.utils.DetailsScreenTopBar
import com.example.wanderbee.R
import com.example.wanderbee.data.remote.apiService.JsonResponses
import com.example.wanderbee.data.remote.models.destinations.Destination
import com.example.wanderbee.data.remote.models.destinations.IndianDestination
import com.example.wanderbee.data.remote.models.weather.DailyWeather
import com.example.wanderbee.navigation.WanderBeeScreens
import com.example.wanderbee.screens.chat.ChatViewModel
import com.example.wanderbee.utils.BottomNavigationBar
import com.example.wanderbee.utils.SubHeading
import com.example.wanderbee.screens.details.CityDataState

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun InfoDetailsScreen(navController: NavController,
                      city: String,
                      dest: String,
                      chatViewModel: ChatViewModel,
                      detailsViewModel: DetailsViewModel) {

    var selectedTab by remember { mutableStateOf("") }
    var selectedOption by remember { mutableStateOf("Info") }
    var currency by remember { mutableStateOf<String?>("") }
    var timezone by remember { mutableStateOf<String?>("") }
    var tags by remember { mutableStateOf<Any>("") }
    var language by remember { mutableStateOf<String?>("") }
    val context = LocalContext.current

    // Get dynamic city data state
    val cityDataState by detailsViewModel.cityDataState.collectAsState()

    LaunchedEffect(city) {
        val cityInfo = JsonResponses().getCityInfo(context, city)
        
        if (cityInfo != null) {
            // Use static JSON data
            currency = when (cityInfo) {
                is IndianDestination -> cityInfo.currency
                is Destination -> cityInfo.currency
                else -> "Unknown"
            }
            timezone = when (cityInfo) {
                is IndianDestination -> cityInfo.timezone
                is Destination -> cityInfo.timezone
                else -> "Unknown"
            }
            tags = when (cityInfo) {
                is IndianDestination -> cityInfo.tags
                is Destination -> cityInfo.tags
                else -> "Unknown"
            }
            language = when (cityInfo) {
                is IndianDestination -> cityInfo.language
                is Destination -> cityInfo.language
                else -> "Unknown"
            }
        } else {
            // City not in static JSON, fetch dynamic data
            detailsViewModel.fetchDynamicCityData(city, dest)
        }
    }

    // Update with dynamic data when available
    LaunchedEffect(cityDataState) {
        val currentState = cityDataState
        when (currentState) {
            is CityDataState.Success -> {
                val cityDetails = currentState.cityDetails
                currency = cityDetails.currency
                timezone = cityDetails.timezone
                tags = cityDetails.tags
                language = cityDetails.language
            }
            is CityDataState.Error -> {
                // Fallback to default values
                currency = "Unknown"
                timezone = "Unknown"
                tags = listOf("Tourism", "Culture", "Travel")
                language = "Unknown"
            }
            else -> {
                // Keep existing values while loading
            }
        }
    }

    Scaffold(
        topBar = { DetailsScreenTopBar(navController = navController,
            isLiked = detailsViewModel.isLiked,
            onLikeClick = { detailsViewModel.toggleLike(city, dest)
                chatViewModel.joinGroupChat(destinationId = "${city}_$dest", destinationName = "${city}_$dest")},
            city = city,
            dest = dest) },
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
        ) {
            // Check if destination is saved when screen loads
            LaunchedEffect(city, dest) {
                detailsViewModel.checkIfSaved(city, dest)
            }
             Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        selectedOption = "Info"
                        navController.navigate("${WanderBeeScreens.InfoDetailsScreen.name}/$city/$dest")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                    border = BorderStroke(
                        0.5.dp,
                        color = if (selectedOption == "Info") {
                            MaterialTheme.colorScheme.secondary
                        } else MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(
                        text = "Info",
                        color = if (selectedOption == "Info") {
                            MaterialTheme.colorScheme.secondary
                        } else MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.istokweb_regular))
                    )
                }

                Button(
                    onClick = {
                        selectedOption = "Photos"
                        navController.navigate("${WanderBeeScreens.PhotosDetailsScreen.name}/$city/$dest")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                    border = BorderStroke(
                        0.5.dp,
                        color = if (selectedOption == "Photos") {
                            MaterialTheme.colorScheme.secondary
                        } else MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(
                        text = "Photos",
                        color = if (selectedOption == "Photos") {
                            MaterialTheme.colorScheme.secondary
                        } else MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.istokweb_regular))
                    )
                }
                Button(
                    onClick = {
                        selectedOption = "Videos"
                        navController.navigate("${WanderBeeScreens.VideosDetailsScreen.name}/$city/$dest")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(2.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.background),
                    border = BorderStroke(
                        0.5.dp,
                        color = if (selectedOption == "Videos") {
                            MaterialTheme.colorScheme.secondary
                        } else MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(
                        text = "Videos",
                        color = if (selectedOption == "Videos") {
                            MaterialTheme.colorScheme.secondary
                        } else MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp,
                        fontFamily = FontFamily(Font(R.font.istokweb_regular))
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            InfoScreenContent(
                detailsViewModel = detailsViewModel,
                modifier = Modifier.weight(1f),
                city = city,
                currency = currency ?: "Unknown",
                timeZone = timezone ?: "Unknown",
                tags = tags.toString(),
                language = language ?: "Unknown",
                cityDataState = cityDataState
            )

            Button(
                onClick = {   navController.navigate("${WanderBeeScreens.PlanItineraryScreen.name}/$city/$dest")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = "Plan Itinerary",
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun InfoScreenContent(
    detailsViewModel: DetailsViewModel,
    modifier: Modifier = Modifier,
    city: String,
    currency: String,
    timeZone: String,
    tags: String,
    language: String,
    cityDataState: CityDataState
) {
    val scrollState = rememberScrollState()

    val descriptionState = detailsViewModel.aiResponseState.collectAsState().value
    val culturalTipsState = detailsViewModel.culturalTipsState.collectAsState().value

    LaunchedEffect(key1 = city) {
        detailsViewModel.getDescription(city)
        detailsViewModel.getCulturalTips(city)
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Top
    ) {
        when (descriptionState) {
            is ItineraryState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is ItineraryState.Success -> {
                Text(
                    text = descriptionState.data,
                    color = MaterialTheme.colorScheme.onBackground,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            is ItineraryState.Error -> {
                Text(
                    text = "Error: ${descriptionState.message}",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            else -> {
                Text(
                    text = "Loading description...",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
        }

        Text(
            text = "Tags: $tags",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        
        // Show loading indicator for dynamic data
        if (cityDataState is CityDataState.Loading) {
            Row(
                modifier = Modifier.padding(bottom = 8.dp, start = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Loading city information...",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
        
        Text(
            text = "Currency: $currency",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Text(
            text = "Timezone: $timeZone",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )
        Text(
            text = "Language: $language",
            fontFamily = FontFamily(Font(R.font.istokweb_regular)),
            fontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        SubHeading(title = "Culture and Highlights")
        when (culturalTipsState) {
            is ItineraryState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
            is ItineraryState.Success -> {
                Text(
                    text = culturalTipsState.data,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            is ItineraryState.Error -> {
                Text(
                    text = "Error: ${culturalTipsState.message}",
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
            else -> {
                Text(
                    text = "Loading description...",
                    fontFamily = FontFamily(Font(R.font.istokweb_regular)),
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
                )
            }
        }

        SubHeading(title = "Weather Conditions")
        Spacer(modifier = Modifier.height(10.dp))
        WeatherForecastCard(cityName = city)
    }
}



@Composable
fun WeatherForecastCard(
    viewModel: DetailsViewModel = hiltViewModel(),
    cityName: String
){
    LaunchedEffect(cityName) {
        viewModel.loadWeatherForecast(cityName)
    }
    val weatherState = viewModel.weatherState.collectAsState().value
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(10.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceVariant),
        border = BorderStroke(width = .5.dp, color = MaterialTheme.colorScheme.secondary)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            when (weatherState) {
                is WeatherUiState.Loading -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            CircularProgressIndicator(
                                modifier = Modifier.matchParentSize(),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Loading...",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 20.sp,
                                fontFamily = FontFamily(Font(resId = R.font.istokweb_bold)),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
             is WeatherUiState.Success -> {
                 val todayWeather = weatherState.dailyWeather.firstOrNull { it.isToday }
                     ?: weatherState.dailyWeather.firstOrNull()

                 todayWeather?.let { today ->
                     Row(
                         modifier = Modifier.fillMaxWidth(),
                         horizontalArrangement = Arrangement.Start,
                         verticalAlignment = Alignment.CenterVertically
                     ) {
                         Box(modifier = Modifier.size(40.dp)) {

                             Image(
                                 painter = painterResource(R.drawable.d02),
                                 contentDescription = "Weather Icon",
                                 modifier = Modifier.size(40.dp),
                                 contentScale = ContentScale.Crop
                             )

                         }

                         Spacer(modifier = Modifier.width(10.dp))

                         Column(verticalArrangement = Arrangement.Center) {
                             Text(
                                 text = "${today.maxTemp}°C",
                                 color = MaterialTheme.colorScheme.onBackground,
                                 fontSize = 20.sp,
                                 fontFamily = FontFamily(Font(resId = R.font.istokweb_bold)),
                                 modifier = Modifier.padding(bottom = 2.dp)
                             )
                             Text(
                                 text = today.weatherMain,
                                 color = MaterialTheme.colorScheme.onBackground,
                                 fontSize = 20.sp,
                                 fontFamily = FontFamily(Font(resId = R.font.istokweb_bold)),

                                 )

                         }
                     }


                     Spacer(modifier = Modifier.height(10.dp))

                     Row(
                         horizontalArrangement = Arrangement.SpaceAround,
                         modifier = Modifier.fillMaxWidth()
                     ) {
                         weatherState.dailyWeather.take(5).forEach { dailyWeather ->
                             ForecastColumn(dailyWeather)
                         }
                     }
                 }
             }

                is WeatherUiState.Error -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(40.dp)) {
                            Icon(
                                imageVector = Icons.Outlined.ErrorOutline,
                                contentDescription = "Error Icon",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.matchParentSize()
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(verticalArrangement = Arrangement.Center) {
                            Text(
                                text = "Error",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 20.sp,
                                fontFamily = FontFamily(Font(resId = R.font.istokweb_bold)),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForecastColumn(dailyWeather: DailyWeather) {
    val iconRes = getWeatherIconResource(dailyWeather.icon)

    Column(
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dailyWeather.dayLabel,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(resId = R.font.istokweb_regular)),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (iconRes != 0) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "Weather Icon",
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Crop
            )
        } else {
            Image(
                painter = painterResource(R.drawable.baseline_error_24),
                contentDescription = "Error",
                modifier = Modifier.size(40.dp),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = "${dailyWeather.maxTemp}°/${dailyWeather.minTemp}°",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 16.sp,
            fontFamily = FontFamily(Font(resId = R.font.istokweb_regular)),
        )
    }
}


@Composable
fun getWeatherIconResource(iconCode: String): Int {
    // Convert "10n" -> "n10"
    val drawableName = when (iconCode.length) {
        3 -> iconCode[2] + iconCode.substring(0, 2)  // 'n' + '10' -> "n10"
        else -> iconCode // fallback
    }

    // get resource id by name
    val context = LocalContext.current
    return remember(drawableName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }
}


