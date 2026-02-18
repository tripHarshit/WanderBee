package com.wanderbee.destinationservice.destination.controller;

import com.wanderbee.destinationservice.destination.dto.GeoDbResponse;
import com.wanderbee.destinationservice.media.dto.PexelsPhotoResponse;
import com.wanderbee.destinationservice.media.dto.PexelsVideoResponse;
import com.wanderbee.destinationservice.destination.service.DestinationService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/destinations")
public class DestinationController {

    private final DestinationService destinationService;

    public DestinationController(DestinationService destinationService) {this.destinationService = destinationService;}

    @GetMapping("/popular")
    public ResponseEntity<GeoDbResponse> getPopular(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(destinationService.getPopularCities(limit));
    }

    @GetMapping("/nearby")
    public ResponseEntity<GeoDbResponse> getNearby(
            @RequestParam String latLon,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(destinationService.getNearbyCities(latLon, limit));
    }

    @GetMapping("/search")
    public ResponseEntity<GeoDbResponse> searchCities(
            @RequestParam String namePrefix,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(destinationService.searchCities(namePrefix,limit,offset));
    }

}
