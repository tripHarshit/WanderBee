package com.example.wanderbee.data.repository

import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.apiService.GeoDbApiService
import com.example.wanderbee.data.remote.models.destinations.GeoDbResponse
import javax.inject.Inject

interface DestinationsRepository {
    suspend fun getPopularCities(limit: Int): GeoDbResponse
    suspend fun getNearbyCities(latLong: String, limit: Int): GeoDbResponse
}

class DestinationRepository @Inject constructor(
    private val geoDbApi: GeoDbApiService
) : DestinationsRepository {

    private val apiKey = BuildConfig.GEO_DB_API_KEY

    override suspend fun getPopularCities(limit: Int): GeoDbResponse {
        val offset = (0..500).random()
        return geoDbApi.getPopularCities(
            limit = limit,
            offset = offset
        )
    }


    override suspend fun getNearbyCities(latLong: String, limit: Int): GeoDbResponse {
        return geoDbApi.getNearbyCities(latLong, limit, apiKey)
    }
}




