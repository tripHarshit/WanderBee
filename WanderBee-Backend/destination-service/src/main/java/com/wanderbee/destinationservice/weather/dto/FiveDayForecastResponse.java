package com.wanderbee.destinationservice.weather.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FiveDayForecastResponse(
        List<ForecastItem> list,
        City city
) {}

