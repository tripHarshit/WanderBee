package com.wanderbee.destinationservice.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ForecastItem(
        long dt,
        MainWeatherData main,
        List<Weather> weather,
        @JsonProperty("dt_txt") String dtTxt
) {}
