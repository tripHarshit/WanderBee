package com.example.wanderbee.screens.itinerary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.repository.AiRepository
import com.example.wanderbee.data.repository.DefaultAiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val data: String) : AiState()
    data class Error(val message: String) : AiState()
}

@HiltViewModel
class AiViewModel @Inject constructor(
    private val aiRepository: DefaultAiRepository
) : ViewModel() {

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)
    val aiState: StateFlow<AiState> = _aiState.asStateFlow()

    /**
     * Generate itinerary based on user preferences.
     * This function matches the usage in PlanItineraryScreen.
     */
    fun getGeneratedItinerary(
        destination: String,
        duration: Int,
        preferences: List<String>,
        budget: String,
        model: String = "gpt-3.5-turbo",
        temperature: Double = 0.7
    ) {
        val prompt = buildPrompt(destination, duration, preferences, budget)
        viewModelScope.launch {
            _aiState.value = AiState.Loading
            try {
                val result = aiRepository.generateFromPrompt(
                    apiKey = BuildConfig.AI_API_KEY,
                    prompt = prompt,
                    model = model,
                    temperature = temperature
                )
                _aiState.value = AiState.Success(result ?: "No response")
                Log.d("AiResponse", result.toString())
            } catch (e: Exception) {
                _aiState.value = AiState.Error(e.message ?: "Failed to generate itinerary")
            }
        }
    }

    /**
     * Legacy function for backward compatibility.
     * Delegates to getGeneratedItinerary.
     */
    fun generateItinerary(
        cityName: String,
        duration: Int,
        preferences: List<String>,
        budget: String,
        model: String = "gpt-3.5-turbo",
        temperature: Double = 0.7
    ) {
        getGeneratedItinerary(
            destination = cityName,
            duration = duration,
            preferences = preferences,
            budget = budget,
            model = model,
            temperature = temperature
        )
    }

    /**
     * Reset the AI state to idle.
     */
    fun resetState() {
        _aiState.value = AiState.Idle
    }

    private fun buildPrompt(
        destination: String,
        duration: Int,
        preferences: List<String>,
        budget: String
    ): String {
        val preferencesText = if (preferences.isNotEmpty()) {
            preferences.joinToString(", ")
        } else {
            "general sightseeing"
        }

        return """
            Generate a detailed travel itinerary for:
            Destination: $destination
            Duration: $duration days
            Travel Preferences: $preferencesText
            Budget Range: $budget

            Please provide a comprehensive day-by-day itinerary with:
            - Morning, afternoon, and evening activities
            - Transportation suggestions between locations
            - Accommodation recommendations for each budget range
            - Local dining options and must-try dishes
            - Estimated costs for activities and meals
            - Cultural tips and local customs to be aware of
            - Best times to visit attractions to avoid crowds

            Format the response clearly as:
            
            **Day 1:**
            **Morning (9:00 AM - 12:00 PM):** [Activity with location and estimated cost]
            **Afternoon (12:00 PM - 6:00 PM):** [Activity with location and estimated cost]
            **Evening (6:00 PM - 10:00 PM):** [Activity with location and estimated cost]
            
            **Day 2:**
            [Continue same format...]
            
            **Additional Tips:**
            - Transportation: [Local transport options and costs]
            - Accommodation: [Recommendations based on budget]
            - Local Cuisine: [Must-try dishes and restaurants]
            - Cultural Notes: [Important customs and etiquette]
            
            Please ensure all recommendations align with the $budget budget range and $preferencesText interests.
        """.trimIndent()
    }
}
