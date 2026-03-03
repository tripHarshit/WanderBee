package com.wanderbee.destinationservice.media.service;

import com.wanderbee.destinationservice.destination.client.GeoDbClient;
import com.wanderbee.destinationservice.media.client.PexelsClient;
import com.wanderbee.destinationservice.insights.dto.CityInsights;
import com.wanderbee.destinationservice.destination.dto.GeoDbResponse;
import com.wanderbee.destinationservice.media.dto.PexelsPhotoResponse;
import com.wanderbee.destinationservice.media.dto.PexelsVideoResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PexelsService {

    private final PexelsClient pexelsClient;

    public PexelsService(PexelsClient pexelsClient){this.pexelsClient = pexelsClient;}

    @Value("${pexels.api.key}")
    private String pexelsApiKey;

    public PexelsPhotoResponse  getPexelsPhotos(String query,int perPage){
        return pexelsClient.searchPhotos(pexelsApiKey,query,perPage);
    }

    public PexelsVideoResponse getPexelsVideos(String query,int perPage){
        return pexelsClient.searchVideos(pexelsApiKey,query,perPage);
    }

}


