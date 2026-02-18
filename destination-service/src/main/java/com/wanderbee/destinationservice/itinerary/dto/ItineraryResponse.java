package com.wanderbee.destinationservice.itinerary.dto;

import java.util.List;

public record ItineraryResponse(
        String tripTitle,
        String destination,
        String duration,
        String budgetRange,
        int numberOfTravellers,
        String totalEstimatedTripCost,
        List<DayPlan> days
) {}

