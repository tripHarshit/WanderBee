package com.example.wanderbee.data.remote

import com.example.wanderbee.data.remote.apiService.GeoDbApiService
import com.example.wanderbee.data.remote.apiService.HuggingFaceApiService
import com.example.wanderbee.data.remote.apiService.PexelsApiService
import com.example.wanderbee.data.remote.apiService.WeatherApiService
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val WEATHER_BASE_URL = "https://api.openweathermap.org/"
    val weatherApi: WeatherApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }

    private const val GEODB_BASE_URL = "https://wft-geo-db.p.rapidapi.com/"

    // Create a logging interceptor
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // You can use BASIC or HEADERS if BODY is too verbose
    }

    // Create your OkHttpClient with both GeoDbInterceptor and logging
    private val geoDbClient = OkHttpClient.Builder()
        .addInterceptor(GeoDbInterceptor()) // Your custom interceptor for headers
        .addInterceptor(loggingInterceptor) // Logging interceptor
        .build()

    val geoDbApi: GeoDbApiService by lazy {
        Retrofit.Builder()
            .baseUrl(GEODB_BASE_URL)
            .client(geoDbClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeoDbApiService::class.java)
    }

    private const val HUGGINGFACE_BASE_URL = "https://api-inference.huggingface.co/"
    val huggingFaceApi: HuggingFaceApiService by lazy {
        Retrofit.Builder()
            .baseUrl(HUGGINGFACE_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(HuggingFaceApiService::class.java)
    }

    private const val PEXELS_BASE_URL = "https://api.pexels.com/"
    val pexelsApi: PexelsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(PEXELS_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PexelsApiService::class.java)
    }


}