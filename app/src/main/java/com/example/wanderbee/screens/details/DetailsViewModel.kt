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
import com.example.wanderbee.data.local.entity.CityDescriptionEntity
import com.example.wanderbee.data.local.entity.CulturalTipsEntity
import com.example.wanderbee.data.remote.apiService.AITask
import com.example.wanderbee.data.remote.models.media.PexelsPhoto
import com.example.wanderbee.data.remote.models.media.PexelsVideo
import com.example.wanderbee.data.remote.models.weather.DailyWeather
import com.example.wanderbee.data.repository.DefaultHuggingFaceRepository
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.example.wanderbee.data.repository.WeatherRepository
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
    private val defaultHuggingFaceRepository: DefaultHuggingFaceRepository,
    private val pexelsRepository: DefaultPexelsRepository,
    private val cityDescriptionDao: CityDescriptionDao,
    private val culturalTipsDao: CulturalTipsDao,
    private val descriptionMemoryCache: DescriptionMemoryCache,
    private val tipsMemoryCache: CulturalTipsMemoryCache,
    private val weatherRepository: WeatherRepository
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

    var isLiked by mutableStateOf(false)
        private set

    fun toggleLike() {
        isLiked = !isLiked
    }

    fun getDescription(cityName: String) {
        val prompt = "Describe $cityName in a very short paragraph(should not exceed 50 words). Mention only the most important aspects for tourists. It should be informative and should not contain language and currency information"
        val parameters = mapOf(
            "temperature" to 0.7,
            "max_new_tokens" to 1000,
            "top_p" to 0.85,
            "do_sample" to true,
            "num_beams" to 3
        )
        val task = AITask.CityInfoTextGeneration
        viewModelScope.launch {
            _descriptionState.value = ItineraryState.Loading

            // Check InMemory Cache
            descriptionMemoryCache.get(cityName)?.let {
                _descriptionState.value = ItineraryState.Success(it)
                Log.e("Caching", "Cached-Data Used: $it")
                return@launch
            }

            // Check Database
            cityDescriptionDao.getDescription(cityName)?.let {
                descriptionMemoryCache.put(cityName, it.description)
                _descriptionState.value = ItineraryState.Success(it.description)
                Log.e("Caching", "Database Used: ${it.description}")
                return@launch
            }

            // Otherwise make API call
            defaultHuggingFaceRepository.getAIResponse(task, prompt, parameters).collect { result ->
                _descriptionState.value = result.fold(
                    onSuccess = { response ->
                        val generatedText = response[0].generatedText
                        val cleanedText = generatedText.replace(prompt, "").trim().replace(Regex("^\\s*\\.\\s*"), "")
                        descriptionMemoryCache.put(cityName, cleanedText)
                        cityDescriptionDao.insertDescription(
                            CityDescriptionEntity(cityName, cleanedText)
                        )
                        Log.e("Caching", "API Calling Used: $cleanedText")
                        ItineraryState.Success(cleanedText)
                    },
                    onFailure = { ItineraryState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }

    fun getCulturalTips(cityName: String) {
        val prompt = "Provide 5 essential cultural tips for tourists visiting $cityName. Each tip should contain not more than 30 words. Format each tip as a separate bullet point. Include information about local customs, etiquette, social norms, and important cultural behaviors to respect. Do not include language and currency information."
        val parameters = mapOf(
            "temperature" to 0.8,  // Slightly higher for more creative tips
            "max_new_tokens" to 400,  // Increased to accommodate multiple bullet points
            "top_p" to 0.9,  // Increased for more diverse responses
            "do_sample" to true,
            "num_beams" to 4  // Increased for better structured output
        )
        val task = AITask.CityInfoTextGeneration
        viewModelScope.launch {
            _culturalTipsState.value = ItineraryState.Loading

            // Check in-memory cache
            tipsMemoryCache.get(cityName)?.let {
                _culturalTipsState.value = ItineraryState.Success(it)
                Log.e("Caching", "Cached-Data Used: $it")
                return@launch
            }

            // Check database
            culturalTipsDao.getTip(cityName)?.let {
                tipsMemoryCache.put(cityName, it.response)
                _culturalTipsState.value = ItineraryState.Success(it.response)
                Log.e("Caching", "Database Used: ${it.response}")
                return@launch
            }

            // Otherwise make API call
            defaultHuggingFaceRepository.getAIResponse(task, prompt, parameters).collect { result ->
                _culturalTipsState.value = result.fold(
                    onSuccess = { response ->
                        val generatedText = response[0].generatedText
                        val cleanedText = generatedText.replace(prompt, "").trim()
                        val bulletPointText = if (!cleanedText.contains("•")) {
                            cleanedText.split("\n")
                                .filter { it.isNotEmpty() }
                                .joinToString("\n") { "${it.trim()}" }
                        } else {
                            cleanedText
                        }
                        // Cache in memory and database
                        tipsMemoryCache.put(cityName, bulletPointText)
                        culturalTipsDao.insertTips(
                            CulturalTipsEntity(cityName, bulletPointText)
                        )
                        Log.e("Caching", "API Calling Used: $bulletPointText")
                        ItineraryState.Success(bulletPointText)
                    },
                    onFailure = { ItineraryState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }

    fun searchPhotos(query: String) {
        viewModelScope.launch {
            _photosState.value = PexelsUiState.Loading
            try {
                val response = pexelsRepository.getPexelsPhotos(query)
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
                val response = pexelsRepository.getPexelsVideos(query)
                _videosState.value = PexelsVideoUiState.Success(response.videos)
                Log.d("Videos", " Videos Fetched successfully${response.videos}")
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
}


