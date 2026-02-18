package com.wanderbee.destinationservice.itinerary.service;

import com.wanderbee.destinationservice.itinerary.dto.ItineraryResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

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
                .user(u -> u.text("""
                Create a detailed {days}-day travel itinerary for {city}.
                
                CONTEXT:
                - Budget Range: {budget}
                - Number of Travellers: {travellers}
                - User Interests: {interests}
                
                REQUIREMENTS:
                1. Plan activities that fit within the {budget} budget for {travellers} people.
                2. For EACH day, provide an 'estimatedDailyCost' (e.g., "$150").
                3. Provide a 'totalEstimatedTripCost' for the entire journey.
                4. Include specific times for activities and 'locationHint'.
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
