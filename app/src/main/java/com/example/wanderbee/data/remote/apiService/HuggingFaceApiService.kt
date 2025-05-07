package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.models.HuggingFaceRequest
import com.example.wanderbee.data.remote.models.HuggingFaceResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path

interface HuggingFaceApiService {
    @POST("models/{modelId}")
    suspend fun getRecommendations(
        @Path("modelId") modelId: String,
        @Header("Authorization") authHeader: String,
        @Body userRequest: HuggingFaceRequest
    ): HuggingFaceResponse
}

sealed class AITask(val modelId: String) {
    object DestinationSuggestion : AITask("google/flan-t5-base")
    object ItineraryGenerator : AITask("google/flan-t5-xl")
    object CulturalTips : AITask("mistralai/Mixtral")
    object Translation : AITask("facebook/nllb-200-distilled-600M")
    object Chatbot : AITask("meta-llama/Llama-2-7b-chat")
}