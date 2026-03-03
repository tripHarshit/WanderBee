package com.wanderbee.destinationservice.media.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record PexelsVideo(
        int id,
        @JsonProperty("video_files") List<PexelsVideoFile> videoFiles,
        String image
) {}
