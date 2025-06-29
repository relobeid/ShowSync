package com.showsync.controller;

import com.showsync.dto.review.CreateReviewRequest;
import com.showsync.dto.review.MediaDetailsResponse;
import com.showsync.dto.review.ReviewResponse;
import com.showsync.entity.Review;
import com.showsync.security.UserPrincipal;
import com.showsync.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for review management and media details.
 * Provides endpoints for creating, updating, deleting reviews, voting, and media statistics.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Review management and media details")
public class ReviewController {
    
    private final ReviewService reviewService;
    
    @PostMapping("/reviews")
    @Operation(summary = "Create a new review", 
               description = "Create a new review for a media item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Review created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data or user already reviewed this media"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<?> createReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Valid @RequestBody CreateReviewRequest request) {
        
        if (userPrincipal == null || userPrincipal.getUser() == null) {
            log.warn("Unauthenticated request to create review for media: {}", request.getMediaId());
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        log.info("Creating review - userId: {}, mediaId: {}", 
                userPrincipal.getUser().getId(), request.getMediaId());
        
        try {
            Review review = reviewService.createReview(userPrincipal.getUser().getId(), request);
            ReviewResponse response = mapToReviewResponse(review, userPrincipal.getUser().getId());
            
            return ResponseEntity.status(201).body(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to create review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error creating review", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/reviews/{reviewId}")
    @Operation(summary = "Get a review by ID", 
               description = "Get a specific review by its ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<?> getReview(
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        log.info("Getting review - reviewId: {}", reviewId);
        
        try {
            Long currentUserId = userPrincipal != null ? userPrincipal.getUser().getId() : null;
            ReviewResponse response = reviewService.getReviewById(reviewId, currentUserId);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Review not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Unexpected error getting review", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }

    @PutMapping("/reviews/{reviewId}")
    @Operation(summary = "Update a review", 
               description = "Update an existing review (only by the author)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Review updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Not authorized to update this review"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<?> updateReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Valid @RequestBody CreateReviewRequest request) {
        
        if (userPrincipal == null || userPrincipal.getUser() == null) {
            log.warn("Unauthenticated request to update review: {}", reviewId);
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        log.info("Updating review - userId: {}, reviewId: {}", 
                userPrincipal.getUser().getId(), reviewId);
        
        try {
            Review review = reviewService.updateReview(userPrincipal.getUser().getId(), reviewId, request);
            ReviewResponse response = mapToReviewResponse(review, userPrincipal.getUser().getId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update review: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Security violation updating review: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error updating review", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @DeleteMapping("/reviews/{reviewId}")
    @Operation(summary = "Delete a review", 
               description = "Delete a review (only by the author)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Review deleted successfully"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "403", description = "Not authorized to delete this review"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<?> deleteReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Review ID") @PathVariable Long reviewId) {
        
        if (userPrincipal == null || userPrincipal.getUser() == null) {
            log.warn("Unauthenticated request to delete review: {}", reviewId);
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        log.info("Deleting review - userId: {}, reviewId: {}", 
                userPrincipal.getUser().getId(), reviewId);
        
        try {
            reviewService.deleteReview(userPrincipal.getUser().getId(), reviewId);
            
            return ResponseEntity.noContent().build();
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete review: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            log.warn("Security violation deleting review: {}", e.getMessage());
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error deleting review", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @PostMapping("/reviews/{reviewId}/vote")
    @Operation(summary = "Vote on a review", 
               description = "Vote on a review as helpful or not helpful")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vote recorded successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid vote or user voting on own review"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<?> voteOnReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Review ID") @PathVariable Long reviewId,
            @Parameter(description = "Whether the vote is helpful") @RequestParam boolean helpful) {
        
        if (userPrincipal == null || userPrincipal.getUser() == null) {
            log.warn("Unauthenticated request to vote on review: {}", reviewId);
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        log.info("Voting on review - userId: {}, reviewId: {}, helpful: {}", 
                userPrincipal.getUser().getId(), reviewId, helpful);
        
        try {
            Review review = reviewService.voteOnReview(userPrincipal.getUser().getId(), reviewId, helpful);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vote recorded successfully");
            response.put("helpfulVotes", review.getHelpfulVotes());
            response.put("totalVotes", review.getTotalVotes());
            response.put("helpfulnessRatio", review.getHelpfulnessRatio());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to vote on review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error voting on review", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @DeleteMapping("/reviews/{reviewId}/vote")
    @Operation(summary = "Remove vote on a review", 
               description = "Remove your vote on a review")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Vote removed successfully"),
        @ApiResponse(responseCode = "400", description = "Vote not found"),
        @ApiResponse(responseCode = "401", description = "Authentication required"),
        @ApiResponse(responseCode = "404", description = "Review not found")
    })
    public ResponseEntity<?> removeVoteOnReview(
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Review ID") @PathVariable Long reviewId) {
        
        if (userPrincipal == null || userPrincipal.getUser() == null) {
            log.warn("Unauthenticated request to remove vote on review: {}", reviewId);
            return ResponseEntity.status(401).body(Map.of("error", "Authentication required"));
        }
        
        log.info("Removing vote on review - userId: {}, reviewId: {}", 
                userPrincipal.getUser().getId(), reviewId);
        
        try {
            Review review = reviewService.removeVoteOnReview(userPrincipal.getUser().getId(), reviewId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Vote removed successfully");
            response.put("helpfulVotes", review.getHelpfulVotes());
            response.put("totalVotes", review.getTotalVotes());
            response.put("helpfulnessRatio", review.getHelpfulnessRatio());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to remove vote on review: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error removing vote on review", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/media/{mediaId}")
    @Operation(summary = "Get detailed media information", 
               description = "Get comprehensive media details including reviews and statistics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Media details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<?> getMediaDetails(
            @Parameter(description = "Media ID") @PathVariable Long mediaId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Long currentUserId = (userPrincipal != null && userPrincipal.getUser() != null) ? userPrincipal.getUser().getId() : null;
        
        log.info("Getting media details - mediaId: {}, currentUserId: {}", mediaId, currentUserId);
        
        try {
            MediaDetailsResponse details = reviewService.getMediaDetails(mediaId, currentUserId);
            return ResponseEntity.ok(details);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get media details: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting media details", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/media/{mediaId}/reviews")
    @Operation(summary = "Get reviews for a media item", 
               description = "Get paginated reviews for a specific media item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<?> getReviewsForMedia(
            @Parameter(description = "Media ID") @PathVariable Long mediaId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort by: created, helpful, rating") @RequestParam(defaultValue = "helpful") String sort) {
        
        Long currentUserId = (userPrincipal != null && userPrincipal.getUser() != null) ? userPrincipal.getUser().getId() : null;
        
        log.info("Getting reviews for media - mediaId: {}, page: {}, size: {}, sort: {}", 
                mediaId, page, size, sort);
        
        try {
            Sort sortBy = createSort(sort);
            Pageable pageable = PageRequest.of(page, size, sortBy);
            
            Page<ReviewResponse> reviews = reviewService.getReviewsForMedia(mediaId, currentUserId, pageable);
            return ResponseEntity.ok(reviews);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get reviews for media: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting reviews for media", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/media/{mediaId}/helpful")
    @Operation(summary = "Get most helpful reviews for media", 
               description = "Get the most helpful reviews for a specific media item")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Most helpful reviews retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Media not found")
    })
    public ResponseEntity<?> getMostHelpfulReviewsForMedia(
            @Parameter(description = "Media ID") @PathVariable Long mediaId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "5") int size) {
        
        Long currentUserId = (userPrincipal != null && userPrincipal.getUser() != null) ? userPrincipal.getUser().getId() : null;
        
        log.info("Getting most helpful reviews for media - mediaId: {}, page: {}, size: {}", 
                mediaId, page, size);
        
        try {
            // Sort by helpful votes descending, then by total votes descending
            Sort sortBy = Sort.by(Sort.Direction.DESC, "helpfulVotes")
                             .and(Sort.by(Sort.Direction.DESC, "totalVotes"));
            Pageable pageable = PageRequest.of(page, size, sortBy);
            
            Page<ReviewResponse> reviews = reviewService.getReviewsForMedia(mediaId, currentUserId, pageable);
            return ResponseEntity.ok(reviews);
            
        } catch (IllegalArgumentException e) {
            log.warn("Failed to get most helpful reviews for media: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error getting most helpful reviews for media", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    @GetMapping("/trending")
    @Operation(summary = "Get trending media", 
               description = "Get trending media based on recent review activity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trending media retrieved successfully")
    })
    public ResponseEntity<?> getTrendingMedia(
            @Parameter(description = "Number of items to return") @RequestParam(defaultValue = "20") int limit) {
        
        log.info("Getting trending media - limit: {}", limit);
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            List<MediaDetailsResponse.MediaStatistics> trending = reviewService.getTrendingMedia(pageable);
            return ResponseEntity.ok(trending);
            
        } catch (Exception e) {
            log.error("Unexpected error getting trending media", e);
            return ResponseEntity.internalServerError().body(Map.of("error", "Internal server error"));
        }
    }
    
    private Sort createSort(String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "created" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "rating" -> Sort.by(Sort.Direction.DESC, "rating");
            case "helpful" -> Sort.by(Sort.Direction.DESC, "helpfulVotes");
            default -> Sort.by(Sort.Direction.DESC, "helpfulVotes");
        };
    }
    
    /**
     * Maps a Review entity to a ReviewResponse DTO.
     * 
     * @param review The review entity to map
     * @param currentUserId The current user's ID (optional, for vote information)
     * @return Mapped ReviewResponse
     */
    private ReviewResponse mapToReviewResponse(Review review, Long currentUserId) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setTitle(review.getTitle());
        response.setContent(review.getContent());
        response.setRating(review.getRating());
        response.setHelpfulVotes(review.getHelpfulVotes());
        response.setTotalVotes(review.getTotalVotes());
        response.setHelpfulnessRatio(review.getHelpfulnessRatio());
        response.setSpoiler(review.isSpoiler());
        response.setModerated(review.isModerated());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        
        // Map user information
        ReviewResponse.UserInfo userInfo = new ReviewResponse.UserInfo();
        userInfo.setId(review.getUser().getId());
        userInfo.setUsername(review.getUser().getUsername());
        userInfo.setDisplayName(review.getUser().getDisplayName());
        userInfo.setProfilePictureUrl(review.getUser().getProfilePictureUrl());
        response.setUser(userInfo);
        
        // Map media information
        ReviewResponse.MediaInfo mediaInfo = new ReviewResponse.MediaInfo();
        mediaInfo.setId(review.getMedia().getId());
        mediaInfo.setType(review.getMedia().getType().toString());
        mediaInfo.setTitle(review.getMedia().getTitle());
        mediaInfo.setPosterUrl(review.getMedia().getPosterUrl());
        // Extract year from release date if available
        if (review.getMedia().getReleaseDate() != null) {
            mediaInfo.setReleaseYear(review.getMedia().getReleaseDate().getYear());
        }
        response.setMedia(mediaInfo);
        
        // Note: User vote information is not included in this simple mapping
        // It would require a separate query to the ReviewVoteRepository
        
        return response;
    }
} 