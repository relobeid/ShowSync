package com.showsync.repository;

import com.showsync.entity.ReviewVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for ReviewVote entity.
 * Manages votes on reviews with constraints to prevent duplicate voting.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Repository
public interface ReviewVoteRepository extends JpaRepository<ReviewVote, Long> {
    
    /**
     * Find a user's vote on a specific review.
     * 
     * @param userId The user ID
     * @param reviewId The review ID
     * @return Optional vote
     */
    Optional<ReviewVote> findByUserIdAndReviewId(Long userId, Long reviewId);
    
    /**
     * Check if a user has already voted on a review.
     * 
     * @param userId The user ID
     * @param reviewId The review ID
     * @return true if vote exists
     */
    boolean existsByUserIdAndReviewId(Long userId, Long reviewId);
    
    /**
     * Count helpful votes for a review.
     * 
     * @param reviewId The review ID
     * @return Number of helpful votes
     */
    @Query("SELECT COUNT(v) FROM ReviewVote v WHERE v.review.id = :reviewId AND v.isHelpful = true")
    long countHelpfulVotes(@Param("reviewId") Long reviewId);
    
    /**
     * Count total votes for a review.
     * 
     * @param reviewId The review ID
     * @return Total number of votes
     */
    long countByReviewId(Long reviewId);
    
    /**
     * Delete all votes for a review (used when review is deleted).
     * 
     * @param reviewId The review ID
     */
    void deleteByReviewId(Long reviewId);
} 