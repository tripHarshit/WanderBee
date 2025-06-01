package com.example.wanderbee.data.remote.apiService


import com.example.wanderbee.data.remote.models.AI.AiRequest
import com.example.wanderbee.data.remote.models.AI.AiResponse
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST


interface AiApiService {
    @Headers("Content-Type: application/json")
    @POST("v1/chat/completions")
    suspend fun generate(
        @Header("Authorization") authorization: String,
        @Body request: AiRequest  // Use specific data class instead of Map
    ): AiResponse
}
