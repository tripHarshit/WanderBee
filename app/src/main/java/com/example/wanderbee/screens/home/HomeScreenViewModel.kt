package com.example.wanderbee.screens.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.example.wanderbee.data.repository.DestinationResult
import com.example.wanderbee.data.repository.DestinationsRepository
import com.example.wanderbee.data.remote.models.destinations.City
import com.example.wanderbee.data.remote.models.destinations.StaticDestination
import com.example.wanderbee.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val defaultPexelsRepository: DefaultPexelsRepository,
    private val destinationsRepository: DestinationsRepository,
    private val appPreferences: AppPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "HomeScreenViewModel"
        private const val SEARCH_DEBOUNCE_DELAY = 800L // Increased to 800ms to reduce API calls
        private const val MIN_SEARCH_LENGTH = 2 // Only search if query is at least 2 characters
    }

    private val _imageUrls = mutableStateMapOf<String, String?>()
    val imageUrls: Map<String, String?> = _imageUrls

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _searchResults = MutableStateFlow<List<City>>(emptyList())
    val searchResults: StateFlow<List<City>> = _searchResults.asStateFlow()
    private val _isSearchLoading = MutableStateFlow(false)
    val isSearchLoading: StateFlow<Boolean> = _isSearchLoading.asStateFlow()

    /** Backend-served popular cities (replaces the local JSON asset). */
    private val _popularCities = MutableStateFlow<List<City>>(emptyList())
    val popularCities: StateFlow<List<City>> = _popularCities.asStateFlow()
    private val _isPopularCitiesLoading = MutableStateFlow(false)
    val isPopularCitiesLoading: StateFlow<Boolean> = _isPopularCitiesLoading.asStateFlow()

    /** Indian destinations from /api/v1/destinations/static/india (replaces local JSON). */
    private val _indianDestinations = MutableStateFlow<List<StaticDestination>>(emptyList())
    val indianDestinations: StateFlow<List<StaticDestination>> = _indianDestinations.asStateFlow()

    /** All destinations from /api/v1/destinations/static/all (replaces local JSON). */
    private val _allDestinations = MutableStateFlow<List<StaticDestination>>(emptyList())
    val allDestinations: StateFlow<List<StaticDestination>> = _allDestinations.asStateFlow()

    // Debounce job for search
    private var searchJob: Job? = null
    
    // Local cache for search results to reduce API calls
    private val searchCache = mutableMapOf<String, List<City>>()
    private val popularCitiesCache = mutableListOf<City>()

    @SuppressLint("SuspiciousIndentation")
    fun fetchUserName() {
        viewModelScope.launch {
            appPreferences.userName.collect { name ->
                _name.value = name
                    ?.split(" ")
                    ?.firstOrNull()
                    ?.uppercase(Locale.ROOT)
                    ?: " "
            }
        }
    }

    /**
     * Fetch popular cities from the destination-service REST endpoint.
     * Results are stored in [popularCities] and also cached in [popularCitiesCache]
     * so that the search fallback doesn't need a second round-trip.
     */
    fun fetchPopularCities(limit: Int = 10) {
        if (_popularCities.value.isNotEmpty()) return // already loaded
        viewModelScope.launch {
            _isPopularCitiesLoading.value = true
            when (val result = destinationsRepository.getPopularCities(limit)) {
                is DestinationResult.Success -> {
                    val cities = result.data.data
                    _popularCities.value = cities
                    // Also warm the search fallback cache
                    if (popularCitiesCache.isEmpty()) popularCitiesCache.addAll(cities)
                    _error.value = null
                    Log.d(TAG, "fetchPopularCities: ${cities.size} cities loaded")
                }
                is DestinationResult.Error -> {
                    Log.e(TAG, "fetchPopularCities error: ${result.message}")
                }
            }
            _isPopularCitiesLoading.value = false
        }
    }

    /**
     * Fetch Indian destinations from /api/v1/destinations/static/india.
     * Replaces the old JsonResponses().indianDestinations(context) call.
     */
    fun fetchIndianDestinations() {
        if (_indianDestinations.value.isNotEmpty()) return // already loaded
        viewModelScope.launch {
            when (val result = destinationsRepository.getIndianDestinations()) {
                is DestinationResult.Success -> {
                    _indianDestinations.value = result.data
                    Log.d(TAG, "fetchIndianDestinations: ${result.data.size} destinations loaded")
                }
                is DestinationResult.Error -> {
                    Log.e(TAG, "fetchIndianDestinations error: ${result.message}")
                }
            }
        }
    }

    /**
     * Fetch all destinations from /api/v1/destinations/static/all.
     * Replaces the old JsonResponses().popularDestinations(context) call.
     */
    fun fetchAllDestinations() {
        if (_allDestinations.value.isNotEmpty()) return // already loaded
        viewModelScope.launch {
            when (val result = destinationsRepository.getAllDestinations()) {
                is DestinationResult.Success -> {
                    _allDestinations.value = result.data
                    Log.d(TAG, "fetchAllDestinations: ${result.data.size} destinations loaded")
                }
                is DestinationResult.Error -> {
                    Log.e(TAG, "fetchAllDestinations error: ${result.message}")
                }
            }
        }
    }

    fun loadCityCoverImage(cityName: String) {
        if (_imageUrls.containsKey(cityName)) return

        viewModelScope.launch {
            try {
                val response = defaultPexelsRepository.getBackendPhotos(cityName)
                val url = response.photos.shuffled().random().src.medium
                _imageUrls[cityName] = url
            } catch (e: Exception) {
                _imageUrls[cityName] = null
            }
        }
    }

    fun searchCities(query: String) {
        Log.d(TAG, "searchCities called with query: '$query'")
        
        // Cancel previous search job
        searchJob?.cancel()
        
        if (query.isBlank()) {
            Log.d(TAG, "Query is blank, clearing search results")
            _searchResults.value = emptyList()
            _error.value = null
            return
        }

        // Only search if query is long enough
        if (query.length < MIN_SEARCH_LENGTH) {
            Log.d(TAG, "Query too short (${query.length} chars), minimum is $MIN_SEARCH_LENGTH")
            _searchResults.value = emptyList()
            _error.value = null
            return
        }

        // Start new search job with debounce
        searchJob = viewModelScope.launch {
            delay(SEARCH_DEBOUNCE_DELAY)
            
            _isSearchLoading.value = true
            Log.d(TAG, "Starting search for query: '$query'")
            
            try {
                // Check cache first
                if (searchCache.containsKey(query)) {
                    Log.d(TAG, "Found cached results for query: '$query'")
                    _searchResults.value = searchCache[query] ?: emptyList()
                    _error.value = null
                    return@launch
                }
                
                // Call backend search via repository
                Log.d(TAG, "Using search endpoint with namePrefix: '$query'")
                when (val result = destinationsRepository.searchCities(namePrefix = query, limit = 10)) {
                    is DestinationResult.Success -> {
                        val cities = result.data.data
                        Log.d(TAG, "Search returned ${cities.size} cities")
                        
                        if (cities.isNotEmpty()) {
                            _searchResults.value = cities
                            searchCache[query] = cities
                            Log.d(TAG, "Using search results: ${cities.map { "${it.name}, ${it.country}" }}")
                        } else {
                            // Fallback to popular cities and filter locally
                            Log.d(TAG, "Search returned no results, falling back to popular cities")
                            
                            val citiesToSearch = if (popularCitiesCache.isNotEmpty()) {
                                Log.d(TAG, "Using cached popular cities (${popularCitiesCache.size} cities)")
                                popularCitiesCache
                            } else {
                                Log.d(TAG, "Fetching popular cities from backend")
                                when (val popularResult = destinationsRepository.getPopularCities(limit = 50)) {
                                    is DestinationResult.Success -> {
                                        val popular = popularResult.data.data
                                        Log.d(TAG, "Received ${popular.size} cities from popular endpoint")
                                        popularCitiesCache.addAll(popular)
                                        popular
                                    }
                                    is DestinationResult.Error -> {
                                        Log.e(TAG, "Failed to fetch popular cities: ${popularResult.message}")
                                        _error.value = popularResult.message
                                        _searchResults.value = emptyList()
                                        return@launch
                                    }
                                }
                            }
                            
                            val filtered = citiesToSearch.filter { city ->
                                city.name.contains(query, ignoreCase = true) ||
                                        city.country.contains(query, ignoreCase = true)
                            }
                            Log.d(TAG, "Filtered to ${filtered.size} matching cities")
                            _searchResults.value = filtered
                            searchCache[query] = filtered
                        }
                        
                        if (_searchResults.value.isEmpty()) {
                            Log.w(TAG, "No cities found matching query: '$query'")
                        }
                        _error.value = null
                    }
                    is DestinationResult.Error -> {
                        Log.e(TAG, "Search failed: ${result.message}")
                        _searchResults.value = emptyList()
                        _error.value = result.message
                    }
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error searching cities", e)
                _searchResults.value = emptyList()
                _error.value = "Search failed: ${e.message ?: "Unknown error"}"
            } finally {
                _isSearchLoading.value = false
                Log.d(TAG, "Search completed")
            }
        }
    }

    fun clearSearchResults() {
        Log.d(TAG, "Clearing search results")
        searchJob?.cancel()
        _searchResults.value = emptyList()
        _error.value = null
        _isSearchLoading.value = false
    }
    
    fun clearSearchCache() {
        Log.d(TAG, "Clearing search cache")
        searchCache.clear()
        popularCitiesCache.clear()
    }
}




