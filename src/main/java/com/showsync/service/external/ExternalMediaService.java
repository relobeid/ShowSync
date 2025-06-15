package com.showsync.service.external;

import com.showsync.dto.external.openlibrary.OpenLibrarySearchResponse;
import com.showsync.dto.external.tmdb.TmdbMovieResponse;
import com.showsync.dto.external.tmdb.TmdbSearchResponse;
import com.showsync.dto.external.tmdb.TmdbTvShowResponse;
import reactor.core.publisher.Mono;

/**
 * Service interface for external media API operations.
 * 
 * This interface defines the contract for interacting with external
 * media APIs such as TMDb and Open Library. It provides methods for
 * searching and retrieving detailed information about movies, TV shows, and books.
 * 
 * Features:
 * - Movie search and details from TMDb
 * - TV show search and details from TMDb
 * - Book search from Open Library
 * - Reactive programming model with Mono return types
 * - Caching support for API responses
 * 
 * Implementation Note:
 * All methods return Mono<T> for reactive, non-blocking operations.
 * Implementations should handle rate limiting, caching, and error recovery.
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
public interface ExternalMediaService {

    /**
     * Search for movies using TMDb API.
     * 
     * @param query the search query
     * @param page the page number (1-based)
     * @return Mono containing search results
     */
    Mono<TmdbSearchResponse<TmdbMovieResponse>> searchMovies(String query, int page);

    /**
     * Search for TV shows using TMDb API.
     * 
     * @param query the search query
     * @param page the page number (1-based)
     * @return Mono containing search results
     */
    Mono<TmdbSearchResponse<TmdbTvShowResponse>> searchTvShows(String query, int page);

    /**
     * Search for books using Open Library API.
     * 
     * @param query the search query
     * @param limit the number of results to return
     * @param offset the offset for pagination
     * @return Mono containing search results
     */
    Mono<OpenLibrarySearchResponse> searchBooks(String query, int limit, int offset);

    /**
     * Get detailed movie information by TMDb ID.
     * 
     * @param movieId the TMDb movie ID
     * @return Mono containing detailed movie information
     */
    Mono<TmdbMovieResponse> getMovieDetails(Long movieId);

    /**
     * Get detailed TV show information by TMDb ID.
     * 
     * @param tvShowId the TMDb TV show ID
     * @return Mono containing detailed TV show information
     */
    Mono<TmdbTvShowResponse> getTvShowDetails(Long tvShowId);

    /**
     * Search for movies with default pagination (first page).
     * 
     * @param query the search query
     * @return Mono containing search results
     */
    default Mono<TmdbSearchResponse<TmdbMovieResponse>> searchMovies(String query) {
        return searchMovies(query, 1);
    }

    /**
     * Search for TV shows with default pagination (first page).
     * 
     * @param query the search query
     * @return Mono containing search results
     */
    default Mono<TmdbSearchResponse<TmdbTvShowResponse>> searchTvShows(String query) {
        return searchTvShows(query, 1);
    }

    /**
     * Search for books with default pagination.
     * 
     * @param query the search query
     * @return Mono containing search results
     */
    default Mono<OpenLibrarySearchResponse> searchBooks(String query) {
        return searchBooks(query, 20, 0);
    }
} 