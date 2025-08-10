package com.showsync.repository;

import com.showsync.entity.RecommendationFeedback;
import com.showsync.entity.recommendation.FeedbackType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for RecommendationFeedback entity.
 * Provides queries for feedback analysis and machine learning improvement.
 */
@Repository
public interface RecommendationFeedbackRepository extends JpaRepository<RecommendationFeedback, Long> {
    
    /**
     * Find all feedback for a specific user
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.user.id = :userId " +
           "ORDER BY f.createdAt DESC")
    Page<RecommendationFeedback> findByUserId(
            @Param("userId") Long userId, 
            Pageable pageable);
    
    /**
     * Find feedback by recommendation type (GROUP or CONTENT)
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.recommendationType = :type " +
           "AND f.createdAt >= :since " +
           "ORDER BY f.createdAt DESC")
    List<RecommendationFeedback> findByRecommendationType(
            @Param("type") String recommendationType,
            @Param("since") LocalDateTime since);
    
    /**
     * Find feedback by type (POSITIVE, NEGATIVE, NEUTRAL)
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.feedbackType = :feedbackType " +
           "AND f.createdAt >= :since " +
           "ORDER BY f.createdAt DESC")
    List<RecommendationFeedback> findByFeedbackType(
            @Param("feedbackType") FeedbackType feedbackType,
            @Param("since") LocalDateTime since);
    
    /**
     * Find positive feedback for learning successful patterns
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.feedbackType = 'POSITIVE' " +
           "AND f.createdAt >= :since " +
           "ORDER BY f.feedbackScore DESC NULLS LAST, f.createdAt DESC")
    List<RecommendationFeedback> findPositiveFeedback(@Param("since") LocalDateTime since);
    
    /**
     * Find negative feedback for improving algorithms
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.feedbackType = 'NEGATIVE' " +
           "AND f.createdAt >= :since " +
           "ORDER BY f.feedbackScore ASC NULLS LAST, f.createdAt DESC")
    List<RecommendationFeedback> findNegativeFeedback(@Param("since") LocalDateTime since);
    
    /**
     * Get feedback statistics by recommendation type
     */
    @Query("SELECT f.recommendationType, f.feedbackType, COUNT(f), AVG(f.feedbackScore) " +
           "FROM RecommendationFeedback f " +
           "WHERE f.createdAt >= :since " +
           "GROUP BY f.recommendationType, f.feedbackType " +
           "ORDER BY f.recommendationType, f.feedbackType")
    List<Object[]> getFeedbackStatistics(@Param("since") LocalDateTime since);
    
    /**
     * Get feedback statistics by action taken
     */
    @Query("SELECT f.actionTaken, COUNT(f), AVG(f.feedbackScore) " +
           "FROM RecommendationFeedback f " +
           "WHERE f.actionTaken IS NOT NULL " +
           "AND f.createdAt >= :since " +
           "GROUP BY f.actionTaken " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> getFeedbackByAction(@Param("since") LocalDateTime since);
    
    /**
     * Find feedback with text comments for qualitative analysis
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.feedbackText IS NOT NULL " +
           "AND LENGTH(TRIM(f.feedbackText)) > 0 " +
           "AND f.createdAt >= :since " +
           "ORDER BY f.createdAt DESC")
    List<RecommendationFeedback> findFeedbackWithText(@Param("since") LocalDateTime since);
    
    /**
     * Find feedback for specific recommendation
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.recommendationType = :type " +
           "AND f.recommendationId = :recommendationId " +
           "ORDER BY f.createdAt DESC")
    List<RecommendationFeedback> findByRecommendation(
            @Param("type") String recommendationType,
            @Param("recommendationId") Long recommendationId);
    
    /**
     * Get average feedback score by user (to identify power users)
     */
    @Query("SELECT f.user.id, f.user.username, COUNT(f), AVG(f.feedbackScore) " +
           "FROM RecommendationFeedback f " +
           "WHERE f.feedbackScore IS NOT NULL " +
           "AND f.createdAt >= :since " +
           "GROUP BY f.user.id, f.user.username " +
           "HAVING COUNT(f) >= :minFeedbackCount " +
           "ORDER BY AVG(f.feedbackScore) DESC")
    List<Object[]> getAverageFeedbackByUser(
            @Param("since") LocalDateTime since,
            @Param("minFeedbackCount") Long minFeedbackCount);
    
    /**
     * Find most common feedback reasons for improvement
     */
    @Query("SELECT f.feedbackReason, f.feedbackType, COUNT(f) " +
           "FROM RecommendationFeedback f " +
           "WHERE f.feedbackReason IS NOT NULL " +
           "AND f.createdAt >= :since " +
           "GROUP BY f.feedbackReason, f.feedbackType " +
           "ORDER BY COUNT(f) DESC")
    List<Object[]> getMostCommonFeedbackReasons(@Param("since") LocalDateTime since);
    
    /**
     * Get feedback trends over time
     */
    @Query("SELECT DATE(f.createdAt), f.feedbackType, COUNT(f) " +
           "FROM RecommendationFeedback f " +
           "WHERE f.createdAt >= :since " +
           "GROUP BY DATE(f.createdAt), f.feedbackType " +
           "ORDER BY DATE(f.createdAt) DESC, f.feedbackType")
    List<Object[]> getFeedbackTrends(@Param("since") LocalDateTime since);
    
    /**
     * Count feedback by user for rate limiting
     */
    @Query("SELECT COUNT(f) FROM RecommendationFeedback f " +
           "WHERE f.user.id = :userId " +
           "AND f.createdAt >= :since")
    long countFeedbackByUserSince(
            @Param("userId") Long userId,
            @Param("since") LocalDateTime since);
    
    /**
     * Find users who provide the most helpful feedback
     */
    @Query("SELECT f.user.id, f.user.username, COUNT(f), " +
           "SUM(CASE WHEN f.feedbackType = 'POSITIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN f.feedbackText IS NOT NULL THEN 1 ELSE 0 END) " +
           "FROM RecommendationFeedback f " +
           "WHERE f.createdAt >= :since " +
           "GROUP BY f.user.id, f.user.username " +
           "HAVING COUNT(f) >= :minCount " +
           "ORDER BY COUNT(f) DESC, " +
           "SUM(CASE WHEN f.feedbackText IS NOT NULL THEN 1 ELSE 0 END) DESC")
    List<Object[]> findMostActiveReviewers(
            @Param("since") LocalDateTime since,
            @Param("minCount") Long minCount);
    
    /**
     * Get conversion rates by feedback type
     */
    @Query("SELECT f.feedbackType, " +
           "COUNT(f) as total, " +
           "SUM(CASE WHEN f.actionTaken IN ('JOINED_GROUP', 'ADDED_TO_LIBRARY') THEN 1 ELSE 0 END) as conversions " +
           "FROM RecommendationFeedback f " +
           "WHERE f.createdAt >= :since " +
           "GROUP BY f.feedbackType " +
           "ORDER BY f.feedbackType")
    List<Object[]> getConversionRatesByFeedbackType(@Param("since") LocalDateTime since);
    
    /**
     * Find recent feedback for real-time analysis
     */
    @Query("SELECT f FROM RecommendationFeedback f " +
           "WHERE f.createdAt >= :since " +
           "ORDER BY f.createdAt DESC")
    List<RecommendationFeedback> findRecentFeedback(
            @Param("since") LocalDateTime since,
            Pageable pageable);
} 