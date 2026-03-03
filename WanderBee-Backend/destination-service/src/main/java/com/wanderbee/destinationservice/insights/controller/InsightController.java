package com.wanderbee.destinationservice.insights.controller;

import com.wanderbee.destinationservice.insights.service.InsightService;
import com.wanderbee.destinationservice.insights.dto.CityInsights;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/description")
public class InsightController {

    private final InsightService insightService;

    public InsightController(InsightService insightService) {this.insightService = insightService;}

    @GetMapping("/{cityName}/insights")
    public ResponseEntity<CityInsights> getInsights(@PathVariable String cityName) {
        CityInsights insights = insightService.getCityInsights(cityName);
        return ResponseEntity.ok(insights);
    }
}

