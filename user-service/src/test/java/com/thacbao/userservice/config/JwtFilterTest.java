package com.thacbao.userservice.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

    @Mock private CustomUserDetailsService userDetailsService;
    @Mock private JwtUtils jwtUtils;
    @Mock private HttpServletRequest request;
    @Mock private HttpServletResponse response;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_publicPath_skips() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");
        when(request.getServletPath()).thenReturn("/api/v1/auth/login");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_noAuthHeader_skips() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidBearerPrefix_skips() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Basic abc");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_invalidToken_returns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer bad-token");
        when(jwtUtils.getUsernameFromToken("bad-token")).thenThrow(new RuntimeException("Invalid"));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_setsAuthentication() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.getUsernameFromToken("valid-token")).thenReturn("test@test.com");

        UserDetails userDetails = new User("test@test.com", "enc", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
        when(jwtUtils.validateToken("valid-token", userDetails)).thenReturn(true);

        jwtFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_validToken_invalidValidation_returns401() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.getUsernameFromToken("valid-token")).thenReturn("test@test.com");

        UserDetails userDetails = new User("test@test.com", "enc", Collections.emptyList());
        when(userDetailsService.loadUserByUsername("test@test.com")).thenReturn(userDetails);
        when(jwtUtils.validateToken("valid-token", userDetails)).thenReturn(false);

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @Test
    void doFilterInternal_alreadyAuthenticated_skips() throws Exception {
        when(request.getRequestURI()).thenReturn("/api/v1/orders");
        when(request.getServletPath()).thenReturn("/api/v1/orders");
        when(request.getHeader("Authorization")).thenReturn("Bearer valid-token");
        when(jwtUtils.getUsernameFromToken("valid-token")).thenReturn("test@test.com");

        SecurityContextHolder.getContext().setAuthentication(
                new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                        "test@test.com", null, Collections.emptyList()));

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void doFilterInternal_internalPath_skips() throws Exception {
        when(request.getRequestURI()).thenReturn("/internal/users/1");
        when(request.getServletPath()).thenReturn("/internal/users/1");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_actuatorPath_skips() throws Exception {
        when(request.getRequestURI()).thenReturn("/actuator/health");
        when(request.getServletPath()).thenReturn("/actuator/health");

        jwtFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }
}
