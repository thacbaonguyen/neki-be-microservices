package com.thacbao.productservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class InternalTokenInterceptor implements HandlerInterceptor {

    private static final String INTERNAL_TOKEN_HEADER = "X-Internal-Token";

    @Value("${app.internal.token}")
    private String internalToken;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (internalToken == null || internalToken.isBlank()) {
            response.setStatus(HttpStatus.SERVICE_UNAVAILABLE.value());
            return false;
        }

        String providedToken = request.getHeader(INTERNAL_TOKEN_HEADER);
        if (providedToken == null || providedToken.isBlank()) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        if (!internalToken.equals(providedToken)) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }

        return true;
    }
}
