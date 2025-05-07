package com.example.wanderbee.data.remote.models

import com.example.wanderbee.MainActivity

data class WeatherResponse(
    val weather: List<Weather>,
    val main: Main,
    val nam: String,
)

data class Weather(
    val main: String,
    val description: String,
)

data class Main(
    val temp: Double,
    val humidity: Int,

)