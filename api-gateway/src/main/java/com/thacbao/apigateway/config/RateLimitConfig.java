package com.thacbao.apigateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

/**
 * Rate limiting configuration for API Gateway.
 * Uses Redis-based RequestRateLimiter with IP-based key resolution.
 *
 * Rate limit rules are configured per-route in application.yml using:
 *   filters:
 *     - name: RequestRateLimiter
 *       args:
 *         redis-rate-limiter.replenishRate: 5
 *         redis-rate-limiter.burstCapacity: 10
 */
@Configuration
public class RateLimitConfig {

    /**
     * Resolves rate limit key by client IP address.
     * Uses X-Forwarded-For header if present (behind proxy), otherwise remote address.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
            if (ip != null && !ip.isEmpty()) {
                ip = ip.split(",")[0].trim();
            } else {
                ip = exchange.getRequest().getRemoteAddress() != null
                        ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                        : "unknown";
            }
            return Mono.just(ip);
        };
    }
}
