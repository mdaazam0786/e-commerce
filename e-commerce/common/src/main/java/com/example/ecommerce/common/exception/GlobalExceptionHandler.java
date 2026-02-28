package com.example.ecommerce.common.exception;

import com.example.ecommerce.common.dto.UIBean;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ECommercePlatformException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public UIBean<String> handleECommercePlatformException(ECommercePlatformException e) {
        logger.error("ECommercePlatformException: {}", ExceptionUtils.getStackTrace(e));
        UIBean<String> response = new UIBean<>();
        response.setMessage(e.getMessage());
        response.setSuccess(false);
        response.setResponse("ERROR");
        return response;
    }
    
    @ExceptionHandler(InvalidArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public UIBean<String> handleInvalidArgumentException(InvalidArgumentException e) {
        logger.error("InvalidArgumentException: {}", e.getMessage());
        UIBean<String> response = new UIBean<>();
        response.setMessage(e.getMessage());
        response.setSuccess(false);
        response.setResponse("ERROR");
        return response;
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public UIBean<String> handleResourceNotFoundException(ResourceNotFoundException e) {
        logger.error("ResourceNotFoundException: {}", e.getMessage());
        UIBean<String> response = new UIBean<>();
        response.setMessage(e.getMessage());
        response.setSuccess(false);
        response.setResponse("ERROR");
        return response;
    }
    
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public UIBean<String> handleGenericException(Exception e) {
        logger.error("Unexpected error occurred: {}", ExceptionUtils.getStackTrace(e));
        UIBean<String> response = new UIBean<>();
        response.setMessage("An unexpected error occurred: " + e.getMessage());
        response.setSuccess(false);
        response.setResponse("ERROR");
        return response;
    }
}
