package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.destinations.GeoDbResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface GeoDbApiService {

    @GET("v1/geo/cities")
    suspend fun getPopularCities(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): GeoDbResponse

    @GET("v1/geo/cities")
    suspend fun searchCities(
        @Query("namePrefix") namePrefix: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0,
        @Query("sort") sort: String = "-population"
    ): GeoDbResponse

    @GET("v1/geo/locations/{latLong}/nearbyCities")
    suspend fun getNearbyCities(
        @Path("latLong") latLong: String,
        @Query("limit") limit: Int,
        @Header("X-RapidAPI-Key") apiKey: String = "b4700d5a56mshc19b401188e9a7fp1537bbjsnc9360e785533",
        @Header("X-RapidAPI-Host") host: String = "wft-geo-db.p.rapidapi.com"
    ): GeoDbResponse
}

