package com.thacbao.common.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AppExceptionTest {

    @Test
    void constructor_withErrorCode_usesDefaultMessage() {
        AppException ex = new AppException(ErrorCode.NOT_FOUND);

        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
        assertEquals("Resource not found", ex.getMessage());
    }

    @Test
    void constructor_withCustomMessage() {
        AppException ex = new AppException(ErrorCode.NOT_FOUND, "User not found");

        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
        assertEquals("User not found", ex.getMessage());
    }

    @Test
    void constructor_withCause() {
        RuntimeException cause = new RuntimeException("root cause");
        AppException ex = new AppException(ErrorCode.UNCATEGORIZED, "Error", cause);

        assertEquals(ErrorCode.UNCATEGORIZED, ex.getErrorCode());
        assertEquals("Error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void notFoundException() {
        NotFoundException ex = new NotFoundException("Item not found");
        assertEquals(ErrorCode.NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void alreadyException() {
        AlreadyException ex = new AlreadyException("Already exists");
        assertEquals(ErrorCode.ALREADY_EXISTS, ex.getErrorCode());
    }

    @Test
    void invalidException() {
        InvalidException ex = new InvalidException("Invalid");
        assertEquals(ErrorCode.INVALID_INPUT, ex.getErrorCode());
    }

    @Test
    void permissionException() {
        PermissionException ex = new PermissionException("Denied");
        assertEquals(ErrorCode.PERMISSION_DENIED, ex.getErrorCode());
    }

    @Test
    void otpExpiredException() {
        OtpExpiredException ex = new OtpExpiredException("Expired");
        assertEquals(ErrorCode.OTP_EXPIRED, ex.getErrorCode());
    }

    @Test
    void emailSenderException() {
        EmailSenderException ex = new EmailSenderException("Failed");
        assertEquals(ErrorCode.EMAIL_SEND_FAILED, ex.getErrorCode());
    }
}
