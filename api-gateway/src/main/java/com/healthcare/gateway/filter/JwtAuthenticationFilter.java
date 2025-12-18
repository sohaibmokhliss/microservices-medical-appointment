package com.healthcare.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String secret;

    // Public endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/login",
            "/api/auth/welcome",
            "/api/docteurs",
            "/api/rdv",
            "/api/billing",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Check if the path is public
        Predicate<ServerHttpRequest> isPublic = r -> PUBLIC_ENDPOINTS.stream()
                .anyMatch(uri -> r.getURI().getPath().contains(uri));

        boolean isPublicEndpoint = isPublic.test(request);

        // Check if Authorization header is present
        if (!request.getHeaders().containsKey("Authorization")) {
            // If it's a public endpoint, allow through without authentication
            if (isPublicEndpoint) {
                return chain.filter(exchange);
            }
            return this.onError(exchange, "No authorization header", HttpStatus.UNAUTHORIZED);
        }

        String authHeader = request.getHeaders().getOrEmpty("Authorization").get(0);

        if (!authHeader.startsWith("Bearer ")) {
            // If it's a public endpoint, allow through even with invalid auth format
            if (isPublicEndpoint) {
                return chain.filter(exchange);
            }
            return this.onError(exchange, "Invalid authorization header format", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        try {
            // Validate token and extract claims
            Claims claims = validateToken(token);

            if (claims == null) {
                // If it's a public endpoint, allow through even with invalid token
                if (isPublicEndpoint) {
                    return chain.filter(exchange);
                }
                return this.onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // Extract username and role from token
            String username = claims.getSubject();
            String role = claims.get("role", String.class);

            // Add username and role to request headers for downstream services
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                    .header("X-User-Username", username)
                    .header("X-User-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception e) {
            // If it's a public endpoint, allow through even if token validation fails
            if (isPublicEndpoint) {
                return chain.filter(exchange);
            }
            return this.onError(exchange, "Token validation failed: " + e.getMessage(), HttpStatus.UNAUTHORIZED);
        }
    }

    private Claims validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters
    }
}
