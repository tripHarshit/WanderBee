package com.wanderbee.destinationservice.destination.dto;

import java.util.List;

public record GeoDbResponse(
        List<City> data
){}
