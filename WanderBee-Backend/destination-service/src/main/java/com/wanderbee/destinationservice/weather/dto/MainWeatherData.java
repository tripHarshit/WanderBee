package com.wanderbee.destinationservice.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MainWeatherData(
        @JsonProperty("temp_min") double tempMin,
        @JsonProperty("temp_max") double tempMax
) {}
