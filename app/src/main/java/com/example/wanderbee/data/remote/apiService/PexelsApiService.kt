package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.media.PexelsPhotoResponse
import com.example.wanderbee.data.remote.models.media.PexelsVideoResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Path

interface PexelsApiService {


    @GET("api/v1/media/{cityName}/photos")
    suspend fun getBackendPhotos(
        @Path("cityName") cityName: String,
        @Query("perPage") perPage: Int = 30
    ): Response<PexelsPhotoResponse>


    @GET("api/v1/media/{cityName}/videos")
    suspend fun getBackendVideos(
        @Path("cityName") cityName: String,
        @Query("perPage") perPage: Int = 30
    ): Response<PexelsVideoResponse>

}

