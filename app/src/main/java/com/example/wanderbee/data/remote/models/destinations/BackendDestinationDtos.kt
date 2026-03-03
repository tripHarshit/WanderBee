package com.example.wanderbee.data.remote.models.destinations

import com.google.gson.annotations.SerializedName

/**
 * Matches backend: destination-service → destination.dto.City
 * Used in GeoDbResponse from /api/v1/destinations/popular, /search, /nearby
 */
data class BackendCity(
    val id: Int,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * Matches backend: destination-service → destination.dto.GeoDbResponse
 */
data class BackendGeoDbResponse(
    val data: List<BackendCity>
)

/**
 * Matches backend: destination-service → destination.dto.SaveDestinationRequest
 */
data class SaveDestinationRequest(
    val cityId: String,
    val cityName: String
)

/**
 * Matches backend: destination-service → destination.dto.SavedDestinationResponse
 */
data class SavedDestinationResponse(
    val id: Long?,
    val userId: String,
    val cityId: String,
    val cityName: String,
    val timestamp: String?
)
