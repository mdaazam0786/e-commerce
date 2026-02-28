package com.example.ecommerce.common.exception;

public class InvalidArgumentException extends ECommercePlatformException {
    private static final long serialVersionUID = 1L;
    
    public InvalidArgumentException(String message) {
        super(message);
    }
    
    public InvalidArgumentException(String message, Throwable cause) {
        super(message, cause);
    }
}
