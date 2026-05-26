package com.thacbao.common.exception;

public class OtpExpiredException extends AppException {
    public OtpExpiredException(String message) {
        super(ErrorCode.OTP_EXPIRED, message);
    }

    public OtpExpiredException(ErrorCode errorCode) {
        super(errorCode);
    }
}
