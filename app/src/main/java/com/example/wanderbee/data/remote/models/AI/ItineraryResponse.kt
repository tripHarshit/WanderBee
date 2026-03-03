package com.example.wanderbee.data.remote.models.AI

/**
 * Matches backend: destination-service → itinerary.dto.ItineraryResponse
 * Returned by GET /api/v1/itinerary/generate
 */
data class ItineraryResponse(
    val tripTitle: String,
    val destination: String,
    val duration: String,
    val budgetRange: String,
    val numberOfTravellers: Int,
    val totalEstimatedTripCost: String,
    val days: List<DayPlan>
)

data class DayPlan(
    val dayNumber: Int,
    val theme: String,
    val estimatedDailyCost: String,
    val activities: List<Activity>
)

data class Activity(
    val time: String,
    val title: String,
    val description: String,
    val locationHint: String
)
