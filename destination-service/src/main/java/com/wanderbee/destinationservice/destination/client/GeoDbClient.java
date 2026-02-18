package com.wanderbee.destinationservice.destination.client;

import com.wanderbee.destinationservice.destination.dto.GeoDbResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "geodb-api", url = "https://wft-geo-db.p.rapidapi.com")
public interface GeoDbClient {

    @GetMapping("/v1/geo/cities")
    GeoDbResponse getPopularCities(
            @RequestParam("limit") int limit,
            @RequestParam("offset") int offset
    );

    @GetMapping("/v1/geo/locations/{latLong}/nearbyCities")
    GeoDbResponse getNearbyCities(
            @PathVariable String latLong,
            @RequestParam("limit") int limit,
            @RequestHeader("X-RapidAPI-Key") String apiKey,
            @RequestHeader("X-RapidAPI-Host") String host
    );

    @GetMapping("/v1/geo/cities")
    GeoDbResponse searchCities(
            @RequestParam("namePrefix") String namePrefix,
            @RequestParam("limit") int limit,
            @RequestParam("offset") int offset,
            @RequestParam("sort") String sort
    );
}