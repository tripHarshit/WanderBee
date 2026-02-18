package com.wanderbee.destinationservice.insights.dto;

import java.util.List;

public record CityInsights(
        String description,
        List<String> culturalTips,
        String bestTimeToVisit,
        String language,
        String currency
) {}
