package com.wanderbee.apigateway.config;

import java.util.List;
import java.util.function.Predicate;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

@Component
public class RouteValidator {

    public static final List<String> openApiEndpoints = List.of(
            // Identity service – pre-auth endpoints
            "/auth/register",
            "/auth/login",
            "/auth/token",
            "/auth/validate",
            "/auth/google-login",
            // Destination service – public browsing endpoints
            "/api/v1/destinations/popular",
            "/api/v1/destinations/nearby",
            "/api/v1/destinations/search",
            "/api/v1/destinations/static",
            "/api/v1/weather",
            "/api/v1/media",
            "/api/v1/description",
            "/api/v1/itinerary",
            // Infrastructure
            "/eureka",
            "/ws"
    );

    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));
}
