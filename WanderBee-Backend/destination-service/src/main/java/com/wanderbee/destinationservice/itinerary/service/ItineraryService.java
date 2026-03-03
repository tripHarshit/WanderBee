package com.wanderbee.destinationservice.itinerary.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.wanderbee.destinationservice.itinerary.dto.ItineraryResponse;

@Service
public class ItineraryService {

    private final ChatClient chatClient;

    public ItineraryService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Cacheable(value = "itineraries", key = "#city + #days + #budget + #travellers + #interests")
    public ItineraryResponse generateItinerary(
            String city, int days, String budget, int travellers, String interests) {

        return chatClient.prompt()
                .system("""
                You are an expert travel itinerary planner.
                You MUST respond with ONLY a valid JSON object — no markdown, no code fences, no extra text.
                The JSON must have exactly these top-level fields:
                  "tripTitle"             : string
                  "destination"           : string
                  "duration"              : string  (e.g. "5 Days")
                  "budgetRange"           : string  (e.g. "Budget" / "Mid-range" / "Luxury")
                  "numberOfTravellers"    : integer
                  "totalEstimatedTripCost" : string (e.g. "$750")
                  "days"                  : array of day objects, each containing:
                      "dayNumber"         : integer
                      "theme"             : string
                      "estimatedDailyCost" : string (e.g. "$150")
                      "activities"        : array of objects, each with:
                          "time"          : string (e.g. "09:00 AM")
                          "title"         : string
                          "description"   : string
                          "locationHint"  : string
                """)
                .user(u -> u.text("""
                Create a detailed {days}-day travel itinerary for {city}.

                CONTEXT:
                - Budget Range: {budget}
                - Number of Travellers: {travellers}
                - User Interests: {interests}

                REQUIREMENTS:
                1. Plan activities that fit within the {budget} budget for {travellers} people.
                2. For EACH day, provide an estimatedDailyCost.
                3. Provide a totalEstimatedTripCost for the entire journey.
                4. Include specific times for activities and a locationHint for each activity.

                Return ONLY the JSON object as described in the system prompt.
                """)
                        .param("days", String.valueOf(days))
                        .param("city", city)
                        .param("budget", budget)
                        .param("travellers", String.valueOf(travellers))
                        .param("interests", interests))
                .call()
                .entity(ItineraryResponse.class);
    }
}
