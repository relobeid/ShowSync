package com.showsync.controller;

import com.showsync.dto.library.AddMediaToLibraryRequest;
import com.showsync.dto.library.MediaLibraryMapper;
import com.showsync.dto.library.MediaLibraryResponse;
import com.showsync.dto.library.UpdateMediaRequest;
import com.showsync.entity.UserMediaInteraction;
import com.showsync.entity.UserMediaInteraction.Status;
import com.showsync.security.UserPrincipal;
import com.showsync.service.UserMediaLibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for user media library operations.
 * Provides secure endpoints for managing user's personal media library.
 * 
 * Security Features:
 * - All endpoints require authentication via JWT token
 * - User can only access their own library entries
 * - Comprehensive input validation
 * - Secure error handling without information leakage
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@Slf4j
@RestController
@RequestMapping("/api/library")
@Tag(name = "User Media Library", description = "Manage user's personal media library")
public class UserMediaLibraryController {
    
    private final UserMediaLibraryService userMediaLibraryService;
    private final MediaLibraryMapper mediaLibraryMapper;
    
    @Autowired
    public UserMediaLibraryController(
            UserMediaLibraryService userMediaLibraryService,
            MediaLibraryMapper mediaLibraryMapper) {
        this.userMediaLibraryService = userMediaLibraryService;
        this.mediaLibraryMapper = mediaLibraryMapper;
    }
    
    @PostMapping
    @Operation(summary = "Add media to library", 
               description = "Add a movie, TV show, or book to user's personal library")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Media added successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "409", description = "Media already in library"),
        @ApiResponse(responseCode = "500", description = "External API unavailable")
    })
    public ResponseEntity<?> addMediaToLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody AddMediaToLibraryRequest request) {
        
        log.info("Adding media to library - userId: {}, externalId: {}, source: {}", 
                userPrincipal.getUser().getId(), request.getExternalId(), request.getExternalSource());
        
        try {
            UserMediaInteraction interaction = userMediaLibraryService.addMediaToLibrary(
                    userPrincipal.getUser().getId(),
                    request.getExternalId(),
                    request.getExternalSource(),
                    request.getInitialStatus()
            );
            
            MediaLibraryResponse response = mediaLibraryMapper.toResponse(interaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request to add media - userId: {}, error: {}", 
                    userPrincipal.getUser().getId(), e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
            
        } catch (RuntimeException e) {
            log.error("Failed to add media to library - userId: {}", 
                    userPrincipal.getUser().getId(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unable to add media to library"));
        }
    }
    
    @GetMapping
    @Operation(summary = "Get user's media library", 
               description = "Retrieve user's complete media library with optional status filtering")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Library retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<MediaLibraryResponse>> getUserLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Filter by status (optional)")
            @RequestParam(required = false) Status status) {
        
        log.debug("Getting user library - userId: {}, status filter: {}", 
                userPrincipal.getUser().getId(), status);
        
        List<UserMediaInteraction> interactions = userMediaLibraryService.getUserLibrary(
                userPrincipal.getUser().getId(), status);
        
        List<MediaLibraryResponse> responses = mediaLibraryMapper.toResponseList(interactions);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/favorites")
    @Operation(summary = "Get user's favorite media", 
               description = "Retrieve all media marked as favorites by the user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Favorites retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required")
    })
    public ResponseEntity<List<MediaLibraryResponse>> getUserFavorites(
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.debug("Getting user favorites - userId: {}", userPrincipal.getUser().getId());
        
        List<UserMediaInteraction> favorites = userMediaLibraryService.getUserFavorites(
                userPrincipal.getUser().getId());
        
        List<MediaLibraryResponse> responses = mediaLibraryMapper.toResponseList(favorites);
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/{mediaId}")
    @Operation(summary = "Get specific media from library", 
               description = "Get details of a specific media item from user's library")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Media not found in library")
    })
    public ResponseEntity<?> getMediaFromLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Media ID in user's library")
            @PathVariable Long mediaId) {
        
        log.debug("Getting media from library - userId: {}, mediaId: {}", 
                userPrincipal.getUser().getId(), mediaId);
        
        Optional<UserMediaInteraction> interaction = userMediaLibraryService.getUserMediaInteraction(
                userPrincipal.getUser().getId(), mediaId);
        
        if (interaction.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        MediaLibraryResponse response = mediaLibraryMapper.toResponse(interaction.get());
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{mediaId}")
    @Operation(summary = "Update media in library", 
               description = "Update rating, status, progress, review, or favorite status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Media not found in library")
    })
    public ResponseEntity<?> updateMediaInLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Media ID in user's library")
            @PathVariable Long mediaId,
            @Valid @RequestBody UpdateMediaRequest request) {
        
        log.info("Updating media in library - userId: {}, mediaId: {}", 
                userPrincipal.getUser().getId(), mediaId);
        
        try {
            Long userId = userPrincipal.getUser().getId();
            UserMediaInteraction updatedInteraction = null;
            
            // Update each field that's provided in the request
            if (request.getRating() != null) {
                updatedInteraction = userMediaLibraryService.updateMediaRating(userId, mediaId, request.getRating());
            }
            
            if (request.getStatus() != null) {
                updatedInteraction = userMediaLibraryService.updateMediaStatus(userId, mediaId, request.getStatus());
            }
            
            if (request.getProgress() != null) {
                updatedInteraction = userMediaLibraryService.updateMediaProgress(userId, mediaId, request.getProgress());
            }
            
            if (request.getReview() != null) {
                updatedInteraction = userMediaLibraryService.updateMediaReview(userId, mediaId, request.getReview());
            }
            
            if (request.getFavorite() != null) {
                // Only toggle if the requested state is different from current state
                Optional<UserMediaInteraction> current = userMediaLibraryService.getUserMediaInteraction(userId, mediaId);
                if (current.isPresent() && current.get().isFavorite() != request.getFavorite()) {
                    updatedInteraction = userMediaLibraryService.toggleMediaFavorite(userId, mediaId);
                }
            }
            
            // If no updates were made, just return the current state
            if (updatedInteraction == null) {
                Optional<UserMediaInteraction> current = userMediaLibraryService.getUserMediaInteraction(userId, mediaId);
                if (current.isEmpty()) {
                    return ResponseEntity.notFound().build();
                }
                updatedInteraction = current.get();
            }
            
            MediaLibraryResponse response = mediaLibraryMapper.toResponse(updatedInteraction);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request to update media - userId: {}, mediaId: {}, error: {}", 
                    userPrincipal.getUser().getId(), mediaId, e.getMessage());
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
            
        } catch (SecurityException e) {
            log.warn("Security violation in update media - userId: {}, mediaId: {}, error: {}", 
                    userPrincipal.getUser().getId(), mediaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse("Access denied"));
            
        } catch (Exception e) {
            log.error("Failed to update media in library - userId: {}, mediaId: {}", 
                    userPrincipal.getUser().getId(), mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unable to update media"));
        }
    }
    
    @DeleteMapping("/{mediaId}")
    @Operation(summary = "Remove media from library", 
               description = "Completely remove a media item from user's library")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Media removed successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Media not found in library")
    })
    public ResponseEntity<?> removeMediaFromLibrary(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Media ID in user's library")
            @PathVariable Long mediaId) {
        
        log.info("Removing media from library - userId: {}, mediaId: {}", 
                userPrincipal.getUser().getId(), mediaId);
        
        try {
            userMediaLibraryService.removeMediaFromLibrary(userPrincipal.getUser().getId(), mediaId);
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Invalid request to remove media - userId: {}, mediaId: {}, error: {}", 
                    userPrincipal.getUser().getId(), mediaId, e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (SecurityException e) {
            log.warn("Security violation in remove media - userId: {}, mediaId: {}, error: {}", 
                    userPrincipal.getUser().getId(), mediaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(createErrorResponse("Access denied"));
            
        } catch (Exception e) {
            log.error("Failed to remove media from library - userId: {}, mediaId: {}", 
                    userPrincipal.getUser().getId(), mediaId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Unable to remove media"));
        }
    }
    
    /**
     * Creates a standardized error response without exposing internal details.
     * 
     * @param message User-friendly error message
     * @return Error response map
     */
    private Map<String, String> createErrorResponse(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
} 