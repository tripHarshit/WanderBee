package com.example.wanderbee.screens.itinerary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.models.AI.ItineraryResponse
import com.example.wanderbee.data.repository.DefaultAiRepository
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.sql.Date
import javax.inject.Inject

// State Management
sealed class AiState {
    object Idle : AiState()
    object Loading : AiState()
    data class Success(val itinerary: ItineraryResponse) : AiState()
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
     */
    fun getGeneratedItinerary(
        destination: String,
        duration: Int,
        startDate: String,
        endDate: String,
        preferences: List<String>,
        travellers: Int,
        budget: String,
        model: String = "gpt-4.1-nano",
        temperature: Double = 0.7
    ) {
        val prompt = buildPrompt(destination, duration, startDate , endDate , preferences , budget, travellers)
        viewModelScope.launch {
            _aiState.value = AiState.Loading
            try {
                val result = aiRepository.generateFromPrompt(
                    apiKey = BuildConfig.AI_API_KEY,
                    prompt = prompt,
                    model = model,
                    temperature = temperature
                )

                if (!result.isNullOrEmpty()) {
                    val itinerary = Gson().fromJson(result, ItineraryResponse::class.java)
                    _aiState.value = AiState.Success(itinerary)
                    Log.d("ItineraryGenerated", itinerary.toString())
                } else {
                    _aiState.value = AiState.Error("No response from AI")
                }

            } catch (e: Exception) {
                _aiState.value = AiState.Error(e.message ?: "Failed to generate itinerary")
                Log.e("AiError", "Error generating itinerary: ${e.message}", e)
            }
        }
    }

    /**
     * Reset the AI state to idle.
     */
    fun resetState() {
        _aiState.value = AiState.Idle
    }

    /**
     * Build AI prompt based on user input.
     */
    private fun buildPrompt(
        destination: String,
        duration: Int,
        startDate: String,
        endDate: String,
        preferences: List<String>,
        budget: String,
        travellers: Int
    ): String {
        val preferencesText = if (preferences.isNotEmpty()) {
            preferences.joinToString(", ")
        } else {
            "general sightseeing"
        }

        return """
            Generate a detailed travel itinerary as a valid JSON object for the following trip:

- Destination: $destination
- Duration: $duration days
- From: $startDate to: $endDate
- Travel Preferences: $preferencesText
- Budget Range: $budget
- Number of Travellers: $travellers

Use the following format for your JSON response:

{
  "days": [
    {
      "dayNumber": 1,
      "date": "YYYY-MM-DD",
      "totalCost": "$125",
      "timeSlots": [
        {
          "time": "8:00 AM - 10:00 AM",
          "activity": "Visit the Kyoto Imperial Palace",
          "location": "Kyoto",
          "cost": "$10",
          "transportation": "Take subway from central station",
          "dining": "Optional light breakfast at nearby bakery"
        },
        {
          "time": "10:30 AM - 12:30 PM",
          "activity": "Tour Nijo Castle and gardens",
          "location": "Kyoto",
          "cost": "$8",
          "transportation": "Walk from previous location",
          "dining": "Try local snacks nearby"
        },
        {
          "time": "1:00 PM - 3:00 PM",
          "activity": "Lunch and cultural stroll in Nishiki Market",
          "location": "Downtown Kyoto",
          "cost": "$20",
          "transportation": "Bus or on foot",
          "dining": "Try Takoyaki and Matcha parfait"
        },
        {
          "time": "3:30 PM - 5:30 PM",
          "activity": "Relax at Maruyama Park or explore tea houses",
          "location": "Gion area",
          "cost": "Free",
          "transportation": "Taxi or local tram",
          "dining": "Evening tea or dessert"
        },
        {
          "time": "6:00 PM - 9:00 PM",
          "activity": "Dinner and cultural show at Gion Corner",
          "location": "Gion District",
          "cost": "$40",
          "transportation": "Walk",
          "dining": "Kaiseki dinner with seasonal specialties"
        }
      ]
    },
    ...
  ],
  "accommodation": {
    "budget": "$budget",
    "recommendation": "ABC Hotel Kyoto for $budget budget"
  },
  "localDishes": [
    "Soba noodles",
    "Yuba (tofu skin)",
    "Kyo Wagashi (traditional sweets)"
  ]
}

Instructions:
- Return only **valid JSON** (no markdown, no bullet points, no extra text).
- Use **realistic time slots** (2–3 hour ranges per activity).
- Maximize day coverage from **around 8:00 AM to 9:00 PM**.
- `totalCost` should reflect approximate sum of activities and meals.
- Activities must reflect the given preferences and budget.
- Only include one accommodation suggestion that matches the given budget tier.

        """.trimIndent()
    }
}
