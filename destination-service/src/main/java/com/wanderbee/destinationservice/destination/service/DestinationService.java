package com.wanderbee.destinationservice.destination.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wanderbee.destinationservice.destination.client.GeoDbClient;
import com.wanderbee.destinationservice.destination.dto.GeoDbResponse;
import com.wanderbee.destinationservice.destination.dto.SaveDestinationRequest;
import com.wanderbee.destinationservice.destination.dto.SavedDestinationResponse;
import com.wanderbee.destinationservice.destination.entity.SavedDestination;
import com.wanderbee.destinationservice.destination.repository.SavedDestinationRepository;

@Service
public class DestinationService {

    private final GeoDbClient geoDbClient;
    private final ObjectMapper objectMapper;
    private final SavedDestinationRepository savedDestinationRepository;

    public DestinationService(GeoDbClient geoDbClient, ObjectMapper objectMapper,
                              SavedDestinationRepository savedDestinationRepository) {
        this.geoDbClient = geoDbClient;
        this.objectMapper = objectMapper;
        this.savedDestinationRepository = savedDestinationRepository;
    }

    @Value("${geodb.api.key}")
    private String geoDbApiKey;

    private static final String GEODB_HOST = "wft-geo-db.p.rapidapi.com";

    @Cacheable(value = "popularCities",key = "#limit")
    public GeoDbResponse getPopularCities(int limit) {
        int offset = (int) (Math.random() * 500);
        return geoDbClient.getPopularCities(limit, offset, geoDbApiKey, GEODB_HOST);
    }

    @Cacheable(value = "nearbyCities", key = "#latLon + #limit")
    public GeoDbResponse getNearbyCities(String latLon, int limit) {
        // Convert "28.6139,77.2090" → "+28.6139+077.2090" (GeoDb format)
        String formatted = latLon;
        if (latLon.contains(",")) {
            String[] parts = latLon.split(",");
            formatted = String.format("%+f%+f", Double.parseDouble(parts[0].trim()), Double.parseDouble(parts[1].trim()));
        }
        return geoDbClient.getNearbyCities(formatted, limit, "-population", geoDbApiKey, GEODB_HOST);
    }

    public GeoDbResponse searchCities(String namePrefix,int limit,int offset){
        return geoDbClient.searchCities(namePrefix, limit, offset, "-population", geoDbApiKey, GEODB_HOST);
    }

    @Cacheable(value = "staticDestinations", key = "#fileName")
    public List<Map<String, Object>> getStaticDestinations(String fileName) throws IOException {
        ClassPathResource resource = new ClassPathResource("static/" + fileName + ".json");
        return objectMapper.readValue(
            resource.getInputStream(),
            objectMapper.getTypeFactory().constructCollectionType(List.class, Map.class)
        );
    }

    public SavedDestinationResponse saveDestination(String userId, SaveDestinationRequest request) {
        if (savedDestinationRepository.existsByUserIdAndCityId(userId, request.cityId())) {
            throw new IllegalArgumentException("Destination already saved");
        }

        SavedDestination entity = new SavedDestination();
        entity.setUserId(userId);
        entity.setCityId(request.cityId());
        entity.setCityName(request.cityName());

        SavedDestination saved = savedDestinationRepository.save(entity);

        return new SavedDestinationResponse(
                saved.getId(),
                saved.getUserId(),
                saved.getCityId(),
                saved.getCityName(),
                saved.getTimestamp()
        );
    }

    public List<SavedDestinationResponse> getSavedDestinations(String userId) {
        return savedDestinationRepository.findByUserIdOrderByTimestampDesc(userId)
                .stream()
                .map(d -> new SavedDestinationResponse(
                        d.getId(),
                        d.getUserId(),
                        d.getCityId(),
                        d.getCityName(),
                        d.getTimestamp()
                ))
                .toList();
    }

}

