package com.showsync.service.external.impl;

import com.showsync.config.ExternalApiProperties;
import com.showsync.dto.external.openlibrary.OpenLibrarySearchResponse;
import com.showsync.dto.external.tmdb.TmdbMovieResponse;
import com.showsync.dto.external.tmdb.TmdbSearchResponse;
import com.showsync.dto.external.tmdb.TmdbTvShowResponse;
import com.showsync.service.external.ExternalMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

/**
 * Implementation of ExternalMediaService for TMDb and Open Library API integration.
 * 
 * This service provides comprehensive external API integration with:
 * - TMDb API for movies and TV shows
 * - Open Library API for books
 * - Caching for performance optimization
 * - Rate limiting and retry logic
 * - Comprehensive error handling
 * 
 * Features:
 * - Reactive programming with WebClient
 * - Method-level caching with Spring Cache
 * - Automatic retry with exponential backoff
 * - Detailed logging and monitoring
 * - Type-safe response handling
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalMediaServiceImpl implements ExternalMediaService {

    private final ExternalApiProperties apiProperties;
    
    @Qualifier("tmdbWebClient")
    private final WebClient tmdbWebClient;
    
    @Qualifier("openLibraryWebClient")
    private final WebClient openLibraryWebClient;

    /**
     * Search for movies using TMDb API with caching.
     */
    @Override
    @Cacheable(value = "external-api-responses", 
               key = "'tmdb-movie-search-' + #query + '-page-' + #page",
               unless = "#result == null || #result.block() == null")
    public Mono<TmdbSearchResponse<TmdbMovieResponse>> searchMovies(String query, int page) {
        log.debug("Searching TMDb movies: query='{}', page={}", query, page);
        
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/movie")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("include_adult", "false")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<TmdbSearchResponse<TmdbMovieResponse>>() {})
                .retryWhen(createRetrySpec("TMDb movie search"))
                .doOnSuccess(response -> {
                    if (response != null) {
                        log.debug("TMDb movie search successful: {} results found", 
                                response.getTotalResults());
                    }
                })
                .doOnError(error -> log.error("TMDb movie search failed for query: {}", query, error));
    }

    /**
     * Search for TV shows using TMDb API with caching.
     */
    @Override
    @Cacheable(value = "external-api-responses", 
               key = "'tmdb-tv-search-' + #query + '-page-' + #page",
               unless = "#result == null || #result.block() == null")
    public Mono<TmdbSearchResponse<TmdbTvShowResponse>> searchTvShows(String query, int page) {
        log.debug("Searching TMDb TV shows: query='{}', page={}", query, page);
        
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search/tv")
                        .queryParam("query", query)
                        .queryParam("page", page)
                        .queryParam("include_adult", "false")
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<TmdbSearchResponse<TmdbTvShowResponse>>() {})
                .retryWhen(createRetrySpec("TMDb TV search"))
                .doOnSuccess(response -> {
                    if (response != null) {
                        log.debug("TMDb TV search successful: {} results found", 
                                response.getTotalResults());
                    }
                })
                .doOnError(error -> log.error("TMDb TV search failed for query: {}", query, error));
    }

    /**
     * Search for books using Open Library API with caching.
     */
    @Override
    @Cacheable(value = "external-api-responses", 
               key = "'openlibrary-search-' + #query + '-limit-' + #limit + '-offset-' + #offset",
               unless = "#result == null || #result.block() == null")
    public Mono<OpenLibrarySearchResponse> searchBooks(String query, int limit, int offset) {
        log.debug("Searching Open Library books: query='{}', limit={}, offset={}", query, limit, offset);
        
        return openLibraryWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search.json")
                        .queryParam("q", query)
                        .queryParam("limit", limit)
                        .queryParam("offset", offset)
                        .queryParam("fields", "key,title,author_name,first_publish_year,isbn,cover_i,publisher,language")
                        .build())
                .retrieve()
                .bodyToMono(OpenLibrarySearchResponse.class)
                .retryWhen(createRetrySpec("Open Library search"))
                .doOnSuccess(response -> {
                    if (response != null) {
                        log.debug("Open Library search successful: {} results found", 
                                response.getNumFound());
                    }
                })
                .doOnError(error -> log.error("Open Library search failed for query: {}", query, error));
    }

    /**
     * Get detailed movie information by TMDb ID with caching.
     */
    @Override
    @Cacheable(value = "external-api-responses", 
               key = "'tmdb-movie-details-' + #movieId",
               unless = "#result == null || #result.block() == null")
    public Mono<TmdbMovieResponse> getMovieDetails(Long movieId) {
        log.debug("Fetching TMDb movie details: movieId={}", movieId);
        
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/movie/{id}")
                        .build(movieId))
                .retrieve()
                .bodyToMono(TmdbMovieResponse.class)
                .retryWhen(createRetrySpec("TMDb movie details"))
                .doOnSuccess(response -> {
                    if (response != null) {
                        log.debug("TMDb movie details successful: title='{}'", response.getTitle());
                    }
                })
                .doOnError(error -> log.error("TMDb movie details failed for ID: {}", movieId, error));
    }

    /**
     * Get detailed TV show information by TMDb ID with caching.
     */
    @Override
    @Cacheable(value = "external-api-responses", 
               key = "'tmdb-tv-details-' + #tvShowId",
               unless = "#result == null || #result.block() == null")
    public Mono<TmdbTvShowResponse> getTvShowDetails(Long tvShowId) {
        log.debug("Fetching TMDb TV show details: tvShowId={}", tvShowId);
        
        return tmdbWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/tv/{id}")
                        .build(tvShowId))
                .retrieve()
                .bodyToMono(TmdbTvShowResponse.class)
                .retryWhen(createRetrySpec("TMDb TV details"))
                .doOnSuccess(response -> {
                    if (response != null) {
                        log.debug("TMDb TV details successful: name='{}'", response.getName());
                    }
                })
                .doOnError(error -> log.error("TMDb TV details failed for ID: {}", tvShowId, error));
    }

    /**
     * Create retry specification for external API calls.
     * 
     * @param operation the operation name for logging
     * @return Retry specification with exponential backoff
     */
    private Retry createRetrySpec(String operation) {
        return Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(10))
                .filter(this::isRetryableError)
                .doBeforeRetry(retrySignal -> 
                        log.warn("Retrying {} (attempt {}): {}", 
                                operation, retrySignal.totalRetries() + 1, 
                                retrySignal.failure().getMessage()))
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("Retry exhausted for {}: {}", operation, retrySignal.failure().getMessage());
                    return retrySignal.failure();
                });
    }

    /**
     * Determine if an error is retryable.
     * 
     * @param throwable the error to check
     * @return true if the error should trigger a retry
     */
    private boolean isRetryableError(Throwable throwable) {
        if (throwable instanceof WebClientResponseException) {
            WebClientResponseException ex = (WebClientResponseException) throwable;
            int statusCode = ex.getStatusCode().value();
            
            // Retry on server errors and rate limiting
            return statusCode >= 500 || statusCode == 429;
        }
        
        // Retry on network errors and timeouts
        return throwable instanceof java.net.ConnectException ||
               throwable instanceof java.util.concurrent.TimeoutException ||
               throwable.getCause() instanceof java.net.SocketTimeoutException;
    }
} 