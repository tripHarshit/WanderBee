package com.wanderbee.destinationservice.destination.dto;

import jakarta.validation.constraints.NotBlank;

public record SaveDestinationRequest(
        @NotBlank(message = "cityId is required") String cityId,
        @NotBlank(message = "cityName is required") String cityName
) {}
