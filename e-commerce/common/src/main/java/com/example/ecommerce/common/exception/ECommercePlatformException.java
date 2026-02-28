package com.example.ecommerce.common.exception;

public class ECommercePlatformException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    
    public ECommercePlatformException(String message) {
        super(message);
    }
    
    public ECommercePlatformException(String message, Throwable cause) {
        super(message, cause);
    }
}
