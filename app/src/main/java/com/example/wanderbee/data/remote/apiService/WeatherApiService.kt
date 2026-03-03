package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.weather.DailyWeather
import com.example.wanderbee.data.remote.models.weather.FiveDayForecastResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query


interface WeatherApiService {

    @GET("api/v1/weather/{cityName}")
    suspend fun getForecast(
        @Path("cityName") cityName: String
    ): Response<List<DailyWeather>>
}
