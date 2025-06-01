package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.AI.HuggingFaceRequest
import com.example.wanderbee.data.remote.models.AI.HuggingFaceResponse
import retrofit2.http.POST
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path

interface HuggingFaceApiService {
    @POST("models/{modelId}")
    suspend fun getResponse(
        @Path("modelId") modelId: String,
        @Header("Authorization") authHeader: String,
        @Body userRequest: HuggingFaceRequest
    ): HuggingFaceResponse
}


sealed class AITask(val modelId: String) {
    object DestinationSuggestion : AITask("google/flan-t5-base")
    object ItineraryGenerator : AITask("mistralai/Mixtral-8x7B-Instruct-v0.1")
    object Translation : AITask("facebook/nllb-200-distilled-600M")
    object Chatbot : AITask("meta-llama/Llama-2-7b-chat")
  object CityInfoTextGeneration : AITask("mistralai/Mixtral-8x7B-Instruct-v0.1")
}