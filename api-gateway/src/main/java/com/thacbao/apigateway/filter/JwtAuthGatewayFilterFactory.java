package com.thacbao.apigateway.filter;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * JWT Authentication Filter for Spring Cloud Gateway.
 * Validates JWT tokens and forwards user info as headers to downstream services.
 *
 * Usage in application.yml:
 *   filters:
 *     - JwtAuth
 */
@Component
@Slf4j
public class JwtAuthGatewayFilterFactory extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip JWT validation for public paths
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Check Authorization header
            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Missing or invalid Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);

            // Validate token
            if (!jwtUtil.isTokenValid(token)) {
                return onError(exchange, "Invalid or expired JWT token", HttpStatus.UNAUTHORIZED);
            }

            // Extract claims and forward to downstream services
            try {
                Claims claims = jwtUtil.extractAllClaims(token);
                String email = claims.getSubject();
                Integer userId = claims.get("userId", Integer.class);
                @SuppressWarnings("unchecked")
                List<String> roles = claims.get("roles", List.class);

                // Add user info headers for downstream services
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header("X-User-Id", userId != null ? userId.toString() : "")
                        .header("X-User-Email", email != null ? email : "")
                        .header("X-User-Roles", roles != null ? String.join(",", roles) : "")
                        .build();

                // Check admin access
                if (path.startsWith("/api/v1/admin/") && (roles == null ||
                        (!roles.contains("ADMIN") && !roles.contains("ROLE_ADMIN")))) {
                    return onError(exchange, "Access denied: Admin role required", HttpStatus.FORBIDDEN);
                }

                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("JWT processing error: {}", e.getMessage());
                return onError(exchange, "JWT processing error", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/v1/auth/")
                || path.startsWith("/api/v1/products")
                || path.startsWith("/api/v1/categories")
                || path.startsWith("/api/v1/search")
                || path.startsWith("/api/v1/catalog")
                || path.startsWith("/payment/")
                || path.equals("/api/v1/order/tracking")
                || path.equals("/api/v1/review/all-review")
                || path.equals("/api/v1/banner")
                || path.equals("/api/v1/settings")
                || path.startsWith("/actuator/")
                || path.startsWith("/oauth2/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }

    private Mono<Void> onError(ServerWebExchange exchange, String message, HttpStatus status) {
        log.warn("Gateway auth error: {} - Path: {}", message, exchange.getRequest().getURI().getPath());
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().add("Content-Type", "application/json");
        byte[] bytes = ("{\"code\":" + status.value() + ",\"status\":\"error\",\"message\":\"" + message + "\"}").getBytes();
        return response.writeWith(Mono.just(response.bufferFactory().wrap(bytes)));
    }

    public static class Config {
        // Configuration properties can be added here if needed
    }
}
