package com.example.wanderbee.screens.home

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.example.wanderbee.data.remote.apiService.GeoDbApiService
import com.example.wanderbee.data.remote.models.destinations.City
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
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
    private val geoDbApiService: GeoDbApiService
) : ViewModel() {

    companion object {
        private const val TAG = "HomeScreenViewModel"
        private const val SEARCH_DEBOUNCE_DELAY = 800L // Increased to 800ms to reduce API calls
        private const val MIN_SEARCH_LENGTH = 2 // Only search if query is at least 2 characters
        private const val MAX_RETRIES = 2
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

    // Debounce job for search
    private var searchJob: Job? = null
    
    // Local cache for search results to reduce API calls
    private val searchCache = mutableMapOf<String, List<City>>()
    private val popularCitiesCache = mutableListOf<City>()

    @SuppressLint("SuspiciousIndentation")
    fun fetchUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { document ->
                val name = document.getString("name")
                _name.value = name
                    ?.split(" ")
                    ?.firstOrNull()
                    ?.uppercase(Locale.ROOT)
                    ?: " "
            }
    }

    fun loadCityCoverImage(cityName: String) {
        if (_imageUrls.containsKey(cityName)) return

        viewModelScope.launch {
            try {
                val response = defaultPexelsRepository.getPexelsPhotos(cityName)
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
                
                // Try to use the search endpoint first with retry mechanism
                var retryCount = 0
                var searchResponse: com.example.wanderbee.data.remote.models.destinations.GeoDbResponse? = null
                
                while (retryCount <= MAX_RETRIES && searchResponse == null) {
                    try {
                        Log.d(TAG, "Using search endpoint with namePrefix: '$query' (attempt ${retryCount + 1})")
                        searchResponse = geoDbApiService.searchCities(
                            namePrefix = query,
                            limit = 10
                        )
                        Log.d(TAG, "Search endpoint returned ${searchResponse.data.size} cities")
                        Log.d(TAG, "Search response data: ${searchResponse.data}")
                    } catch (e: Exception) {
                        retryCount++
                        if (e.message?.contains("429") == true && retryCount <= MAX_RETRIES) {
                            val delayMs = (1000L * retryCount) // Exponential backoff: 1s, 2s
                            Log.w(TAG, "Rate limited, retrying in ${delayMs}ms (attempt $retryCount)")
                            delay(delayMs)
                        } else {
                            throw e
                        }
                    }
                }
                
                if (searchResponse?.data?.isNotEmpty() == true) {
                    _searchResults.value = searchResponse.data
                    // Cache the results
                    searchCache[query] = searchResponse.data
                    Log.d(TAG, "Using search results: ${searchResponse.data.map { "${it.name}, ${it.country}" }}")
                } else {
                    // Fallback to popular cities and filter
                    Log.d(TAG, "Search returned no results, falling back to popular cities")
                    
                    // Use cached popular cities if available
                    val citiesToSearch = if (popularCitiesCache.isNotEmpty()) {
                        Log.d(TAG, "Using cached popular cities (${popularCitiesCache.size} cities)")
                        popularCitiesCache
                    } else {
                        Log.d(TAG, "Fetching popular cities from API")
                        val response = geoDbApiService.getPopularCities(limit = 50, offset = 0)
                        Log.d(TAG, "Received ${response.data.size} cities from popular cities API")
                        Log.d(TAG, "Popular cities sample: ${response.data.take(5).map { "${it.name}, ${it.country}" }}")
                        // Cache popular cities
                        popularCitiesCache.addAll(response.data)
                        response.data
                    }
                    
                    // Filter cities that match the query
                    val filtered = citiesToSearch.filter { city ->
                        val matches = city.name.contains(query, ignoreCase = true) ||
                                city.country.contains(query, ignoreCase = true)
                        Log.d(TAG, "City: ${city.name}, Country: ${city.country}, Matches: $matches")
                        matches
                    }
                    
                    Log.d(TAG, "Filtered to ${filtered.size} matching cities")
                    _searchResults.value = filtered
                    // Cache the filtered results
                    searchCache[query] = filtered
                }
                
                if (_searchResults.value.isEmpty()) {
                    Log.w(TAG, "No cities found matching query: '$query'")
                }
                
                _error.value = null
                
            } catch (e: Exception) {
                Log.e(TAG, "Error searching cities", e)
                _searchResults.value = emptyList()
                
                // Handle specific error types
                when {
                    e.message?.contains("429") == true -> {
                        _error.value = "Too many requests. Please wait a moment and try again."
                        Log.w(TAG, "Rate limit exceeded, suggesting user to wait")
                    }
                    e.message?.contains("401") == true -> {
                        _error.value = "API authentication failed. Please check your API key."
                        Log.e(TAG, "API authentication failed")
                    }
                    e.message?.contains("403") == true -> {
                        _error.value = "API access forbidden. Please check your subscription."
                        Log.e(TAG, "API access forbidden")
                    }
                    else -> {
                        _error.value = "Search failed: ${e.message ?: "Unknown error"}"
                        Log.e(TAG, "Generic search error", e)
                    }
                }
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




