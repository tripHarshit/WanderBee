package com.example.wanderbee.data.remote.models.AI

/**
 * Matches backend: destination-service → insights.dto.CityInsights
 * Returned by GET /api/v1/description/{cityName}/insights
 */
data class CityInsights(
    val description: String,
    val culturalTips: List<String>,
    val bestTimeToVisit: String,
    val language: String,
    val currency: String
)
