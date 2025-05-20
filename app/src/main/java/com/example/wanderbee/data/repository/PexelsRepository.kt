package com.example.wanderbee.data.repository

import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.apiService.PexelsApiService
import com.example.wanderbee.data.remote.models.media.PexelsPhotoResponse
import com.example.wanderbee.data.remote.models.media.PexelsVideoResponse
import javax.inject.Inject

interface PexelsRepository {
    suspend fun getPexelsPhotos(query: String): PexelsPhotoResponse
    suspend fun getPexelsVideos(query: String): PexelsVideoResponse
}

class DefaultPexelsRepository @Inject constructor(
    private val pexelsApiService: PexelsApiService
): PexelsRepository{
    override suspend fun getPexelsPhotos(query: String): PexelsPhotoResponse {
        return pexelsApiService.searchPhotos(apiKey = BuildConfig.PEXELS_API_KEY, query = query)
    }

    override suspend fun getPexelsVideos(query: String): PexelsVideoResponse {
        return pexelsApiService.searchVideos(apiKey = BuildConfig.PEXELS_API_KEY,query = query)
    }
}