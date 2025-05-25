package com.example.wanderbee.data.repository

import com.example.wanderbee.BuildConfig
import com.example.wanderbee.data.remote.apiService.WeatherApiService
import com.example.wanderbee.data.remote.models.weather.DailyWeather
import com.example.wanderbee.data.remote.models.weather.FiveDayForecastResponse
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

interface WeatherRepository {
    suspend fun getFiveDayForecast(cityName: String): FiveDayForecastResponse
    suspend fun getDailyWeatherForecast(cityName: String): List<DailyWeather>
}

class DefaultWeatherRepository @Inject constructor(
    private val weatherApiService: WeatherApiService
) : WeatherRepository {

    override suspend fun getFiveDayForecast(cityName: String): FiveDayForecastResponse {
        return weatherApiService.getFiveDayForecast(
            cityName = cityName,
            apiKey = BuildConfig.OPENWEATHER_API_KEY
        )
    }

    override suspend fun getDailyWeatherForecast(cityName: String): List<DailyWeather> {
        val forecastResponse = getFiveDayForecast(cityName)
        return processFiveDayForecast(forecastResponse)
    }

    private fun processFiveDayForecast(forecastResponse: FiveDayForecastResponse): List<DailyWeather> {
        val groupedByDate = forecastResponse.list.groupBy { forecast ->
            forecast.dtTxt.substring(0, 10)
        }

        val dailyWeatherList = mutableListOf<DailyWeather>()
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        groupedByDate.forEach { (dateStr, forecasts) ->
            val minTemp = forecasts.minOf { it.main.tempMin }.toInt()
            val maxTemp = forecasts.maxOf { it.main.tempMax }.toInt()

            val representativeForecast = forecasts.minByOrNull { forecast ->
                val time = forecast.dtTxt.substring(11, 16)
                abs(time.replace(":", "").toInt() - 1200)
            } ?: forecasts.first()

            val isToday = dateStr == today
            val dayLabel = if (isToday) {
                "Today"
            } else {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                SimpleDateFormat("EEE", Locale.getDefault()).format(date!!)
            }

            dailyWeatherList.add(
                DailyWeather(
                    dayLabel = dayLabel,
                    minTemp = minTemp,
                    maxTemp = maxTemp,
                    icon = representativeForecast.weather[0].icon,
                    weatherMain = representativeForecast.weather[0].main,
                    isToday = isToday
                )
            )
        }

        return dailyWeatherList.take(5)
    }
}
