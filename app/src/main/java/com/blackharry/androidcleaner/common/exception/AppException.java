package com.blackharry.androidcleaner.common.exception;

import androidx.annotation.NonNull;

public class AppException extends RuntimeException {
    private final ErrorCode errorCode;
    
    public AppException(@NonNull ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
    
    public AppException(@NonNull ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public AppException(@NonNull ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }
    
    public AppException(@NonNull ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
} 