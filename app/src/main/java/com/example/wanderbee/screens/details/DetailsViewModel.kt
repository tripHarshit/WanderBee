package com.example.wanderbee.screens.details

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.cache.CulturalTipsMemoryCache
import com.example.wanderbee.data.cache.DescriptionMemoryCache
import com.example.wanderbee.data.local.dao.CityDescriptionDao
import com.example.wanderbee.data.local.dao.CulturalTipsDao
import com.example.wanderbee.data.local.dao.SavedDestinationDao
import com.example.wanderbee.data.local.entity.CityDescriptionEntity
import com.example.wanderbee.data.local.entity.CulturalTipsEntity
import com.example.wanderbee.data.local.entity.SavedDestinationEntity
import com.example.wanderbee.data.remote.models.media.PexelsPhoto
import com.example.wanderbee.data.remote.models.media.PexelsVideo
import com.example.wanderbee.data.remote.models.weather.DailyWeather
import com.example.wanderbee.data.remote.models.destinations.City
import com.example.wanderbee.data.remote.models.destinations.Country
import com.example.wanderbee.data.remote.models.destinations.Language
import com.example.wanderbee.data.repository.AiRepository
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.example.wanderbee.data.repository.DestinationResult
import com.example.wanderbee.data.repository.DestinationsRepository
import com.example.wanderbee.data.repository.WeatherRepository
import com.example.wanderbee.data.repository.CityDataRepository
import com.example.wanderbee.data.repository.CityDetails
import com.example.wanderbee.data.repository.ChatRepository
import com.example.wanderbee.screens.details.CityDataState
import com.example.wanderbee.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ItineraryState {
    object Idle : ItineraryState()
    object Loading : ItineraryState()
    data class Success(val data: String) : ItineraryState()
    data class Error(val message: String) : ItineraryState()
}
sealed class PexelsUiState {
    object Loading : PexelsUiState()
    data class Success(val photos: List<PexelsPhoto>) : PexelsUiState()
    data class Error(val message: String) : PexelsUiState()
}
sealed class PexelsVideoUiState {
    object Loading : PexelsVideoUiState()
    data class Success(val videos: List<PexelsVideo>) : PexelsVideoUiState()
    data class Error(val message: String) : PexelsVideoUiState()
}
sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val dailyWeather: List<DailyWeather>) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val aiRepository: AiRepository,
    private val pexelsRepository: DefaultPexelsRepository,
    private val cityDescriptionDao: CityDescriptionDao,
    private val culturalTipsDao: CulturalTipsDao,
    private val savedDestinationDao: SavedDestinationDao,
    private val descriptionMemoryCache: DescriptionMemoryCache,
    private val tipsMemoryCache: CulturalTipsMemoryCache,
    private val weatherRepository: WeatherRepository,
    private val cityDataRepository: CityDataRepository,
    private val destinationsRepository: DestinationsRepository,
    private val chatRepository: ChatRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    private val _descriptionState = MutableStateFlow<ItineraryState>(ItineraryState.Idle)
    val aiResponseState: StateFlow<ItineraryState> = _descriptionState.asStateFlow()

    private val _culturalTipsState = MutableStateFlow<ItineraryState>(ItineraryState.Idle)
    val culturalTipsState: StateFlow<ItineraryState> = _culturalTipsState.asStateFlow()

    private val _photosState = MutableStateFlow<PexelsUiState>(PexelsUiState.Loading)
    val photosState: StateFlow<PexelsUiState> = _photosState.asStateFlow()

    private val _videosState = MutableStateFlow<PexelsVideoUiState>(PexelsVideoUiState.Loading)
    val videosState: StateFlow<PexelsVideoUiState> = _videosState.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val weatherState: StateFlow<WeatherUiState> = _weatherState.asStateFlow()

    // Dynamic city data state
    private val _cityDataState = MutableStateFlow<CityDataState>(CityDataState.Idle)
    val cityDataState: StateFlow<CityDataState> = _cityDataState.asStateFlow()

    var isLiked by mutableStateOf(false)
        private set
    
    init {
        // Uncomment the next line to clear all cache and data for release
        // clearAllCacheAndData()
        
        // Log user info on initialization
        viewModelScope.launch {
            val email = appPreferences.getUserEmailOnce()
            Log.d("DetailsViewModel", "Initialized with user: ${email ?: "Not logged in"}")
        }
        clearAllCacheAndData()
        
        // Test database connection
        testDatabaseConnection()
    }

    /**
     * Fetch city info from the backend's static JSON endpoints.
     * Tries Indian destinations first, then all destinations.
     * If not found, falls back to dynamic city data (CityDataRepository).
     */
    fun fetchStaticCityInfo(cityName: String, countryName: String) {
        viewModelScope.launch {
            _cityDataState.value = CityDataState.Loading
            try {
                // Try Indian destinations first
                val indianResult = destinationsRepository.getIndianDestinations()
                if (indianResult is DestinationResult.Success) {
                    val match = indianResult.data.find { it.name.equals(cityName, ignoreCase = true) }
                    if (match != null) {
                        _cityDataState.value = CityDataState.Success(
                            match.toCityDetails()
                        )
                        Log.d("DetailsViewModel", "Static Indian data found for: $cityName")
                        return@launch
                    }
                }

                // Try all destinations
                val allResult = destinationsRepository.getAllDestinations()
                if (allResult is DestinationResult.Success) {
                    val match = allResult.data.find { it.name.equals(cityName, ignoreCase = true) }
                    if (match != null) {
                        _cityDataState.value = CityDataState.Success(
                            match.toCityDetails()
                        )
                        Log.d("DetailsViewModel", "Static all-destinations data found for: $cityName")
                        return@launch
                    }
                }

                // Not found in static data, fall back to dynamic
                Log.d("DetailsViewModel", "City not in static data, falling back to dynamic: $cityName")
                fetchDynamicCityData(cityName, countryName)
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error fetching static city info, falling back to dynamic", e)
                fetchDynamicCityData(cityName, countryName)
            }
        }
    }

    /**
     * Convert a StaticDestination to CityDetails for the UI.
     */
    private fun com.example.wanderbee.data.remote.models.destinations.StaticDestination.toCityDetails(): CityDetails {
        val countryName = country ?: state ?: ""
        return CityDetails(
            city = City(
                id = 0,
                name = name,
                country = countryName,
                latitude = lat,
                longitude = lon
            ),
            country = Country(
                id = 0,
                name = countryName,
                code = countryCode,
                currencyCode = currency,
                currencyName = null,
                currencySymbol = null,
                timezone = timezone,
                languages = listOf(Language(name = language, code = ""))
            ),
            tags = tags,
            timezone = timezone,
            currency = currency,
            language = language
        )
    }

    // Function to fetch dynamic city data for cities not in static JSON
    fun fetchDynamicCityData(cityName: String, countryName: String) {
        viewModelScope.launch {
            _cityDataState.value = CityDataState.Loading
            try {
                Log.d("DetailsViewModel", "Fetching dynamic data for: $cityName, $countryName")
                val cityDetails = cityDataRepository.getCityDetails(cityName, countryName)
                
                if (cityDetails != null) {
                    _cityDataState.value = CityDataState.Success(cityDetails)
                    Log.d("DetailsViewModel", "Dynamic data fetched successfully: ${cityDetails.currency}, ${cityDetails.timezone}")
                } else {
                    _cityDataState.value = CityDataState.Error("City not found in database")
                    Log.w("DetailsViewModel", "City not found: $cityName, $countryName")
                }
            } catch (e: Exception) {
                _cityDataState.value = CityDataState.Error(e.message ?: "Unknown error")
                Log.e("DetailsViewModel", "Error fetching dynamic city data", e)
            }
        }
    }

    fun toggleLike(city: String, destination: String) {
        viewModelScope.launch {
            val userId = appPreferences.getUserEmailOnce()
            if (userId.isNullOrBlank()) {
                Log.e("DetailsViewModel", "User not logged in - cannot save destination")
                return@launch
            }
        
            val destinationId = "${city}_$destination"
        
            Log.d("DetailsViewModel", "toggleLike called for: city=$city, destination=$destination, userId=$userId")
        
            try {
                val isCurrentlySaved = savedDestinationDao.isDestinationSaved(destinationId, userId)
                Log.d("DetailsViewModel", "Current saved status: $isCurrentlySaved")
                
                if (isCurrentlySaved) {
                    // Unsave locally
                    savedDestinationDao.unsaveDestination(destinationId, userId)
                    isLiked = false
                    Log.d("DetailsViewModel", "Destination unsaved locally: $destinationId")

                    // Unsave on backend
                    try {
                        destinationsRepository.unsaveDestination(destinationId)
                        Log.d("DetailsViewModel", "Destination unsaved on backend: $destinationId")
                    } catch (e: Exception) {
                        Log.e("DetailsViewModel", "Error unsaving on backend: ${e.message}", e)
                    }
                } else {
                    // Save locally
                    val savedDestination = SavedDestinationEntity(
                        destinationId = destinationId,
                        city = city,
                        destination = destination,
                        userId = userId
                    )
                    Log.d("DetailsViewModel", "Attempting to save destination: $savedDestination")
                    
                    try {
                        savedDestinationDao.saveDestination(savedDestination)
                        Log.d("DetailsViewModel", "Successfully saved destination to local database")
                        
                        // Save on backend
                        val destinationName = "$city, $destination"
                        try {
                            destinationsRepository.saveDestination(destinationId, destinationName)
                            Log.d("DetailsViewModel", "Destination saved on backend: $destinationId")
                        } catch (e: Exception) {
                            Log.e("DetailsViewModel", "Error saving on backend: ${e.message}", e)
                        }

                        // Create/join chat room for this destination
                        try {
                            val destinationName = "$city, $destination"
                            chatRepository.createGroupRoom(destinationName, listOf(userId))
                            Log.d("DetailsViewModel", "Chat room created for destination: $destinationId")
                        } catch (e: Exception) {
                            Log.e("DetailsViewModel", "Error creating chat room: ${e.message}", e)
                        }
                        
                        isLiked = true
                        Log.d("DetailsViewModel", "Destination saved and liked: $destinationId")
                    } catch (e: Exception) {
                        Log.e("DetailsViewModel", "Error saving destination to database: ${e.message}", e)
                    }
                }
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error toggling like: ${e.message}")
            }
        }
    }

    fun checkIfSaved(city: String, destination: String) {
        viewModelScope.launch {
            val userId = appPreferences.getUserEmailOnce()
            if (userId.isNullOrBlank()) {
                Log.e("DetailsViewModel", "User not logged in - cannot check saved status")
                isLiked = false
                return@launch
            }
        
            val destinationId = "${city}_$destination"
        
            try {
                isLiked = savedDestinationDao.isDestinationSaved(destinationId, userId)
                Log.d("DetailsViewModel", "Checked saved status for $destinationId: $isLiked")
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error checking saved status: ${e.message}")
                isLiked = false
            }
        }
    }

    fun getDescription(cityName: String) {
        viewModelScope.launch {
            _descriptionState.value = ItineraryState.Loading

            // Check InMemory Cache
            descriptionMemoryCache.get(cityName)?.let {
                _descriptionState.value = ItineraryState.Success(it)
                Log.d("Caching", "Cached-Data Used: $it")
                return@launch
            }

            // Check Database
            cityDescriptionDao.getDescription(cityName)?.let {
                descriptionMemoryCache.put(cityName, it.description)
                _descriptionState.value = ItineraryState.Success(it.description)
                Log.d("Caching", "Database Used: ${it.description}")
                return@launch
            }

            // Fetch from backend InsightController
            try {
                val insights = aiRepository.getCityInsights(cityName)
                val description = insights.description
                descriptionMemoryCache.put(cityName, description)
                cityDescriptionDao.insertDescription(
                    CityDescriptionEntity(cityName, description)
                )
                Log.d("Caching", "Backend API Used: $description")
                _descriptionState.value = ItineraryState.Success(description)
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error fetching description: ${e.message}", e)
                _descriptionState.value = ItineraryState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun getCulturalTips(cityName: String) {
        viewModelScope.launch {
            _culturalTipsState.value = ItineraryState.Loading

            // Check in-memory cache
            tipsMemoryCache.get(cityName)?.let {
                _culturalTipsState.value = ItineraryState.Success(it)
                Log.d("Caching", "Cached-Data Used: $it")
                return@launch
            }

            // Check database
            culturalTipsDao.getTip(cityName)?.let {
                tipsMemoryCache.put(cityName, it.response)
                _culturalTipsState.value = ItineraryState.Success(it.response)
                Log.d("Caching", "Database Used: ${it.response}")
                return@launch
            }

            // Fetch from backend InsightController
            try {
                val insights = aiRepository.getCityInsights(cityName)
                val bulletPointText = insights.culturalTips.joinToString("\n") { "• $it" }
                // Cache in memory and database
                tipsMemoryCache.put(cityName, bulletPointText)
                culturalTipsDao.insertTips(
                    CulturalTipsEntity(cityName, bulletPointText)
                )
                Log.d("Caching", "Backend API Used: $bulletPointText")
                _culturalTipsState.value = ItineraryState.Success(bulletPointText)
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Error fetching cultural tips: ${e.message}", e)
                _culturalTipsState.value = ItineraryState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchPhotos(query: String) {
        viewModelScope.launch {
            _photosState.value = PexelsUiState.Loading
            try {
                val response = pexelsRepository.getBackendPhotos(query)
                _photosState.value = PexelsUiState.Success(response.photos)
            } catch (e: Exception) {
                _photosState.value = PexelsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun searchVideos(query: String) {
        viewModelScope.launch {
            _videosState.value = PexelsVideoUiState.Loading
            try {
                val response = pexelsRepository.getBackendVideos(query)
                _videosState.value = PexelsVideoUiState.Success(response.videos)
            } catch (e: Exception) {
                _videosState.value = PexelsVideoUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun loadWeatherForecast(cityName: String) {
        viewModelScope.launch {
            _weatherState.value = WeatherUiState.Loading
            try {
                Log.d("WeatherViewModel", "Loading weather for: $cityName")
                val dailyWeather = weatherRepository.getDailyWeatherForecast(cityName)
                Log.d("WeatherViewModel", "Weather loaded successfully: ${dailyWeather.size} days")
                _weatherState.value = WeatherUiState.Success(dailyWeather)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Weather error: ${e.message}", e)
                _weatherState.value = WeatherUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
    
    /**
     * Test database connection and functionality
     */
    fun testDatabaseConnection() {
        viewModelScope.launch {
            try {
                val count = savedDestinationDao.getSavedDestinationsCount()
                Log.d("DetailsViewModel", "Database test - Total saved destinations: $count")
                
                val userId = appPreferences.getUserEmailOnce()
                Log.d("DetailsViewModel", "Database test - Current user: ${userId ?: "Not logged in"}")
                
                if (!userId.isNullOrBlank()) {
                    val userDestinations = savedDestinationDao.getAllSavedDestinations(userId)
                    Log.d("DetailsViewModel", "Database test - User destinations: ${userDestinations.size}")
                }
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "Database test failed: ${e.message}", e)
            }
        }
    }
    
    /**
     * Clear all local cache and data for release preparation
     * Call this function before building release APK
     */
    fun clearAllCacheAndData() {
        viewModelScope.launch {
            try {
                Log.d("DetailsViewModel", "🧹 Starting cache and data cleanup...")
                
                // Clear memory caches
                descriptionMemoryCache.clear()
                tipsMemoryCache.clear()
                Log.d("DetailsViewModel", "✅ Memory caches cleared")
                
                // Clear database tables
                cityDescriptionDao.deleteAllCityDescriptions()
                culturalTipsDao.deleteAllCulturalTips()
                savedDestinationDao.deleteAllSavedDestinations()
                Log.d("DetailsViewModel", "✅ Database tables cleared")
                
                Log.d("DetailsViewModel", "🎉 All cache and data cleared successfully!")
                
            } catch (e: Exception) {
                Log.e("DetailsViewModel", "❌ Error clearing cache and data: ${e.message}", e)
            }
        }
    }
}


