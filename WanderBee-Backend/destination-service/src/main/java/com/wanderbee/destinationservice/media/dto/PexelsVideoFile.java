package com.wanderbee.destinationservice.media.dto;

public record PexelsVideoFile(
        String link,
        String quality,
        int width,
        int height
) {}
