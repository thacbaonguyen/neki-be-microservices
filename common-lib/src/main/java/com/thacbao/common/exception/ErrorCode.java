package com.thacbao.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common errors
    NOT_FOUND(404, "Resource not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(404, "Không tìm thấy sản phẩm", HttpStatus.NOT_FOUND),
    ALREADY_EXISTS(409, "Resource already exists", HttpStatus.CONFLICT),
    INVALID_INPUT(400, "Invalid input", HttpStatus.BAD_REQUEST),
    UNCATEGORIZED(500, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    OTP_EXPIRED(400, "OTP expired", HttpStatus.BAD_REQUEST),

    // User errors
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS(409, "User already exists", HttpStatus.CONFLICT),
    PERMISSION_DENIED(403, "Permission denied", HttpStatus.FORBIDDEN),
    ACCOUNT_BLOCKED(403, "Your account has been blocked", HttpStatus.FORBIDDEN),

    // Email errors
    EMAIL_SEND_FAILED(503, "Failed to send email", HttpStatus.SERVICE_UNAVAILABLE),

    // Auth errors
    INVALID_TOKEN(401, "Invalid token", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(401, "Token expired", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(401, "Unauthorized", HttpStatus.UNAUTHORIZED),

    // Database errors
    SQL_ERROR(500, "Database error occurred", HttpStatus.INTERNAL_SERVER_ERROR),

    // Service errors
    SERVICE_UNAVAILABLE(503, "Service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    INTERNAL_SERVER_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
