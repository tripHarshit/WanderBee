package com.example.wanderbee.data.remote.models.weather

import com.google.gson.annotations.SerializedName

data class FiveDayForecastResponse(
    val list: List<ForecastItem>,
    val city: City
)

data class ForecastItem(
    val dt: Long,
    val main: MainWeatherData,
    val weather: List<Weather>,
    @SerializedName("dt_txt")
    val dtTxt: String
)

data class MainWeatherData(
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double
)

data class Weather(
    val main: String,
    val icon: String
)

data class City(
    val name: String
)

data class DailyWeather(
    val dayLabel: String,
    val minTemp: Int,
    val maxTemp: Int,
    val icon: String,
    val weatherMain: String,
    val isToday: Boolean = false
)
