package com.showsync.exception;

import com.showsync.dto.ApiError;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import jakarta.validation.ConstraintViolationException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for GlobalExceptionHandler.
 * 
 * Tests all exception handling scenarios to ensure consistent
 * error responses, proper status codes, and correlation ID generation.
 */
@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private WebRequest mockRequest;

    @BeforeEach
    void setUp() {
        mockRequest = mock(WebRequest.class);
        when(mockRequest.getDescription(false)).thenReturn("uri=/api/test");
        
        // Set active profile to dev for testing
        ReflectionTestUtils.setField(globalExceptionHandler, "activeProfile", "dev");
    }

    @Test
    void handleValidationError_ShouldReturnBadRequestWithFieldErrors() {
        // Arrange
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
        bindingResult.addError(new FieldError("testObject", "email", "invalid@", false, null, null, "Email format is invalid"));
        
        // Create a properly mocked MethodParameter
        org.springframework.core.MethodParameter methodParameter = mock(org.springframework.core.MethodParameter.class);
        try {
            // Create a dummy method for the mock
            java.lang.reflect.Method dummyMethod = this.getClass().getDeclaredMethod("dummyMethod", String.class);
            when(methodParameter.getExecutable()).thenReturn(dummyMethod);
        } catch (NoSuchMethodException e) {
            // Fallback to null - the GlobalExceptionHandler should handle this gracefully
            when(methodParameter.getExecutable()).thenReturn(null);
        }
        
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(methodParameter, bindingResult);

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleValidationError(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed for request");
        assertThat(response.getBody().getPath()).isEqualTo("/api/test");
        assertThat(response.getBody().getCorrelationId()).isNotNull();
        assertThat(response.getBody().getCorrelationId()).hasSize(8);
        assertThat(response.getBody().getFieldErrors()).hasSize(1);
        assertThat(response.getBody().getFieldErrors().get(0).getField()).isEqualTo("email");
        assertThat(response.getBody().getFieldErrors().get(0).getMessage()).isEqualTo("Email format is invalid");
        assertThat(response.getBody().getTimestamp()).isNotNull();
    }

    @Test
    void handleBindException_ShouldReturnBadRequestWithFieldErrors() {
        // Arrange
        Object target = new Object();
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(target, "testObject");
        bindingResult.addError(new FieldError("testObject", "name", "", false, null, null, "Name is required"));
        
        BindException exception = new BindException(bindingResult);

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleBindException(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("BINDING_ERROR");
        assertThat(response.getBody().getFieldErrors()).hasSize(1);
    }

    @Test
    void handleConstraintViolation_ShouldReturnBadRequest() {
        // Arrange
        ConstraintViolationException exception = new ConstraintViolationException("Size must be between 1 and 100", Collections.emptySet());

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleConstraintViolation(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("CONSTRAINT_VIOLATION");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid request parameters");
        assertThat(response.getBody().getDetails()).isEqualTo("Size must be between 1 and 100");
    }

    @Test
    void handleAuthenticationError_ShouldReturnUnauthorized() {
        // Arrange
        BadCredentialsException exception = new BadCredentialsException("Invalid credentials");

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleAuthenticationError(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(401);
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHENTICATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication failed");
        assertThat(response.getBody().getDetails()).isEqualTo("Invalid credentials");
    }

    @Test
    void handleAuthorizationError_ShouldReturnForbidden() {
        // Arrange
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleAuthorizationError(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(403);
        assertThat(response.getBody().getErrorCode()).isEqualTo("AUTHORIZATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Access denied");
    }

    @Test
    void handleIllegalArgument_ShouldReturnBadRequest() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid media ID");

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleIllegalArgument(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INVALID_REQUEST");
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid media ID");
    }

    @Test
    void handleExternalApiError_ShouldMapStatusCorrectly() {
        // Arrange - 500 error from external API should become 503 Service Unavailable
        WebClientResponseException exception = WebClientResponseException.create(
                500, "Internal Server Error", null, "Server error".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleExternalApiError(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(503);
        assertThat(response.getBody().getErrorCode()).isEqualTo("EXTERNAL_API_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("External service temporarily unavailable");
    }

    @Test
    void handleExternalApiError_ShouldHandleRateLimiting() {
        // Arrange - 429 Too Many Requests from external API
        WebClientResponseException exception = WebClientResponseException.create(
                429, "Too Many Requests", null, "Rate limit exceeded".getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleExternalApiError(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(429);
    }

    @Test
    void handleRuntimeException_ShouldReturnInternalServerError() {
        // Arrange
        RuntimeException exception = new RuntimeException("Database connection failed");

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleRuntimeException(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(500);
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getDetails()).isEqualTo("Database connection failed");
    }

    @Test
    void handleGenericException_ShouldReturnInternalServerError() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleGenericException(exception, mockRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldNotExposeDetailsInProductionEnvironment() {
        // Arrange
        ReflectionTestUtils.setField(globalExceptionHandler, "activeProfile", "prod");
        RuntimeException exception = new RuntimeException("Sensitive database info");

        // Act
        ResponseEntity<ApiError> response = globalExceptionHandler.handleRuntimeException(exception, mockRequest);

        // Assert
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDetails()).isNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void shouldGenerateUniqueCorrelationIds() {
        // Arrange
        IllegalArgumentException exception1 = new IllegalArgumentException("Error 1");
        IllegalArgumentException exception2 = new IllegalArgumentException("Error 2");

        // Act
        ResponseEntity<ApiError> response1 = globalExceptionHandler.handleIllegalArgument(exception1, mockRequest);
        ResponseEntity<ApiError> response2 = globalExceptionHandler.handleIllegalArgument(exception2, mockRequest);

        // Assert
        assertThat(response1.getBody()).isNotNull();
        assertThat(response2.getBody()).isNotNull();
        assertThat(response1.getBody().getCorrelationId()).isNotEqualTo(response2.getBody().getCorrelationId());
        assertThat(response1.getBody().getCorrelationId()).hasSize(8);
        assertThat(response2.getBody().getCorrelationId()).hasSize(8);
    }
    
    // Dummy method for mocking MethodParameter
    private void dummyMethod(String param) {
        // This method is only used for testing - no implementation needed
    }
} 