package com.thacbao.common.exception;

public class AlreadyException extends AppException {
    public AlreadyException(String message) {
        super(ErrorCode.ALREADY_EXISTS, message);
    }

    public AlreadyException(ErrorCode errorCode) {
        super(errorCode);
    }
}
