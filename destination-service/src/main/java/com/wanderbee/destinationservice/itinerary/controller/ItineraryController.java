package com.wanderbee.destinationservice.itinerary.controller;

import com.wanderbee.destinationservice.itinerary.dto.ItineraryResponse;
import com.wanderbee.destinationservice.itinerary.service.ItineraryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/itinerary")
public class ItineraryController {

    private final ItineraryService itineraryService;

    public ItineraryController(ItineraryService itineraryService) {
        this.itineraryService = itineraryService;
    }

    @GetMapping("/generate")
    public ResponseEntity<ItineraryResponse> getItinerary(
            @RequestParam String city,
            @RequestParam int days,
            @RequestParam String budget,
            @RequestParam int travellers,
            @RequestParam(defaultValue = "general sightseeing") String interests) {

        return ResponseEntity.ok(itineraryService.generateItinerary(city, days, budget, travellers, interests));
    }
}
