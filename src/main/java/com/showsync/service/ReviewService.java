package com.showsync.service;

import com.showsync.dto.review.CreateReviewRequest;
import com.showsync.dto.review.MediaDetailsResponse;
import com.showsync.dto.review.ReviewResponse;
import com.showsync.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing reviews and media details.
 * Handles review creation, voting, statistics, and trending calculations.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
public interface ReviewService {
    
    /**
     * Creates a new review for a media item.
     * 
     * @param userId The authenticated user's ID
     * @param request Review creation request
     * @return Created review
     * @throws IllegalArgumentException if user already reviewed this media or media not found
     * @throws SecurityException if user not authorized
     */
    Review createReview(Long userId, CreateReviewRequest request);
    
    /**
     * Updates an existing review.
     * 
     * @param userId The authenticated user's ID
     * @param reviewId The review ID to update
     * @param request Updated review data
     * @return Updated review
     * @throws IllegalArgumentException if review not found
     * @throws SecurityException if user doesn't own the review
     */
    Review updateReview(Long userId, Long reviewId, CreateReviewRequest request);
    
    /**
     * Deletes a review.
     * 
     * @param userId The authenticated user's ID
     * @param reviewId The review ID to delete
     * @throws IllegalArgumentException if review not found
     * @throws SecurityException if user doesn't own the review
     */
    void deleteReview(Long userId, Long reviewId);
    
    /**
     * Votes on a review (helpful or not helpful).
     * 
     * @param userId The authenticated user's ID
     * @param reviewId The review ID to vote on
     * @param isHelpful Whether the vote is helpful
     * @return Updated review with new vote counts
     * @throws IllegalArgumentException if review not found or user voting on own review
     */
    Review voteOnReview(Long userId, Long reviewId, boolean isHelpful);
    
    /**
     * Removes a user's vote on a review.
     * 
     * @param userId The authenticated user's ID
     * @param reviewId The review ID
     * @return Updated review with adjusted vote counts
     * @throws IllegalArgumentException if vote not found
     */
    Review removeVoteOnReview(Long userId, Long reviewId);
    
    /**
     * Gets detailed media information including reviews and statistics.
     * 
     * @param mediaId The media ID
     * @param currentUserId The current user's ID (optional, for user-specific data)
     * @return Detailed media information
     * @throws IllegalArgumentException if media not found
     */
    MediaDetailsResponse getMediaDetails(Long mediaId, Long currentUserId);
    
    /**
     * Gets paginated reviews for a media item.
     * 
     * @param mediaId The media ID
     * @param currentUserId The current user's ID (optional, for vote information)
     * @param pageable Pagination parameters
     * @return Page of reviews
     */
    Page<ReviewResponse> getReviewsForMedia(Long mediaId, Long currentUserId, Pageable pageable);
    
    /**
     * Gets the most helpful reviews for a media item.
     * 
     * @param mediaId The media ID
     * @param currentUserId The current user's ID (optional)
     * @param pageable Pagination parameters
     * @return Page of helpful reviews
     */
    Page<ReviewResponse> getMostHelpfulReviews(Long mediaId, Long currentUserId, Pageable pageable);
    
    /**
     * Gets reviews written by a specific user.
     * 
     * @param userId The user ID
     * @param currentUserId The current user's ID (optional, for vote information)
     * @param pageable Pagination parameters
     * @return Page of user's reviews
     */
    Page<ReviewResponse> getReviewsByUser(Long userId, Long currentUserId, Pageable pageable);
    
    /**
     * Gets a specific review by ID.
     * 
     * @param reviewId The review ID
     * @param currentUserId The current user's ID (optional, for vote information)
     * @return Review information
     * @throws IllegalArgumentException if review not found
     */
    ReviewResponse getReviewById(Long reviewId, Long currentUserId);
    
    /**
     * Gets trending media based on recent review activity.
     * 
     * @param pageable Pagination parameters
     * @return List of trending media with basic information
     */
    List<MediaDetailsResponse.MediaStatistics> getTrendingMedia(Pageable pageable);
    
    /**
     * Updates media statistics based on reviews and library interactions.
     * This method should be called periodically or triggered by events.
     * 
     * @param mediaId The media ID to update statistics for
     */
    void updateMediaStatistics(Long mediaId);
    
    /**
     * Gets a user's review for a specific media item.
     * 
     * @param userId The user ID
     * @param mediaId The media ID
     * @return Optional review
     */
    Optional<Review> getUserReviewForMedia(Long userId, Long mediaId);
    
    /**
     * Checks if a user has already reviewed a media item.
     * 
     * @param userId The user ID
     * @param mediaId The media ID
     * @return true if user has reviewed this media
     */
    boolean hasUserReviewedMedia(Long userId, Long mediaId);
} 