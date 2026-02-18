package com.wanderbee.destinationservice.destination.dto;

import jakarta.validation.constraints.NotBlank;

public record City(
        int id,
        @NotBlank String name,
        @NotBlank String country,
        double latitude,
        double longitude
) {}

