package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.models.PexelsPhotoResponse
import com.example.wanderbee.data.remote.models.PexelsVideoResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface PexelsApiService {

    @GET("v1/search")
    suspend fun searchPhotos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 10
    ): PexelsPhotoResponse

    @GET("videos/search")
    suspend fun searchVideos(
        @Header("Authorization") apiKey: String,
        @Query("query") query: String,
        @Query("per_page") perPage: Int = 10
    ): PexelsVideoResponse
}

