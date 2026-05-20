package com.thacbao.common.exception;

import com.thacbao.common.dto.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        webRequest = mock(WebRequest.class);
        when(webRequest.getDescription(false)).thenReturn("uri=/test");
    }

    @Test
    void handleAppException_notFound() {
        AppException ex = new NotFoundException("User not found");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex, webRequest);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(404, response.getBody().getCode());
        assertEquals("error", response.getBody().getStatus());
        assertEquals("User not found", response.getBody().getMessage());
    }

    @Test
    void handleAppException_alreadyExists() {
        AppException ex = new AlreadyException("Email exists");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex, webRequest);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(409, response.getBody().getCode());
    }

    @Test
    void handleAppException_invalidInput() {
        AppException ex = new InvalidException("Invalid data");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleAppException_permissionDenied() {
        AppException ex = new PermissionException("Access denied");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex, webRequest);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void handleAppException_otpExpired() {
        AppException ex = new OtpExpiredException("OTP expired");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex, webRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void handleAppException_emailSendFailed() {
        AppException ex = new EmailSenderException("Send failed");

        ResponseEntity<ApiResponse<Void>> response = handler.handleAppException(ex, webRequest);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
    }

    @Test
    void handleValidationException() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "email", "must not be blank");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidationException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertNotNull(response.getBody().getErrors());
    }

    @Test
    void handleSQLException() {
        SQLException ex = new SQLException("DB error");

        ResponseEntity<ApiResponse<Void>> response = handler.handleSQLException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Database error occurred", response.getBody().getMessage());
    }

    @Test
    void handleGlobalException() {
        Exception ex = new RuntimeException("Unexpected");

        ResponseEntity<ApiResponse<Void>> response = handler.handleGlobalException(ex, webRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}
