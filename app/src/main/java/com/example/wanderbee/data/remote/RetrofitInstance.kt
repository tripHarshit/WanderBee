package com.example.wanderbee.data.remote

import com.example.wanderbee.data.remote.apiService.ChatApiService
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.apiService.GenerationService
import com.example.wanderbee.data.remote.apiService.IdentityApiService
import com.example.wanderbee.data.remote.apiService.ImgBBApiService
import com.example.wanderbee.data.remote.apiService.PexelsApiService
import com.example.wanderbee.data.remote.apiService.WeatherApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {

    private const val BACKEND_BASE_URL = "http://10.0.2.2:8082/"

    // Create a logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val backendClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    val identityApi: IdentityApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(backendClient) // public endpoints (login/register) – no auth
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IdentityApiService::class.java)
    }

    /**
     * Authenticated variant of [IdentityApiService] used for protected endpoints
     * such as /auth/validate. The [AuthInterceptor] is a no-op when no token is
     * stored, so public endpoints still work if this client is accidentally used.
     */
    fun createIdentityApi(authClient: OkHttpClient): IdentityApiService {
        return Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(authClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(IdentityApiService::class.java)
    }

    /**
     * Creates a [DestinationApiService] using an authenticated [OkHttpClient]
     * that attaches the JWT token via [AuthInterceptor].
     */
    fun createDestinationApi(authClient: OkHttpClient): DestinationApiService {
        return Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DestinationApiService::class.java)
    }

    /**
     * Creates a [ChatApiService] using an authenticated [OkHttpClient].
     */
    fun createChatApi(authClient: OkHttpClient): ChatApiService {
        return Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ChatApiService::class.java)
    }

    /**
     * Creates a [GenerationService] using an authenticated [OkHttpClient].
     * Used for AI-generated content like itineraries and city insights.
     */
    fun createGenerationService(authClient: OkHttpClient): GenerationService {
        return Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GenerationService::class.java)
    }

    /**
     * Creates a [PexelsApiService] using an authenticated [OkHttpClient].
     * Pexels API calls now go through the backend PexelsController.
     */
    fun createPexelsApi(authClient: OkHttpClient): PexelsApiService {
        return Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PexelsApiService::class.java)
    }

    /**
     * Creates a [WeatherApiService] using an authenticated [OkHttpClient].
     * Weather calls go through the backend WeatherController.
     */
    fun createWeatherApi(authClient: OkHttpClient): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(authClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    private const val IMGBB_BASE_URL = "https://api.imgbb.com/"
    val imgbbApi: ImgBBApiService by lazy {
        Retrofit.Builder()
            .baseUrl(IMGBB_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ImgBBApiService::class.java)
    }
}