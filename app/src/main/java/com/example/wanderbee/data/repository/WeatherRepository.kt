package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.models.weather.DailyWeather
import com.example.wanderbee.data.remote.apiService.WeatherApiService
import javax.inject.Inject

interface WeatherRepository {
    suspend fun getDailyWeatherForecast(cityName: String): List<DailyWeather>
}

/**
 * Fetches weather forecasts from the backend WeatherController
 * (GET /api/v1/weather/{cityName}) via the API Gateway.
 * The backend calls OpenWeather internally and returns processed DailyWeather.
 */
class DefaultWeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRepository {

    override suspend fun getDailyWeatherForecast(cityName: String): List<DailyWeather> {
        Log.d(TAG, "Fetching weather forecast for: $cityName")
        val response = weatherApiService.getForecast(cityName)

        if (response.isSuccessful) {
            val forecast = response.body() ?: emptyList()
            Log.d(TAG, "Weather loaded: ${forecast.size} days")
            return forecast
        } else {
            val errorMsg = when (response.code()) {
                204  -> "No weather data available for $cityName"
                404  -> "City not found: $cityName"
                in 500..599 -> "Weather service temporarily unavailable"
                else -> "Weather request failed (${response.code()})"
            }
            Log.e(TAG, errorMsg)
            throw Exception(errorMsg)
        }
    }

    companion object {
        private const val TAG = "WeatherRepository"
    }
}
