package com.wanderbee.destinationservice.weather.client;

import com.wanderbee.destinationservice.weather.dto.FiveDayForecastResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "weather-api", url = "https://api.openweathermap.org")
public interface WeatherClient {

    @GetMapping("/data/2.5/forecast")
    FiveDayForecastResponse getFiveDayForecast(
            @RequestParam("q") String cityName,
            @RequestParam("appid") String apiKey,
            @RequestParam("units") String units
    );
}
