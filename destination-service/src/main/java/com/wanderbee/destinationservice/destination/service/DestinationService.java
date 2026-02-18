package com.wanderbee.destinationservice.destination.service;

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
public class DestinationService {

    private final GeoDbClient geoDbClient;

    public DestinationService(GeoDbClient geoDbClient){this.geoDbClient = geoDbClient;}

    @Value("${geodb.api.key}")
    private String geoDbApiKey;

    @Cacheable(value = "popularCities",key = "#limit")
    public GeoDbResponse getPopularCities(int limit) {
        int offset = (int) (Math.random() * 500);
        return geoDbClient.getPopularCities(limit, offset);
    }

    @Cacheable(value = "nearbyCities", key = "#latLon + #limit")
    public GeoDbResponse getNearbyCities(String latLon, int limit) {
        return geoDbClient.getNearbyCities(latLon, limit, geoDbApiKey,"-population");
    }

    public GeoDbResponse searchCities(String namePrefix,int limit,int offset){
        return geoDbClient.searchCities(namePrefix,limit,offset,"-population");
    }

}

