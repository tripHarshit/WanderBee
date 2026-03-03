package com.wanderbee.destinationservice.itinerary.dto;

import java.util.List;

public record DayPlan(
        int dayNumber,
        String theme,
        String estimatedDailyCost,
        List<Activity> activities
) {}
