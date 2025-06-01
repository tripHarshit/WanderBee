package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.apiService.AiApiService
import com.example.wanderbee.data.remote.models.AI.AiMessage
import com.example.wanderbee.data.remote.models.AI.AiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface AiRepository {
    suspend fun generateFromPrompt(
        apiKey: String,
        prompt: String,
        model: String = "gpt-3.5-turbo",
        temperature: Double = 0.7
    ): String?
}


class DefaultAiRepository @Inject constructor(
    private val aiApiService: AiApiService
) : AiRepository {
    override suspend fun generateFromPrompt(
        apiKey: String,
        prompt: String,
        model: String,
        temperature: Double
    ): String? = withContext(Dispatchers.IO) {
        Log.d("DefaultAiRepository", "Starting API call with model: $model, temperature: $temperature")
        Log.d("DefaultAiRepository", "Prompt: $prompt")

        // Create request object instead of Map
        val request = AiRequest(
            model = model,
            messages = listOf(AiMessage(role = "user", content = prompt)),
            temperature = temperature
        )

        Log.d("DefaultAiRepository", "Request: $request")

        try {
            val response = aiApiService.generate(
                authorization = "Bearer $apiKey",
                request = request  // Pass the data class
            )

            Log.d("DefaultAiRepository", "API call successful")
            Log.d("DefaultAiRepository", "Full response: $response")

            val content = response.choices.firstOrNull()?.message?.content
            Log.d("DefaultAiRepository", "Generated content: $content")

            return@withContext content
        } catch (e: Exception) {
            Log.e("DefaultAiRepository", "Error generating text: ${e.message}", e)
            Log.e("DefaultAiRepository", "Exception type: ${e.javaClass.simpleName}")
            return@withContext null
        }
    }
}
