package com.example.wanderbee.data.remote.models.destinations

data class GeoDbResponse(
    val data: List<City>
)

data class City(
    val id: Int,
    val name: String,
    val country: String,
    val latitude: Double,
    val longitude: Double
)
