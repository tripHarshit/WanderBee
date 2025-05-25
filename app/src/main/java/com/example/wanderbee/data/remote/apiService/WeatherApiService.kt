package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.weather.FiveDayForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/forecast")
    suspend fun getFiveDayForecast(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): FiveDayForecastResponse
}
