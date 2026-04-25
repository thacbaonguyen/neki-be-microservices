package com.thacbao.common.exception;

import com.thacbao.common.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Void>> handleAppException(
            AppException ex, WebRequest request) {
        log.error("AppException: {} - Path: {}", ex.getMessage(), request.getDescription(false), ex);

        ErrorCode errorCode = ex.getErrorCode();
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(errorCode.getCode())
                .status("error")
                .message(ex.getMessage())
                .build();

        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        log.warn("Validation failed: {}", errors);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.BAD_REQUEST.value())
                .status("error")
                .message("Validation failed")
                .errors(errors)
                .build();

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<ApiResponse<Void>> handleSQLException(
            SQLException ex, WebRequest request) {
        log.error("SQL Error: {} - Path: {}", ex.getMessage(), request.getDescription(false), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status("error")
                .message("Database error occurred")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGlobalException(
            Exception ex, WebRequest request) {
        log.error("Unexpected error: {} - Path: {}", ex.getMessage(), request.getDescription(false), ex);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .code(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .status("error")
                .message("An unexpected error occurred")
                .build();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
