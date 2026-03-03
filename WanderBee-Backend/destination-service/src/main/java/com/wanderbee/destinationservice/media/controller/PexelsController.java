package com.wanderbee.destinationservice.media.controller;

import com.wanderbee.destinationservice.destination.dto.GeoDbResponse;
import com.wanderbee.destinationservice.media.dto.PexelsPhotoResponse;
import com.wanderbee.destinationservice.media.dto.PexelsVideoResponse;
import com.wanderbee.destinationservice.destination.service.DestinationService;
import com.wanderbee.destinationservice.media.service.PexelsService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
    @RequestMapping("/api/v1/media")
public class PexelsController {

    private final PexelsService pexelsService;

    public PexelsController(PexelsService pexelsService){this.pexelsService = pexelsService;}

    @GetMapping("/{cityName}/photos")
    public ResponseEntity<PexelsPhotoResponse> getPexelsPhotos(
            @PathVariable String cityName,
            @RequestParam(defaultValue = "30")  @Min(value = 1, message = "Page size must be at least 1") int perPage
    ){
        return ResponseEntity.ok(pexelsService.getPexelsPhotos(cityName,perPage));
    }

    @GetMapping("/{cityName}/videos")
    public ResponseEntity<PexelsVideoResponse> getPexelsVideos(
            @PathVariable String cityName,
            @RequestParam(defaultValue = "30") @Min(value = 1, message = "Page size must be at least 1") int perPage
    ){
        return ResponseEntity.ok(pexelsService.getPexelsVideos(cityName,perPage));
    }

}

