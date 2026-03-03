package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.AI.ItineraryResponse
import com.example.wanderbee.data.remote.models.destinations.BackendGeoDbResponse
import com.example.wanderbee.data.remote.models.destinations.SaveDestinationRequest
import com.example.wanderbee.data.remote.models.destinations.SavedDestinationResponse
import com.example.wanderbee.data.remote.models.destinations.StaticDestination
import com.example.wanderbee.data.remote.models.media.PexelsPhotoResponse
import com.example.wanderbee.data.remote.models.media.PexelsVideoResponse
import com.example.wanderbee.data.remote.models.weather.DailyWeather
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query


interface DestinationApiService {

    // ── DestinationController ───────────────────────────────────────────

    @GET("api/v1/destinations/popular")
    suspend fun getPopularCities(
        @Query("limit") limit: Int = 10
    ): Response<BackendGeoDbResponse>

    @GET("api/v1/destinations/nearby")
    suspend fun getNearbyCities(
        @Query("latLon") latLon: String,
        @Query("limit") limit: Int = 10
    ): Response<BackendGeoDbResponse>

    @GET("api/v1/destinations/search")
    suspend fun searchCities(
        @Query("namePrefix") namePrefix: String,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): Response<BackendGeoDbResponse>

    /**
     * Fetch static destinations by name.
     * @param name Use "india" for Indian destinations or "all" for all destinations
     */
    @GET("api/v1/destinations/static/{name}")
    suspend fun getStaticDestinations(
        @Path("name") name: String
    ): Response<List<StaticDestination>>

    /**
     * Convenience method to get Indian destinations
     */
    suspend fun getIndianDestinations(): Response<List<StaticDestination>> =
        getStaticDestinations("india")

    /**
     * Convenience method to get all destinations
     */
    suspend fun getAllDestinations(): Response<List<StaticDestination>> =
        getStaticDestinations("all")

    @POST("api/v1/destinations/save")
    suspend fun saveDestination(
        @Body request: SaveDestinationRequest
    ): Response<SavedDestinationResponse>

    @GET("api/v1/destinations/saved/{userId}")
    suspend fun getSavedDestinations(
        @Path("userId") userId: String
    ): Response<List<SavedDestinationResponse>>

    @DELETE("api/v1/destinations/save/{cityId}")
    suspend fun unsaveDestination(
        @Path("cityId") cityId: String
    ): Response<Unit>


}
