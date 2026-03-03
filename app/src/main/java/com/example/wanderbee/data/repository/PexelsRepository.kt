package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.apiService.PexelsApiService
import com.example.wanderbee.data.remote.models.media.PexelsPhotoResponse
import com.example.wanderbee.data.remote.models.media.PexelsVideoResponse
import javax.inject.Inject

interface PexelsRepository {

    suspend fun getBackendPhotos(cityName: String, perPage: Int = 30): PexelsPhotoResponse
    suspend fun getBackendVideos(cityName: String, perPage: Int = 30): PexelsVideoResponse
}

class DefaultPexelsRepository @Inject constructor(
    private val pexelsApiService: PexelsApiService,
    private val destinationApiService: DestinationApiService
): PexelsRepository {

    companion object {
        private const val TAG = "PexelsRepository"
    }


    override suspend fun getBackendPhotos(cityName: String, perPage: Int): PexelsPhotoResponse {
        Log.d(TAG, "Fetching photos via backend for: $cityName")
        val response = pexelsApiService.getBackendPhotos(cityName, perPage)

        if (response.isSuccessful) {
            return response.body() ?: PexelsPhotoResponse(photos = emptyList())
        } else {
            val errorMsg = when (response.code()) {
                404 -> "City not found: $cityName"
                in 500..599 -> "Media service temporarily unavailable"
                else -> "Failed to fetch photos (${response.code()})"
            }
            Log.e(TAG, errorMsg)
            throw Exception(errorMsg)
        }
    }

    override suspend fun getBackendVideos(cityName: String, perPage: Int): PexelsVideoResponse {
        Log.d(TAG, "Fetching videos via backend for: $cityName")
        val response = pexelsApiService.getBackendVideos(cityName, perPage)

        if (response.isSuccessful) {
            return response.body() ?: PexelsVideoResponse(videos = emptyList())
        } else {
            val errorMsg = when (response.code()) {
                404 -> "City not found: $cityName"
                in 500..599 -> "Media service temporarily unavailable"
                else -> "Failed to fetch videos (${response.code()})"
            }
            Log.e(TAG, errorMsg)
            throw Exception(errorMsg)
        }
    }
}