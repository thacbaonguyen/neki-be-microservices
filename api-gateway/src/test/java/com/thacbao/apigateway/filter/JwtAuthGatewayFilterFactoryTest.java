package com.thacbao.apigateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthGatewayFilterFactoryTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private GatewayFilterChain chain;

    private JwtAuthGatewayFilterFactory factory;
    private GatewayFilter filter;

    @BeforeEach
    void setUp() {
        factory = new JwtAuthGatewayFilterFactory(jwtUtil);
        filter = factory.apply(new JwtAuthGatewayFilterFactory.Config());
    }

    @Test
    void publicPath_products_noAuthRequired() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/products").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    void publicPath_auth_noAuthRequired() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/auth/login").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
        verify(chain).filter(exchange);
    }

    @Test
    void publicPath_actuator_noAuthRequired() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();
    }

    @Test
    void missingAuthorizationHeader_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/orders").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void invalidBearerPrefix_returns401() {
        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Basic abc123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void invalidToken_returns401() {
        when(jwtUtil.isTokenValid("bad-token")).thenReturn(false);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer bad-token")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void validToken_forwardsUserHeaders() {
        String token = "valid-token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("userId", Integer.class)).thenReturn(1);
        when(claims.get("roles", List.class)).thenReturn(List.of("USER"));
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void adminPath_withoutAdminRole_returns403() {
        String token = "valid-token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("userId", Integer.class)).thenReturn(1);
        when(claims.get("roles", List.class)).thenReturn(List.of("USER"));
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.FORBIDDEN;
    }

    @Test
    void adminPath_withAdminRole_passes() {
        String token = "valid-token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("admin@test.com");
        when(claims.get("userId", Integer.class)).thenReturn(1);
        when(claims.get("roles", List.class)).thenReturn(List.of("ADMIN"));
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/admin/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        when(chain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        verify(chain).filter(any());
    }

    @Test
    void jwtProcessingException_returns401() {
        String token = "valid-token";
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenThrow(new RuntimeException("parse error"));

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.UNAUTHORIZED;
    }

    @Test
    void adminPath_withNullRoles_returns403() {
        String token = "valid-token";
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("user@test.com");
        when(claims.get("userId", Integer.class)).thenReturn(1);
        when(claims.get("roles", List.class)).thenReturn(null);
        when(jwtUtil.isTokenValid(token)).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        MockServerHttpRequest request = MockServerHttpRequest
                .get("/api/v1/admin/dashboard")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(filter.filter(exchange, chain))
                .verifyComplete();

        assert exchange.getResponse().getStatusCode() == HttpStatus.FORBIDDEN;
    }
}
