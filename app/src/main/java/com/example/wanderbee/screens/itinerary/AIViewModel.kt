package com.example.wanderbee.screens.itinerary

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.remote.models.AI.ItineraryResponse
import com.example.wanderbee.data.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _aiState = MutableStateFlow<AiState>(AiState.Idle)
    val aiState: StateFlow<AiState> = _aiState.asStateFlow()

    /**
     * Generate itinerary via the backend itinerary-service.
     * The backend handles prompt engineering — we just send the user's choices.
     */
    fun getGeneratedItinerary(
        destination: String,
        duration: Int,
        preferences: List<String>,
        travellers: Int,
        budget: String
    ) {
        val interests = if (preferences.isNotEmpty()) {
            preferences.joinToString(", ")
        } else {
            "general sightseeing"
        }

        viewModelScope.launch {
            _aiState.value = AiState.Loading
            try {
                val itinerary = aiRepository.generateItinerary(
                    city = destination,
                    days = duration,
                    budget = budget,
                    travellers = travellers,
                    interests = interests
                )
                _aiState.value = AiState.Success(itinerary)
                Log.d(TAG, "Itinerary generated: ${itinerary.tripTitle}")
            } catch (e: Exception) {
                _aiState.value = AiState.Error(e.message ?: "Failed to generate itinerary")
                Log.e(TAG, "Error generating itinerary: ${e.message}", e)
            }
        }
    }

    /**
     * Reset the AI state to idle.
     */
    fun resetState() {
        _aiState.value = AiState.Idle
    }

    companion object {
        private const val TAG = "AiViewModel"
    }
}
