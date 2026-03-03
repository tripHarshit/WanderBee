package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.apiService.GenerationService
import com.example.wanderbee.data.remote.models.AI.CityInsights
import com.example.wanderbee.data.remote.models.AI.ItineraryResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AiRepository {
    suspend fun generateItinerary(
        city: String,
        days: Int,
        budget: String,
        travellers: Int,
        interests: String = "general sightseeing"
    ): ItineraryResponse

    suspend fun getCityInsights(cityName: String): CityInsights
}

/**
 * Calls the backend ItineraryController and InsightController
 * via the API Gateway.
 * The backend handles the AI prompt engineering and returns
 * structured responses directly.
 */
class DefaultAiRepository @Inject constructor(
    private val generationService: GenerationService
) : AiRepository {

    override suspend fun generateItinerary(
        city: String,
        days: Int,
        budget: String,
        travellers: Int,
        interests: String
    ): ItineraryResponse = withContext(Dispatchers.IO) {
        Log.d(TAG, "Requesting itinerary: city=$city, days=$days, budget=$budget, travellers=$travellers")
        Log.d(TAG, "Interests: $interests")

        val response = generationService.generateItinerary(
            city = city,
            days = days,
            budget = budget,
            travellers = travellers,
            interests = interests
        )

        if (response.isSuccessful) {
            val itinerary = response.body()
                ?: throw Exception("Empty itinerary response from server")
            Log.d(TAG, "Itinerary generated: ${itinerary.tripTitle}")
            itinerary
        } else {
            val errorMsg = when (response.code()) {
                400  -> "Invalid request — please check your inputs"
                408  -> "Itinerary generation timed out — please try again"
                429  -> "Too many requests — please wait a moment"
                in 500..599 -> "Itinerary service temporarily unavailable"
                else -> "Itinerary generation failed (${response.code()})"
            }
            Log.e(TAG, "$errorMsg — ${response.message()}")
            throw Exception(errorMsg)
        }
    }

    override suspend fun getCityInsights(cityName: String): CityInsights = withContext(Dispatchers.IO) {
        Log.d(TAG, "Requesting insights for: $cityName")

        val response = generationService.getCityInsights(cityName)

        if (response.isSuccessful) {
            val insights = response.body()
                ?: throw Exception("Empty insights response from server")
            Log.d(TAG, "Insights loaded for: $cityName")
            insights
        } else {
            val errorMsg = when (response.code()) {
                400  -> "Invalid city name"
                404  -> "City not found: $cityName"
                408  -> "Insights request timed out — please try again"
                429  -> "Too many requests — please wait a moment"
                in 500..599 -> "Insights service temporarily unavailable"
                else -> "Failed to get city insights (${response.code()})"
            }
            Log.e(TAG, "$errorMsg — ${response.message()}")
            throw Exception(errorMsg)
        }
    }

    companion object {
        private const val TAG = "AiRepository"
    }
}
