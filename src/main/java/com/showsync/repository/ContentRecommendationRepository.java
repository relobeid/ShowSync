package com.showsync.repository;

import com.showsync.entity.ContentRecommendation;
import com.showsync.entity.recommendation.RecommendationReason;
import com.showsync.entity.recommendation.RecommendationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for ContentRecommendation entity.
 * Provides queries for content recommendation management and analytics.
 */
@Repository
public interface ContentRecommendationRepository extends JpaRepository<ContentRecommendation, Long> {
    
    /**
     * Find active personal recommendations for a user
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.group IS NULL " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now " +
           "ORDER BY r.relevanceScore DESC, r.createdAt DESC")
    Page<ContentRecommendation> findActivePersonalRecommendations(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * Find active group recommendations for a user
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.group.id = :groupId " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now " +
           "ORDER BY r.relevanceScore DESC, r.createdAt DESC")
    Page<ContentRecommendation> findActiveGroupRecommendations(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * Find top recommendations by type
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.recommendationType = :type " +
           "AND r.isDismissed = false " +
           "AND r.isAddedToLibrary = false " +
           "AND r.expiresAt > :now " +
           "AND r.relevanceScore >= :minScore " +
           "ORDER BY r.relevanceScore DESC")
    List<ContentRecommendation> findTopRecommendationsByType(
            @Param("userId") Long userId,
            @Param("type") RecommendationType type,
            @Param("now") LocalDateTime now,
            @Param("minScore") BigDecimal minScore,
            Pageable pageable);
    
    /**
     * Find trending recommendations across platform
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.recommendationType = 'TRENDING' " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now " +
           "GROUP BY r.recommendedMedia.id " +
           "HAVING COUNT(r) >= :minCount " +
           "ORDER BY COUNT(r) DESC, AVG(r.relevanceScore) DESC")
    List<ContentRecommendation> findTrendingRecommendations(
            @Param("now") LocalDateTime now,
            @Param("minCount") Long minCount,
            Pageable pageable);
    
    /**
     * Find unviewed recommendations for a user
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.isViewed = false " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now " +
           "ORDER BY r.relevanceScore DESC")
    List<ContentRecommendation> findUnviewedRecommendations(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);
    
    /**
     * Check if user already has recommendation for specific media
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.recommendedMedia.id = :mediaId " +
           "AND r.expiresAt > :now")
    Optional<ContentRecommendation> findActiveRecommendationForUserAndMedia(
            @Param("userId") Long userId,
            @Param("mediaId") Long mediaId,
            @Param("now") LocalDateTime now);
    
    /**
     * Find recommendations with positive feedback for learning
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.userFeedback >= 4 " +
           "AND r.createdAt >= :since " +
           "ORDER BY r.userFeedback DESC, r.relevanceScore DESC")
    List<ContentRecommendation> findPositiveFeedbackRecommendations(
            @Param("since") LocalDateTime since);
    
    /**
     * Find recommendations with negative feedback for improvement
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.userFeedback <= 2 " +
           "AND r.createdAt >= :since " +
           "ORDER BY r.userFeedback ASC")
    List<ContentRecommendation> findNegativeFeedbackRecommendations(
            @Param("since") LocalDateTime since);
    
    /**
     * Get recommendation success metrics by type
     */
    @Query("SELECT r.recommendationType, " +
           "COUNT(r) as total, " +
           "SUM(CASE WHEN r.isViewed = true THEN 1 ELSE 0 END) as viewed, " +
           "SUM(CASE WHEN r.isAddedToLibrary = true THEN 1 ELSE 0 END) as added, " +
           "SUM(CASE WHEN r.isDismissed = true THEN 1 ELSE 0 END) as dismissed, " +
           "AVG(r.relevanceScore) as avgScore, " +
           "AVG(r.userFeedback) as avgFeedback " +
           "FROM ContentRecommendation r " +
           "WHERE r.createdAt >= :since " +
           "GROUP BY r.recommendationType " +
           "ORDER BY r.recommendationType")
    List<Object[]> getMetricsByType(@Param("since") LocalDateTime since);
    
    /**
     * Get most successful reason codes
     */
    @Query("SELECT r.reasonCode, " +
           "COUNT(r) as total, " +
           "SUM(CASE WHEN r.isAddedToLibrary = true THEN 1 ELSE 0 END) as added, " +
           "(SUM(CASE WHEN r.isAddedToLibrary = true THEN 1 ELSE 0 END) * 1.0 / COUNT(r)) as successRate " +
           "FROM ContentRecommendation r " +
           "WHERE r.createdAt >= :since " +
           "GROUP BY r.reasonCode " +
           "HAVING COUNT(r) >= :minCount " +
           "ORDER BY successRate DESC")
    List<Object[]> getMostSuccessfulReasonCodes(
            @Param("since") LocalDateTime since,
            @Param("minCount") Long minCount);
    
    /**
     * Find recommendations for a specific media (for analytics)
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.recommendedMedia.id = :mediaId " +
           "AND r.createdAt >= :since " +
           "ORDER BY r.relevanceScore DESC")
    List<ContentRecommendation> findRecommendationsForMedia(
            @Param("mediaId") Long mediaId,
            @Param("since") LocalDateTime since);
    
    /**
     * Mark recommendation as viewed
     */
    @Modifying
    @Query("UPDATE ContentRecommendation r " +
           "SET r.isViewed = true " +
           "WHERE r.id = :recommendationId")
    void markAsViewed(@Param("recommendationId") Long recommendationId);
    
    /**
     * Mark recommendation as added to library
     */
    @Modifying
    @Query("UPDATE ContentRecommendation r " +
           "SET r.isAddedToLibrary = true, r.isViewed = true " +
           "WHERE r.id = :recommendationId")
    void markAsAddedToLibrary(@Param("recommendationId") Long recommendationId);
    
    /**
     * Mark recommendation as dismissed
     */
    @Modifying
    @Query("UPDATE ContentRecommendation r " +
           "SET r.isDismissed = true " +
           "WHERE r.id = :recommendationId")
    void markAsDismissed(@Param("recommendationId") Long recommendationId);
    
    /**
     * Set user feedback for recommendation
     */
    @Modifying
    @Query("UPDATE ContentRecommendation r " +
           "SET r.userFeedback = :feedback, r.isViewed = true " +
           "WHERE r.id = :recommendationId")
    void setUserFeedback(
            @Param("recommendationId") Long recommendationId,
            @Param("feedback") Integer feedback);
    
    /**
     * Clean up expired recommendations
     */
    @Modifying
    @Query("DELETE FROM ContentRecommendation r " +
           "WHERE r.expiresAt < :cutoffDate")
    void deleteExpiredRecommendations(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count active recommendations for a user
     */
    @Query("SELECT COUNT(r) FROM ContentRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now")
    long countActiveRecommendationsForUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);
    
    /**
     * Find recommendations expiring soon
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.expiresAt BETWEEN :now AND :cutoff " +
           "AND r.isDismissed = false " +
           "AND r.isAddedToLibrary = false " +
           "ORDER BY r.expiresAt ASC")
    List<ContentRecommendation> findExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("cutoff") LocalDateTime cutoff);
    
    /**
     * Get average feedback score by recommendation type
     */
    @Query("SELECT r.recommendationType, AVG(r.userFeedback) " +
           "FROM ContentRecommendation r " +
           "WHERE r.userFeedback IS NOT NULL " +
           "AND r.createdAt >= :since " +
           "GROUP BY r.recommendationType " +
           "ORDER BY AVG(r.userFeedback) DESC")
    List<Object[]> getAverageFeedbackByType(@Param("since") LocalDateTime since);
    
    /**
     * Find similar successful recommendations (for pattern learning)
     */
    @Query("SELECT r FROM ContentRecommendation r " +
           "WHERE r.reasonCode = :reasonCode " +
           "AND r.sourceMedia.id = :sourceMediaId " +
           "AND (r.isAddedToLibrary = true OR r.userFeedback >= 4) " +
           "AND r.user.id != :excludeUserId " +
           "ORDER BY r.relevanceScore DESC")
    List<ContentRecommendation> findSimilarSuccessfulRecommendations(
            @Param("reasonCode") RecommendationReason reasonCode,
            @Param("sourceMediaId") Long sourceMediaId,
            @Param("excludeUserId") Long excludeUserId,
            Pageable pageable);
} 