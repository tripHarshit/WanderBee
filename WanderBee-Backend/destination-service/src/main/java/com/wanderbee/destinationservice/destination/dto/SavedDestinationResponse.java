package com.wanderbee.destinationservice.destination.dto;

import java.time.LocalDateTime;

public record SavedDestinationResponse(
        Long id,
        String userId,
        String cityId,
        String cityName,
        LocalDateTime timestamp
) {}
