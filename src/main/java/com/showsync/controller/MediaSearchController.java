package com.showsync.controller;

import com.showsync.dto.external.openlibrary.OpenLibrarySearchResponse;
import com.showsync.dto.external.tmdb.TmdbMovieResponse;
import com.showsync.dto.external.tmdb.TmdbSearchResponse;
import com.showsync.dto.external.tmdb.TmdbTvShowResponse;
import com.showsync.service.external.ExternalMediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * REST Controller for media search operations.
 * 
 * This controller provides endpoints for searching external media APIs
 * including TMDb for movies/TV shows and Open Library for books.
 * All endpoints require authentication.
 * 
 * Features:
 * - Movie search via TMDb API
 * - TV show search via TMDb API  
 * - Book search via Open Library API
 * - Detailed media information retrieval
 * - Comprehensive error handling
 * - OpenAPI documentation
 * 
 * Security:
 * All endpoints require user authentication (ROLE_USER or higher).
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/media/search")
@RequiredArgsConstructor
@Validated
@Tag(name = "Media Search", description = "External media search operations")
public class MediaSearchController {

    private final ExternalMediaService externalMediaService;

    /**
     * Search for movies using TMDb API.
     * 
     * @param query the search query
     * @param page the page number (1-based, default: 1)
     * @return search results containing movies
     */
    @GetMapping("/movies")
    @Operation(summary = "Search for movies", 
               description = "Search for movies using The Movie Database (TMDb) API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = TmdbSearchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "503", description = "External API service unavailable")
    })
    public Mono<ResponseEntity<TmdbSearchResponse<TmdbMovieResponse>>> searchMovies(
            @Parameter(description = "Search query", required = true, example = "The Matrix")
            @RequestParam @NotBlank @Size(min = 1, max = 100) String query,
            
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int page) {
        
        log.info("Movie search request: query='{}', page={}", query, page);
        
        return externalMediaService.searchMovies(query, page)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.debug("Movie search completed successfully"))
                .doOnError(error -> log.error("Movie search failed", error))
                .onErrorReturn(ResponseEntity.status(503).build());
    }

    /**
     * Search for TV shows using TMDb API.
     * 
     * @param query the search query
     * @param page the page number (1-based, default: 1)
     * @return search results containing TV shows
     */
    @GetMapping("/tv-shows")
    @Operation(summary = "Search for TV shows", 
               description = "Search for TV shows using The Movie Database (TMDb) API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = TmdbSearchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "503", description = "External API service unavailable")
    })
    public Mono<ResponseEntity<TmdbSearchResponse<TmdbTvShowResponse>>> searchTvShows(
            @Parameter(description = "Search query", required = true, example = "Breaking Bad")
            @RequestParam @NotBlank @Size(min = 1, max = 100) String query,
            
            @Parameter(description = "Page number (1-based)", example = "1")
            @RequestParam(defaultValue = "1") @Min(1) @Max(1000) int page) {
        
        log.info("TV show search request: query='{}', page={}", query, page);
        
        return externalMediaService.searchTvShows(query, page)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.debug("TV show search completed successfully"))
                .doOnError(error -> log.error("TV show search failed", error))
                .onErrorReturn(ResponseEntity.status(503).build());
    }

    /**
     * Search for books using Open Library API.
     * 
     * @param query the search query
     * @param limit the number of results to return (default: 20)
     * @param offset the offset for pagination (default: 0)
     * @return search results containing books
     */
    @GetMapping("/books")
    @Operation(summary = "Search for books", 
               description = "Search for books using Open Library API")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search completed successfully",
                    content = @Content(schema = @Schema(implementation = OpenLibrarySearchResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "503", description = "External API service unavailable")
    })
    public Mono<ResponseEntity<OpenLibrarySearchResponse>> searchBooks(
            @Parameter(description = "Search query", required = true, example = "The Lord of the Rings")
            @RequestParam @NotBlank @Size(min = 1, max = 100) String query,
            
            @Parameter(description = "Number of results to return", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit,
            
            @Parameter(description = "Offset for pagination", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int offset) {
        
        log.info("Book search request: query='{}', limit={}, offset={}", query, limit, offset);
        
        return externalMediaService.searchBooks(query, limit, offset)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.debug("Book search completed successfully"))
                .doOnError(error -> log.error("Book search failed", error))
                .onErrorReturn(ResponseEntity.status(503).build());
    }

    /**
     * Get detailed movie information by TMDb ID.
     * 
     * @param movieId the TMDb movie ID
     * @return detailed movie information
     */
    @GetMapping("/movies/{movieId}")
    @Operation(summary = "Get movie details", 
               description = "Get detailed movie information by TMDb ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Movie details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TmdbMovieResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid movie ID"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Movie not found"),
        @ApiResponse(responseCode = "503", description = "External API service unavailable")
    })
    public Mono<ResponseEntity<TmdbMovieResponse>> getMovieDetails(
            @Parameter(description = "TMDb movie ID", required = true, example = "603")
            @PathVariable @Min(1) Long movieId) {
        
        log.info("Movie details request: movieId={}", movieId);
        
        return externalMediaService.getMovieDetails(movieId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.debug("Movie details retrieved successfully"))
                .doOnError(error -> log.error("Movie details retrieval failed", error))
                .onErrorReturn(ResponseEntity.notFound().build());
    }

    /**
     * Get detailed TV show information by TMDb ID.
     * 
     * @param tvShowId the TMDb TV show ID
     * @return detailed TV show information
     */
    @GetMapping("/tv-shows/{tvShowId}")
    @Operation(summary = "Get TV show details", 
               description = "Get detailed TV show information by TMDb ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "TV show details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TmdbTvShowResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid TV show ID"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "TV show not found"),
        @ApiResponse(responseCode = "503", description = "External API service unavailable")
    })
    public Mono<ResponseEntity<TmdbTvShowResponse>> getTvShowDetails(
            @Parameter(description = "TMDb TV show ID", required = true, example = "1396")
            @PathVariable @Min(1) Long tvShowId) {
        
        log.info("TV show details request: tvShowId={}", tvShowId);
        
        return externalMediaService.getTvShowDetails(tvShowId)
                .map(ResponseEntity::ok)
                .doOnSuccess(response -> log.debug("TV show details retrieved successfully"))
                .doOnError(error -> log.error("TV show details retrieval failed", error))
                .onErrorReturn(ResponseEntity.notFound().build());
    }
} 