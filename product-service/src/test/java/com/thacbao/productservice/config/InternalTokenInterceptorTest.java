package com.thacbao.productservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class InternalTokenInterceptorTest {

    private InternalTokenInterceptor interceptor;
    private HttpServletRequest request;
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        interceptor = new InternalTokenInterceptor();
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        ReflectionTestUtils.setField(interceptor, "internalToken", "secret-token");
    }

    @Test
    void preHandle_missingToken_returns401() {
        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(401);
    }

    @Test
    void preHandle_invalidToken_returns403() {
        when(request.getHeader("X-Internal-Token")).thenReturn("wrong-token");

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(403);
    }

    @Test
    void preHandle_blankConfiguredToken_returns503() {
        ReflectionTestUtils.setField(interceptor, "internalToken", "");

        assertFalse(interceptor.preHandle(request, response, new Object()));

        verify(response).setStatus(503);
    }

    @Test
    void preHandle_validToken_allowsRequest() {
        when(request.getHeader("X-Internal-Token")).thenReturn("secret-token");

        assertTrue(interceptor.preHandle(request, response, new Object()));
    }
}
