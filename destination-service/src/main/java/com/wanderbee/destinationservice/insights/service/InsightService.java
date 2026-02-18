package com.wanderbee.destinationservice.insights.service;


import com.wanderbee.destinationservice.insights.dto.CityInsights;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class InsightService {

    private final ChatClient chatClient;
    public InsightService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    @Cacheable(value = "cityInsights", key = "#cityName")
    public CityInsights getCityInsights(String cityName) {
        return chatClient.prompt()
                .user(u -> u.text("""
                Provide tourist insights for {city}.
                - Description: A short paragraph (max 50 words) on highlights. It should be informative for tourist.
                - Cultural Tips: 3 brief tips for visitors.
                - Best Time to Visit: Recommended months.
                - Language: Primary language spoken.
                - Currency: Local currency used.
                """)
                        .param("city", cityName))
                .call()
                .entity(CityInsights.class);
    }
}

