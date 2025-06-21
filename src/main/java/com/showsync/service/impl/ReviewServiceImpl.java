package com.showsync.service.impl;

import com.showsync.dto.review.CreateReviewRequest;
import com.showsync.dto.review.MediaDetailsResponse;
import com.showsync.dto.review.ReviewResponse;
import com.showsync.entity.*;
import com.showsync.repository.*;
import com.showsync.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ReviewService for managing reviews and media details.
 * Handles review CRUD operations, voting, statistics, and trending calculations.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final ReviewVoteRepository reviewVoteRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final UserMediaInteractionRepository userMediaInteractionRepository;
    
    @Override
    @Transactional
    public Review createReview(Long userId, CreateReviewRequest request) {
        log.info("Creating review - userId: {}, mediaId: {}", userId, request.getMediaId());
        
        // Validate inputs
        validateUserId(userId);
        validateMediaId(request.getMediaId());
        
        // Check if user already reviewed this media
        if (reviewRepository.existsByUserIdAndMediaId(userId, request.getMediaId())) {
            throw new IllegalArgumentException("User has already reviewed this media");
        }
        
        // Get user and media entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Media media = mediaRepository.findById(request.getMediaId())
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
        
        // Create review
        Review review = new Review();
        review.setUser(user);
        review.setMedia(media);
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setSpoiler(request.isSpoiler());
        
        Review savedReview = reviewRepository.save(review);
        log.info("Review created successfully - reviewId: {}", savedReview.getId());
        
        // Update media statistics asynchronously
        updateMediaStatistics(request.getMediaId());
        
        return savedReview;
    }
    
    @Override
    @Transactional
    public Review updateReview(Long userId, Long reviewId, CreateReviewRequest request) {
        log.info("Updating review - userId: {}, reviewId: {}", userId, reviewId);
        
        validateUserId(userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            log.warn("Security violation: User {} attempted to update review belonging to user {}", 
                    userId, review.getUser().getId());
            throw new SecurityException("User can only update their own reviews");
        }
        
        // Update fields
        review.setTitle(request.getTitle());
        review.setContent(request.getContent());
        review.setRating(request.getRating());
        review.setSpoiler(request.isSpoiler());
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Review updated successfully - reviewId: {}", updatedReview.getId());
        
        // Update media statistics
        updateMediaStatistics(review.getMedia().getId());
        
        return updatedReview;
    }
    
    @Override
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {
        log.info("Deleting review - userId: {}, reviewId: {}", userId, reviewId);
        
        validateUserId(userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Check ownership
        if (!review.getUser().getId().equals(userId)) {
            log.warn("Security violation: User {} attempted to delete review belonging to user {}", 
                    userId, review.getUser().getId());
            throw new SecurityException("User can only delete their own reviews");
        }
        
        Long mediaId = review.getMedia().getId();
        
        // Delete associated votes first
        reviewVoteRepository.deleteByReviewId(reviewId);
        
        // Delete the review
        reviewRepository.delete(review);
        
        log.info("Review deleted successfully - reviewId: {}", reviewId);
        
        // Update media statistics
        updateMediaStatistics(mediaId);
    }
    
    @Override
    @Transactional
    public Review voteOnReview(Long userId, Long reviewId, boolean isHelpful) {
        log.info("Voting on review - userId: {}, reviewId: {}, isHelpful: {}", userId, reviewId, isHelpful);
        
        validateUserId(userId);
        
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
        
        // Prevent users from voting on their own reviews
        if (review.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Users cannot vote on their own reviews");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Check if user already voted
        Optional<ReviewVote> existingVote = reviewVoteRepository.findByUserIdAndReviewId(userId, reviewId);
        
        if (existingVote.isPresent()) {
            // Update existing vote
            ReviewVote vote = existingVote.get();
            boolean wasHelpful = vote.isHelpful();
            vote.setHelpful(isHelpful);
            reviewVoteRepository.save(vote);
            
            // Update vote counts
            if (wasHelpful != isHelpful) {
                if (isHelpful) {
                    review.setHelpfulVotes(review.getHelpfulVotes() + 1);
                } else {
                    review.setHelpfulVotes(review.getHelpfulVotes() - 1);
                }
            }
        } else {
            // Create new vote
            ReviewVote vote = isHelpful ? 
                    ReviewVote.createHelpfulVote(user, review) : 
                    ReviewVote.createNotHelpfulVote(user, review);
            reviewVoteRepository.save(vote);
            
            // Update vote counts
            review.setTotalVotes(review.getTotalVotes() + 1);
            if (isHelpful) {
                review.setHelpfulVotes(review.getHelpfulVotes() + 1);
            }
        }
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Vote recorded successfully - reviewId: {}, helpful: {}, total: {}", 
                reviewId, updatedReview.getHelpfulVotes(), updatedReview.getTotalVotes());
        
        return updatedReview;
    }
    
    @Override
    @Transactional
    public Review removeVoteOnReview(Long userId, Long reviewId) {
        log.info("Removing vote on review - userId: {}, reviewId: {}", userId, reviewId);
        
        validateUserId(userId);
        
        ReviewVote vote = reviewVoteRepository.findByUserIdAndReviewId(userId, reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Vote not found"));
        
        Review review = vote.getReview();
        boolean wasHelpful = vote.isHelpful();
        
        // Remove the vote
        reviewVoteRepository.delete(vote);
        
        // Update vote counts
        review.setTotalVotes(review.getTotalVotes() - 1);
        if (wasHelpful) {
            review.setHelpfulVotes(review.getHelpfulVotes() - 1);
        }
        
        Review updatedReview = reviewRepository.save(review);
        log.info("Vote removed successfully - reviewId: {}, helpful: {}, total: {}", 
                reviewId, updatedReview.getHelpfulVotes(), updatedReview.getTotalVotes());
        
        return updatedReview;
    }
    
    // Continue with other methods...
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
    
    private void validateMediaId(Long mediaId) {
        if (mediaId == null || mediaId <= 0) {
            throw new IllegalArgumentException("Invalid media ID");
        }
    }
    
    // Placeholder implementations for remaining methods - will be completed next
    
    @Override
    public MediaDetailsResponse getMediaDetails(Long mediaId, Long currentUserId) {
        log.info("Getting media details - mediaId: {}, currentUserId: {}", mediaId, currentUserId);
        
        validateMediaId(mediaId);
        
        Media media = mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found"));
        
        // Get review statistics
        long reviewCount = reviewRepository.countByMediaIdNotModerated(mediaId);
        Double averageRating = reviewRepository.getAverageRatingByMediaId(mediaId);
        
        // Get user's review if authenticated
        Optional<Review> userReview = currentUserId != null ? 
                reviewRepository.findByUserIdAndMediaId(currentUserId, mediaId) : 
                Optional.empty();
        
        // Get recent reviews
        PageRequest recentReviewsPageable = PageRequest.of(0, 3);
        Page<Review> recentReviews = reviewRepository.findByMediaIdNotModerated(mediaId, recentReviewsPageable);
        
        // Build response
        MediaDetailsResponse response = new MediaDetailsResponse();
        response.setId(media.getId());
        response.setType(media.getType().toString());
        response.setTitle(media.getTitle());
        response.setDescription(media.getDescription());
        response.setPosterUrl(media.getPosterUrl());
        response.setBackdropUrl(media.getBackdropUrl());
        response.setReleaseDate(media.getReleaseDate());
        response.setExternalId(media.getExternalId());
        response.setExternalSource(media.getExternalSource());
        
        // Set statistics
        MediaDetailsResponse.MediaStatistics stats = new MediaDetailsResponse.MediaStatistics();
        stats.setTotalReviews(reviewCount);
        stats.setAverageRating(averageRating != null ? averageRating : 0.0);
        response.setStatistics(stats);
        
        // Set recent reviews
        List<ReviewResponse> recentReviewResponses = recentReviews.getContent().stream()
                .map(review -> convertToReviewResponse(review, currentUserId))
                .collect(java.util.stream.Collectors.toList());
        response.setRecentReviews(recentReviewResponses);
        
        log.info("Media details retrieved - mediaId: {}, reviewCount: {}, avgRating: {}", 
                mediaId, reviewCount, averageRating);
        
        return response;
    }
    
    @Override
    public Page<ReviewResponse> getReviewsForMedia(Long mediaId, Long currentUserId, Pageable pageable) {
        validateMediaId(mediaId);
        
        Page<Review> reviews = reviewRepository.findByMediaIdNotModerated(mediaId, pageable);
        return reviews.map(review -> convertToReviewResponse(review, currentUserId));
    }
    
    @Override
    public Page<ReviewResponse> getMostHelpfulReviews(Long mediaId, Long currentUserId, Pageable pageable) {
        validateMediaId(mediaId);
        
        Page<Review> reviews = reviewRepository.findMostHelpfulReviews(mediaId, 3, pageable);
        return reviews.map(review -> convertToReviewResponse(review, currentUserId));
    }
    
    @Override
    public Page<ReviewResponse> getReviewsByUser(Long userId, Long currentUserId, Pageable pageable) {
        // TODO: Implement in next part
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public ReviewResponse getReviewById(Long reviewId, Long currentUserId) {
        // TODO: Implement in next part
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public List<MediaDetailsResponse.MediaStatistics> getTrendingMedia(Pageable pageable) {
        // TODO: Implement in next part
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    @Override
    public void updateMediaStatistics(Long mediaId) {
        // TODO: Implement in next part
        log.debug("Media statistics update placeholder for mediaId: {}", mediaId);
    }
    
    @Override
    public Optional<Review> getUserReviewForMedia(Long userId, Long mediaId) {
        return reviewRepository.findByUserIdAndMediaId(userId, mediaId);
    }
    
    @Override
    public boolean hasUserReviewedMedia(Long userId, Long mediaId) {
        return reviewRepository.existsByUserIdAndMediaId(userId, mediaId);
    }
    
    /**
     * Convert Review entity to ReviewResponse DTO.
     */
    private ReviewResponse convertToReviewResponse(Review review, Long currentUserId) {
        ReviewResponse response = new ReviewResponse();
        response.setId(review.getId());
        response.setTitle(review.getTitle());
        response.setContent(review.getContent());
        response.setRating(review.getRating());
        response.setSpoiler(review.isSpoiler());
        response.setHelpfulVotes(review.getHelpfulVotes());
        response.setTotalVotes(review.getTotalVotes());
        response.setHelpfulnessRatio(review.getHelpfulnessRatio());
        response.setCreatedAt(review.getCreatedAt());
        response.setUpdatedAt(review.getUpdatedAt());
        
        // Set user info
        ReviewResponse.UserInfo userInfo = new ReviewResponse.UserInfo();
        userInfo.setId(review.getUser().getId());
        userInfo.setUsername(review.getUser().getUsername());
        userInfo.setDisplayName(review.getUser().getDisplayName());
        response.setUser(userInfo);
        
        // Set media info
        ReviewResponse.MediaInfo mediaInfo = new ReviewResponse.MediaInfo();
        mediaInfo.setId(review.getMedia().getId());
        mediaInfo.setTitle(review.getMedia().getTitle());
        mediaInfo.setType(review.getMedia().getType().toString());
        response.setMedia(mediaInfo);
        
        // Check if current user voted on this review
        if (currentUserId != null) {
            Optional<ReviewVote> userVoteEntity = reviewVoteRepository.findByUserIdAndReviewId(currentUserId, review.getId());
            if (userVoteEntity.isPresent()) {
                ReviewResponse.UserVote userVote = new ReviewResponse.UserVote();
                userVote.setHelpful(userVoteEntity.get().isHelpful());
                userVote.setVotedAt(userVoteEntity.get().getCreatedAt());
                response.setUserVote(userVote);
            }
        }
        
        return response;
    }
} 