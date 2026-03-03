package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.AI.CityInsights
import com.example.wanderbee.data.remote.models.AI.ItineraryResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GenerationService {

    // ── ItineraryController ─────────────────────────────────────────────

    /**
     * Generate a personalized travel itinerary for a city.
     * Maps to backend: GET /api/v1/itinerary/generate
     */
    @GET("api/v1/itinerary/generate")
    suspend fun generateItinerary(
        @Query("city") city: String,
        @Query("days") days: Int,
        @Query("budget") budget: String,
        @Query("travellers") travellers: Int,
        @Query("interests") interests: String = "general sightseeing"
    ): Response<ItineraryResponse>

    @GET("api/v1/description/{cityName}/insights")
    suspend fun getCityInsights(
        @Path("cityName") cityName: String
    ): Response<CityInsights>
}