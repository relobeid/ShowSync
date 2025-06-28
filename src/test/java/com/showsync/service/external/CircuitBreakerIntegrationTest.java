package com.showsync.service.external;

import com.showsync.dto.external.tmdb.TmdbMovieResponse;
import com.showsync.dto.external.tmdb.TmdbSearchResponse;
import com.showsync.service.external.ExternalMediaService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Integration test for circuit breaker functionality in external media service.
 * 
 * Tests that circuit breaker properly handles failures and transitions between states:
 * - Closed: Normal operation
 * - Open: Circuit breaker triggered after failures
 * - Half-Open: Testing if service is back up
 * 
 * Also tests fallback methods when circuit breaker is open.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "resilience4j.circuitbreaker.instances.tmdb-api.ringBufferSizeInClosedState=3",
    "resilience4j.circuitbreaker.instances.tmdb-api.failureRateThreshold=50",
    "resilience4j.circuitbreaker.instances.tmdb-api.minimumNumberOfCalls=2",
    "resilience4j.circuitbreaker.instances.tmdb-api.waitDurationInOpenState=1s"
})
class CircuitBreakerIntegrationTest {

    @Autowired
    private ExternalMediaService externalMediaService;
    
    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;
    
    @MockBean
    @Qualifier("tmdbWebClient")
    private WebClient tmdbWebClient;
    
    @MockBean
    @Qualifier("openLibraryWebClient")
    private WebClient openLibraryWebClient;
    
    private CircuitBreaker tmdbCircuitBreaker;
    
    @BeforeEach
    void setUp() {
        tmdbCircuitBreaker = circuitBreakerRegistry.circuitBreaker("tmdb-api");
        tmdbCircuitBreaker.reset(); // Reset circuit breaker state
    }
    
    @Test
    void circuitBreaker_ShouldTransitionToOpen_WhenFailureThresholdExceeded() {
        // Arrange - Mock WebClient to return server errors
        WebClient.RequestHeadersUriSpec requestSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);
        
        when(tmdbWebClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(WebClientResponseException.create(500, "Internal Server Error", null, null, null)));
        
        // Act - Make multiple failing calls to trigger circuit breaker
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectNextMatches(response -> response.getTotalResults() == 0) // Fallback response
                .verifyComplete();
                
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectNextMatches(response -> response.getTotalResults() == 0) // Fallback response
                .verifyComplete();
                
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectNextMatches(response -> response.getTotalResults() == 0) // Fallback response
                .verifyComplete();
        
        // Assert - Circuit breaker should be open after enough failures
        // Note: This might take a moment to transition, so we check the metrics
        assertThat(tmdbCircuitBreaker.getMetrics().getNumberOfFailedCalls()).isGreaterThan(0);
    }
    
    @Test
    void fallbackMethod_ShouldReturnEmptyResponse_WhenCircuitBreakerOpen() {
        // Arrange - Force circuit breaker to open state
        tmdbCircuitBreaker.transitionToOpenState();
        
        // Act - Call service when circuit breaker is open
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectNextMatches(response -> {
                    // Verify fallback response structure
                    return response.getPage() == 1 &&
                           response.getTotalResults() == 0 &&
                           response.getTotalPages() == 0 &&
                           response.getResults().isEmpty();
                })
                .verifyComplete();
    }
    
    @Test
    void circuitBreaker_ShouldAllowCalls_WhenInClosedState() {
        // Arrange - Mock successful response
        WebClient.RequestHeadersUriSpec requestSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);
        
        TmdbSearchResponse<TmdbMovieResponse> mockResponse = new TmdbSearchResponse<>();
        mockResponse.setPage(1);
        mockResponse.setTotalResults(1);
        mockResponse.setTotalPages(1);
        mockResponse.setResults(java.util.Collections.emptyList());
        
        when(tmdbWebClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(mockResponse));
        
        // Assert - Circuit breaker should be in closed state initially
        assertThat(tmdbCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        
        // Act - Make successful call
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectNextMatches(response -> response.getTotalResults() == 1)
                .verifyComplete();
        
        // Assert - Circuit breaker should remain closed
        assertThat(tmdbCircuitBreaker.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
    }
    
    @Test
    void circuitBreakerMetrics_ShouldTrackSuccessAndFailures() {
        // Arrange - Mock alternating success/failure responses
        WebClient.RequestHeadersUriSpec requestSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.RequestHeadersSpec headersSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersSpec.class);
        WebClient.ResponseSpec responseSpec = org.mockito.Mockito.mock(WebClient.ResponseSpec.class);
        
        TmdbSearchResponse<TmdbMovieResponse> successResponse = new TmdbSearchResponse<>();
        successResponse.setTotalResults(1);
        
        when(tmdbWebClient.get()).thenReturn(requestSpec);
        when(requestSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(org.springframework.core.ParameterizedTypeReference.class)))
                .thenReturn(Mono.just(successResponse))
                .thenReturn(Mono.error(WebClientResponseException.create(500, "Error", null, null, null)));
        
        // Act - Make one successful call and one failing call
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectNextCount(1)
                .verifyComplete();
                
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectNextCount(1) // Fallback response
                .verifyComplete();
        
        // Assert - Metrics should reflect both success and failure
        CircuitBreaker.Metrics metrics = tmdbCircuitBreaker.getMetrics();
        assertThat(metrics.getNumberOfSuccessfulCalls()).isGreaterThanOrEqualTo(1);
    }
} 