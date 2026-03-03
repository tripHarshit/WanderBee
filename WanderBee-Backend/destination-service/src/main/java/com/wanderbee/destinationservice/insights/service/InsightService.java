package com.wanderbee.destinationservice.insights.service;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.wanderbee.destinationservice.insights.dto.CityInsights;

@Service
public class InsightService {

    private final ChatClient chatClient;

    public InsightService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Cacheable(value = "cityInsights", key = "#cityName")
    public CityInsights getCityInsights(String cityName) {
        return chatClient.prompt()
                .system("""
                You are a travel information assistant.
                You MUST respond with ONLY a valid JSON object — no markdown, no code fences, no extra text.
                The JSON must contain exactly these fields:
                  "description"      : string (max 50 words on tourist highlights)
                  "culturalTips"     : array of 3 short strings
                  "bestTimeToVisit"  : string (recommended months)
                  "language"         : string (primary language)
                  "currency"         : string (local currency)
                """)
                .user(u -> u.text("""
                Provide tourist insights for the city: {city}.
                Return ONLY the JSON object with the fields described in the system prompt.
                """)
                        .param("city", cityName))
                .call()
                .entity(CityInsights.class);
    }
}

