package com.system.design.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Custom Business Exception for handling business logic errors
 * 
 * @author Shailender Kumar
 */
@Getter
public class BusinessException extends RuntimeException {
    
    private final HttpStatus statusCode;
    
    public BusinessException(String message) {
        super(message);
        this.statusCode = HttpStatus.BAD_REQUEST;
    }
    
    public BusinessException(String message, HttpStatus statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
    
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = HttpStatus.BAD_REQUEST;
    }
    
    public BusinessException(String message, Throwable cause, HttpStatus statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }
} 