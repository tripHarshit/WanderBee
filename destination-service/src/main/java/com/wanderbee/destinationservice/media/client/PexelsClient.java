package com.wanderbee.destinationservice.media.client;


import com.wanderbee.destinationservice.media.dto.PexelsPhotoResponse;
import com.wanderbee.destinationservice.media.dto.PexelsVideoResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pexels-api", url = "https://api.pexels.com")
public interface PexelsClient {

    @GetMapping("/v1/search")
    PexelsPhotoResponse searchPhotos(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("query") String query,
            @RequestParam("per_page") int perPage
    );

    @GetMapping("/videos/search")
    PexelsVideoResponse searchVideos(
            @RequestHeader("Authorization") String apiKey,
            @RequestParam("query") String query,
            @RequestParam("per_page") int perPage
    );
}
