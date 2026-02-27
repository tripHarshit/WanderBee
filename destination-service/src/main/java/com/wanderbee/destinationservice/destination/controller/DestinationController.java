package com.wanderbee.destinationservice.destination.controller;

import com.wanderbee.destinationservice.destination.dto.GeoDbResponse;
import com.wanderbee.destinationservice.destination.dto.SaveDestinationRequest;
import com.wanderbee.destinationservice.destination.dto.SavedDestinationResponse;
import com.wanderbee.destinationservice.media.dto.PexelsPhotoResponse;
import com.wanderbee.destinationservice.media.dto.PexelsVideoResponse;
import com.wanderbee.destinationservice.destination.service.DestinationService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;


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

    @GetMapping("/static/{name}")
    public ResponseEntity<List<Map<String, Object>>> getStaticDestinationsByName(@PathVariable String name) {
        // Map shorthand names to actual JSON file names
        String fileName = switch (name.toLowerCase()) {
            case "india" -> "indian_destinations";
            case "all" -> "destinations";
            default -> name;
        };
        try {
            return ResponseEntity.ok(destinationService.getStaticDestinations(fileName));
        } catch (IOException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/save")
    public ResponseEntity<?> saveDestination(
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody SaveDestinationRequest request
    ) {
        try {
            SavedDestinationResponse response = destinationService.saveDestination(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/saved/{userId}")
    public ResponseEntity<List<SavedDestinationResponse>> getSavedDestinations(
            @PathVariable String userId,
            @RequestHeader("X-User-Id") String authenticatedUserId
    ) {
        if (!userId.equals(authenticatedUserId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(destinationService.getSavedDestinations(userId));
    }

}
