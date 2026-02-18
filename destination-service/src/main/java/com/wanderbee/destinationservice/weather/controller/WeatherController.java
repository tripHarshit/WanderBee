package com.wanderbee.destinationservice.weather.controller;

import com.wanderbee.destinationservice.weather.dto.DailyWeather;
import com.wanderbee.destinationservice.weather.service.WeatherService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{cityName}")
    public ResponseEntity<List<DailyWeather>> getForecast(@PathVariable String cityName) {
        List<DailyWeather> forecast = weatherService.getDailyForecast(cityName);

        if (forecast.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(forecast);
    }
}
