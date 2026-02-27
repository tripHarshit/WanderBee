package com.wanderbee.apigateway.filter;

import com.wanderbee.apigateway.config.RouteValidator;
import com.wanderbee.apigateway.util.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;
    private final RouteValidator routeValidator;

    public AuthenticationFilter(JwtUtil jwtUtil, RouteValidator routeValidator) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.routeValidator = routeValidator;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Check if the route requires authentication
            if (!routeValidator.isSecured.test(request)) {
                // Open endpoint - allow without authentication
                return chain.filter(exchange);
            }

            // Secured endpoint - validate JWT token
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // Extract Token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                try {
                    // Validate Token
                    jwtUtil.validateToken(token);

                    // Extract userId (email) from token and forward to downstream services
                    String userId = jwtUtil.extractUserId(token);
                    exchange = exchange.mutate()
                            .request(request.mutate()
                                    .header("X-User-Id", userId)
                                    .build())
                            .build();
                } catch (Exception e) {
                    return onError(exchange, "Invalid or Expired Token: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
                }
            } else {
                return onError(exchange, "Invalid Authorization Header Format", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        exchange.getResponse().setStatusCode(httpStatus);
        exchange.getResponse().getHeaders().set(HttpHeaders.CONTENT_TYPE, "application/json");
        return exchange.getResponse().writeWith(
                Mono.just(exchange.getResponse()
                        .bufferFactory()
                        .wrap(String.format("{\"error\": \"%s\"}", err).getBytes()))
        );
    }

    public static class Config { }
}