package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.models.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): WeatherResponse
}