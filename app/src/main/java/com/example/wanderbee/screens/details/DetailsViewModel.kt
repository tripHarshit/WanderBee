package com.example.wanderbee.screens.details

import android.util.Log
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.cache.CulturalTipsMemoryCache
import com.example.wanderbee.data.cache.DescriptionMemoryCache
import com.example.wanderbee.data.local.dao.CityDescriptionDao
import com.example.wanderbee.data.local.dao.CulturalTipsDao
import com.example.wanderbee.data.local.entity.CityDescriptionEntity
import com.example.wanderbee.data.local.entity.CulturalTipsEntity
import com.example.wanderbee.data.remote.apiService.AITask
import com.example.wanderbee.data.remote.models.destinations.City
import com.example.wanderbee.data.repository.DefaultHuggingFaceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AIResponseState {
    object Idle : AIResponseState()
    object Loading : AIResponseState()
    data class Success(val data: String) : AIResponseState()
    data class Error(val message: String) : AIResponseState()
}

@HiltViewModel
class DetailsViewModel @Inject constructor(
    private val defaultHuggingFaceRepository: DefaultHuggingFaceRepository,
    private val cityDescriptionDao: CityDescriptionDao,
    private val culturalTipsDao: CulturalTipsDao,
    private val descriptionMemoryCache: DescriptionMemoryCache,
    private val tipsMemoryCache: CulturalTipsMemoryCache
) : ViewModel() {

    private val _descriptionState = MutableStateFlow<AIResponseState>(AIResponseState.Idle)
    val aiResponseState: StateFlow<AIResponseState> = _descriptionState.asStateFlow()

    private val _culturalTipsState = MutableStateFlow<AIResponseState>(AIResponseState.Idle)
    val culturalTipsState: StateFlow<AIResponseState> = _culturalTipsState.asStateFlow()

    fun getDescription(cityName: String) {
        val prompt = "Describe $cityName in a very short paragraph(should not exceed 50 words). Mention only the most important aspects for tourists. It should be informative and should not contain language and currency information"
        val parameters = mapOf(
            "temperature" to 0.7,
            "max_new_tokens" to 300,
            "top_p" to 0.85,
            "do_sample" to true,
            "num_beams" to 3
        )
        val task = AITask.CityDescription
        viewModelScope.launch {
            _descriptionState.value = AIResponseState.Loading

            // Check InMemory Cache
            descriptionMemoryCache.get(cityName)?.let {
                _descriptionState.value = AIResponseState.Success(it)
                Log.e("Caching", "Cached-Data Used: $it")
                return@launch
            }

            // Check Database
            cityDescriptionDao.getDescription(cityName)?.let {
                descriptionMemoryCache.put(cityName, it.description)
                _descriptionState.value = AIResponseState.Success(it.description)
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
                        AIResponseState.Success(cleanedText)
                    },
                    onFailure = { AIResponseState.Error(it.message ?: "Unknown error") }
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
        val task = AITask.CityDescription
        viewModelScope.launch {
            _culturalTipsState.value = AIResponseState.Loading

            // Check in-memory cache
            tipsMemoryCache.get(cityName)?.let {
                _culturalTipsState.value = AIResponseState.Success(it)
                Log.e("Caching", "Cached-Data Used: $it")
                return@launch
            }

            // Check database
            culturalTipsDao.getTip(cityName)?.let {
                tipsMemoryCache.put(cityName, it.response)
                _culturalTipsState.value = AIResponseState.Success(it.response)
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
                        AIResponseState.Success(bulletPointText)
                    },
                    onFailure = { AIResponseState.Error(it.message ?: "Unknown error") }
                )
            }
        }
    }


}
