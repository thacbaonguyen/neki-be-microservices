package com.thacbao.common.exception;

public class InvalidException extends AppException {
    public InvalidException(String message) {
        super(ErrorCode.INVALID_INPUT, message);
    }

    public InvalidException(ErrorCode errorCode) {
        super(errorCode);
    }
}
