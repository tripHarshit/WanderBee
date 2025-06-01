package com.example.wanderbee.data.repository


import com.example.wanderbee.data.remote.apiService.AITask
import com.example.wanderbee.data.remote.apiService.HuggingFaceApiService
import com.example.wanderbee.data.remote.models.AI.GeneratedTextResult
import com.example.wanderbee.data.remote.models.AI.HuggingFaceRequest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.example.wanderbee.BuildConfig

interface HuggingFaceRepository {
    suspend fun getAIResponse(
        task: AITask,
        prompt: String,
        parameters: Map<String, Any>? = null
    ): Flow<Result<List<GeneratedTextResult>>>
}

class DefaultHuggingFaceRepository @Inject constructor(
    val huggingFaceApiService: HuggingFaceApiService
): HuggingFaceRepository{

    override suspend fun getAIResponse(
        task: AITask,
        prompt: String,
        parameters: Map<String, Any>?
    ): Flow<Result<List<GeneratedTextResult>>> = flow {

        try{
             val request = HuggingFaceRequest(
                 inputs = prompt,
                 parameters = parameters
             )
            val authHeader = "Bearer ${BuildConfig.HUGGINGFACE_API_KEY}"

            val response = huggingFaceApiService.getResponse(
                modelId = task.modelId,
                authHeader = authHeader,
                userRequest = request
            )
            emit(Result.success(response))
        }catch (e: Exception){
            emit(Result.failure(e))
        }
    }
}