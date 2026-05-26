package com.thacbao.common.exception;

public class EmailSenderException extends AppException {
    public EmailSenderException(String message) {
        super(ErrorCode.EMAIL_SEND_FAILED, message);
    }

    public EmailSenderException(Throwable cause) {
        super(ErrorCode.EMAIL_SEND_FAILED, ErrorCode.EMAIL_SEND_FAILED.getMessage(), cause);
    }
}
