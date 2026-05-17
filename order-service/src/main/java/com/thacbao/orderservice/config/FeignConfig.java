package com.thacbao.orderservice.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                // Forward JWT and user headers to downstream services
                String authorization = attributes.getRequest().getHeader("Authorization");
                if (authorization != null) {
                    requestTemplate.header("Authorization", authorization);
                }
                String userId = attributes.getRequest().getHeader("X-User-Id");
                if (userId != null) {
                    requestTemplate.header("X-User-Id", userId);
                }
                String userEmail = attributes.getRequest().getHeader("X-User-Email");
                if (userEmail != null) {
                    requestTemplate.header("X-User-Email", userEmail);
                }
                String userRoles = attributes.getRequest().getHeader("X-User-Roles");
                if (userRoles != null) {
                    requestTemplate.header("X-User-Roles", userRoles);
                }
            }
        };
    }
}
