package com.showsync.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standardized API error response format.
 * 
 * This DTO provides a consistent error response structure across all API endpoints.
 * It includes correlation IDs for tracking, detailed error information, and
 * validation error details when applicable.
 * 
 * Features:
 * - Correlation ID for error tracking and debugging
 * - HTTP status code and standardized error codes
 * - User-friendly error messages
 * - Detailed validation errors for form submissions
 * - Timestamp for logging and monitoring
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    
    /**
     * Correlation ID for tracking errors across logs and services.
     */
    private String correlationId;
    
    /**
     * HTTP status code.
     */
    private int status;
    
    /**
     * Application-specific error code for client handling.
     */
    private String errorCode;
    
    /**
     * User-friendly error message.
     */
    private String message;
    
    /**
     * Detailed error description for debugging (not exposed in production).
     */
    private String details;
    
    /**
     * Request path that caused the error.
     */
    private String path;
    
    /**
     * Timestamp when the error occurred.
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    /**
     * List of field validation errors (for form validation failures).
     */
    private List<FieldError> fieldErrors;
    
    /**
     * Field-specific validation error.
     */
    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FieldError {
        /**
         * Name of the field that failed validation.
         */
        private String field;
        
        /**
         * Value that was rejected.
         */
        private Object rejectedValue;
        
        /**
         * Validation error message.
         */
        private String message;
    }
    
    /**
     * Create a simple error response with just message and status.
     */
    public static ApiError simple(int status, String message, String path) {
        return ApiError.builder()
                .status(status)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Create an error response with correlation ID.
     */
    public static ApiError withCorrelationId(String correlationId, int status, String errorCode, 
                                           String message, String path) {
        return ApiError.builder()
                .correlationId(correlationId)
                .status(status)
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
} 