package com.wanderbee.destinationservice.itinerary.dto;

public record Activity(
        String time,
        String title,
        String description,
        String locationHint
) {}
