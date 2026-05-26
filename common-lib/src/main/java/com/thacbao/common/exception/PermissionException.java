package com.thacbao.common.exception;

public class PermissionException extends AppException {
    public PermissionException(String message) {
        super(ErrorCode.PERMISSION_DENIED, message);
    }

    public PermissionException(ErrorCode errorCode) {
        super(errorCode);
    }
}
