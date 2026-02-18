package com.wanderbee.destinationservice.weather.dto;

public record DailyWeather(
        String dayLabel,
        int minTemp,
        int maxTemp,
        String icon,
        String weatherMain,
        boolean isToday
) {}
