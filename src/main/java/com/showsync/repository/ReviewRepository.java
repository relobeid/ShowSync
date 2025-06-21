package com.showsync.repository;

import com.showsync.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Review entity.
 * Provides methods for managing user reviews with advanced querying capabilities.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    /**
     * Find all reviews for a specific media item, excluding moderated reviews.
     * 
     * @param mediaId The media ID
     * @param pageable Pagination parameters
     * @return Page of reviews
     */
    @Query("SELECT r FROM Review r WHERE r.media.id = :mediaId AND r.isModerated = false ORDER BY r.helpfulVotes DESC, r.createdAt DESC")
    Page<Review> findByMediaIdNotModerated(@Param("mediaId") Long mediaId, Pageable pageable);
    
    /**
     * Find all reviews by a specific user.
     * 
     * @param userId The user ID
     * @param pageable Pagination parameters
     * @return Page of reviews
     */
    Page<Review> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    /**
     * Find a specific review by user and media.
     * 
     * @param userId The user ID
     * @param mediaId The media ID
     * @return Optional review
     */
    Optional<Review> findByUserIdAndMediaId(Long userId, Long mediaId);
    
    /**
     * Check if a user has already reviewed a media item.
     * 
     * @param userId The user ID
     * @param mediaId The media ID
     * @return true if review exists
     */
    boolean existsByUserIdAndMediaId(Long userId, Long mediaId);
    
    /**
     * Find the most helpful reviews for a media item.
     * 
     * @param mediaId The media ID
     * @param minVotes Minimum number of votes required
     * @param pageable Pagination parameters
     * @return Page of helpful reviews
     */
    @Query("SELECT r FROM Review r WHERE r.media.id = :mediaId AND r.totalVotes >= :minVotes AND r.isModerated = false ORDER BY (CAST(r.helpfulVotes AS double) / r.totalVotes) DESC, r.helpfulVotes DESC")
    Page<Review> findMostHelpfulReviews(@Param("mediaId") Long mediaId, @Param("minVotes") int minVotes, Pageable pageable);
    
    /**
     * Find recent reviews for trending analysis.
     * 
     * @param since Only reviews created after this date
     * @param pageable Pagination parameters
     * @return Page of recent reviews
     */
    @Query("SELECT r FROM Review r WHERE r.createdAt >= :since AND r.isModerated = false ORDER BY r.helpfulVotes DESC, r.createdAt DESC")
    Page<Review> findRecentHelpfulReviews(@Param("since") LocalDateTime since, Pageable pageable);
    
    /**
     * Get media statistics for trending calculations.
     * 
     * @param since Only reviews created after this date
     * @return List of media IDs with review counts and average ratings
     */
    @Query("SELECT r.media.id, COUNT(r), AVG(CAST(r.rating AS double)), SUM(r.helpfulVotes) " +
           "FROM Review r WHERE r.createdAt >= :since AND r.isModerated = false AND r.rating IS NOT NULL " +
           "GROUP BY r.media.id HAVING COUNT(r) >= 2 ORDER BY COUNT(r) DESC, AVG(CAST(r.rating AS double)) DESC")
    List<Object[]> getMediaStatistics(@Param("since") LocalDateTime since);
    
    /**
     * Count total reviews for a media item (excluding moderated).
     * 
     * @param mediaId The media ID
     * @return Number of reviews
     */
    @Query("SELECT COUNT(r) FROM Review r WHERE r.media.id = :mediaId AND r.isModerated = false")
    long countByMediaIdNotModerated(@Param("mediaId") Long mediaId);
    
    /**
     * Get average rating for a media item from reviews.
     * 
     * @param mediaId The media ID
     * @return Average rating or null if no ratings
     */
    @Query("SELECT AVG(CAST(r.rating AS double)) FROM Review r WHERE r.media.id = :mediaId AND r.rating IS NOT NULL AND r.isModerated = false")
    Double getAverageRatingByMediaId(@Param("mediaId") Long mediaId);
    
    /**
     * Find reviews flagged for moderation.
     * 
     * @param pageable Pagination parameters
     * @return Page of flagged reviews
     */
    Page<Review> findByIsModeratedTrueOrderByCreatedAtDesc(Pageable pageable);
} 