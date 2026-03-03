package com.example.wanderbee.data.remote.models.destinations

/**
 * Represents a static destination from the backend's static JSON files.
 * Maps to GET /api/v1/destinations/static/{name}
 * 
 * Indian destinations use "state" while international use "country" and "region"
 */
data class StaticDestination(
    val name: String,
    val lat: Double,
    val lon: Double,
    val currency: String,
    val language: String,
    val timezone: String,
    val countryCode: String,
    val tags: List<String>,
    // For Indian destinations
    val state: String? = null,
    // For international destinations
    val country: String? = null,
    val region: String? = null
)
