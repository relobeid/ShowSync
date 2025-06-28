package com.showsync.exception;

import com.showsync.dto.ApiError;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Global exception handler for consistent API error responses.
 * 
 * This class centralizes exception handling across all controllers,
 * providing standardized error responses with correlation IDs,
 * proper HTTP status codes, and user-friendly messages.
 * 
 * Features:
 * - Correlation ID generation for error tracking
 * - Different error handling based on environment (dev vs prod)
 * - Comprehensive exception coverage (validation, security, business logic)
 * - Structured error responses with field-level validation details
 * - Integration with monitoring and logging systems
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Value("${spring.profiles.active:prod}")
    private String activeProfile;
    
    private boolean isDevEnvironment() {
        return "dev".equals(activeProfile) || "test".equals(activeProfile);
    }
    
    private String generateCorrelationId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
    
    private String extractRequestPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationError(
            MethodArgumentNotValidException ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        
        ApiError apiError = ApiError.builder()
                .correlationId(correlationId)
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("VALIDATION_ERROR")
                .message("Validation failed for request")
                .path(path)
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
        
        if (isDevEnvironment()) {
            try {
                apiError.setDetails(ex.getMessage());
            } catch (Exception msgEx) {
                // Fallback if getMessage() fails (e.g., in testing with mocked parameters)
                apiError.setDetails("Validation failed: " + fieldErrors.size() + " field error(s)");
            }
        }
        
        log.warn("Validation error [{}]: {} field errors at {}", 
                correlationId, fieldErrors.size(), path);
        
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * Handle validation errors from @RequestParam and path variables.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        ApiError apiError = ApiError.withCorrelationId(
                correlationId,
                HttpStatus.BAD_REQUEST.value(),
                "CONSTRAINT_VIOLATION",
                "Invalid request parameters",
                path
        );
        
        if (isDevEnvironment()) {
            apiError.setDetails(ex.getMessage());
        }
        
        log.warn("Constraint violation [{}]: {} at {}", correlationId, ex.getMessage(), path);
        
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * Handle form binding errors.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<ApiError> handleBindException(
            BindException ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        List<ApiError.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::mapFieldError)
                .collect(Collectors.toList());
        
        ApiError apiError = ApiError.builder()
                .correlationId(correlationId)
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode("BINDING_ERROR")
                .message("Request binding failed")
                .path(path)
                .timestamp(LocalDateTime.now())
                .fieldErrors(fieldErrors)
                .build();
        
        log.warn("Binding error [{}]: {} field errors at {}", 
                correlationId, fieldErrors.size(), path);
        
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * Handle authentication errors.
     */
    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class})
    public ResponseEntity<ApiError> handleAuthenticationError(
            AuthenticationException ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        ApiError apiError = ApiError.withCorrelationId(
                correlationId,
                HttpStatus.UNAUTHORIZED.value(),
                "AUTHENTICATION_ERROR",
                "Authentication failed",
                path
        );
        
        if (isDevEnvironment()) {
            apiError.setDetails(ex.getMessage());
        }
        
        log.warn("Authentication error [{}]: {} at {}", correlationId, ex.getMessage(), path);
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiError);
    }
    
    /**
     * Handle authorization errors.
     */
    @ExceptionHandler({AccessDeniedException.class, SecurityException.class})
    public ResponseEntity<ApiError> handleAuthorizationError(
            Exception ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        ApiError apiError = ApiError.withCorrelationId(
                correlationId,
                HttpStatus.FORBIDDEN.value(),
                "AUTHORIZATION_ERROR",
                "Access denied",
                path
        );
        
        if (isDevEnvironment()) {
            apiError.setDetails(ex.getMessage());
        }
        
        log.warn("Authorization error [{}]: {} at {}", correlationId, ex.getMessage(), path);
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiError);
    }
    
    /**
     * Handle business logic errors (IllegalArgumentException).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        ApiError apiError = ApiError.withCorrelationId(
                correlationId,
                HttpStatus.BAD_REQUEST.value(),
                "INVALID_REQUEST",
                ex.getMessage(),
                path
        );
        
        log.warn("Business logic error [{}]: {} at {}", correlationId, ex.getMessage(), path);
        
        return ResponseEntity.badRequest().body(apiError);
    }
    
    /**
     * Handle external API errors.
     */
    @ExceptionHandler(WebClientResponseException.class)
    public ResponseEntity<ApiError> handleExternalApiError(
            WebClientResponseException ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        // Map external API errors to appropriate client errors
        HttpStatus status = mapExternalApiError(ex.getStatusCode().value());
        String message = status == HttpStatus.SERVICE_UNAVAILABLE 
                ? "External service temporarily unavailable" 
                : "External service error";
        
        ApiError apiError = ApiError.withCorrelationId(
                correlationId,
                status.value(),
                "EXTERNAL_API_ERROR",
                message,
                path
        );
        
        if (isDevEnvironment()) {
            apiError.setDetails(String.format("External API returned %d: %s", 
                    ex.getStatusCode().value(), ex.getResponseBodyAsString()));
        }
        
        log.error("External API error [{}]: {} {} at {}", 
                correlationId, ex.getStatusCode(), ex.getResponseBodyAsString(), path);
        
        return ResponseEntity.status(status).body(apiError);
    }
    
    /**
     * Handle all other runtime exceptions.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        ApiError apiError = ApiError.withCorrelationId(
                correlationId,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                path
        );
        
        if (isDevEnvironment()) {
            apiError.setDetails(ex.getMessage());
        }
        
        log.error("Runtime error [{}]: {} at {}", correlationId, ex.getMessage(), path, ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
    
    /**
     * Handle all other exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(
            Exception ex, WebRequest request) {
        
        String correlationId = generateCorrelationId();
        String path = extractRequestPath(request);
        
        ApiError apiError = ApiError.withCorrelationId(
                correlationId,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                path
        );
        
        if (isDevEnvironment()) {
            apiError.setDetails(ex.getMessage());
        }
        
        log.error("Unexpected error [{}]: {} at {}", correlationId, ex.getMessage(), path, ex);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiError);
    }
    
    /**
     * Map Spring validation FieldError to our ApiError.FieldError.
     */
    private ApiError.FieldError mapFieldError(FieldError fieldError) {
        return ApiError.FieldError.builder()
                .field(fieldError.getField())
                .rejectedValue(fieldError.getRejectedValue())
                .message(fieldError.getDefaultMessage())
                .build();
    }
    
    /**
     * Map external API HTTP status codes to appropriate client response codes.
     */
    private HttpStatus mapExternalApiError(int externalStatus) {
        return switch (externalStatus) {
            case 400, 401, 403, 404 -> HttpStatus.BAD_REQUEST;
            case 429 -> HttpStatus.TOO_MANY_REQUESTS;
            case 500, 502, 503, 504 -> HttpStatus.SERVICE_UNAVAILABLE;
            default -> HttpStatus.SERVICE_UNAVAILABLE;
        };
    }
} 