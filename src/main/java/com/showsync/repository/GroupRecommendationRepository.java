package com.showsync.repository;

import com.showsync.entity.GroupRecommendation;
import com.showsync.entity.recommendation.RecommendationReason;
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
 * Repository for GroupRecommendation entity.
 * Provides queries for group recommendation management and retrieval.
 */
@Repository
public interface GroupRecommendationRepository extends JpaRepository<GroupRecommendation, Long> {
    
    /**
     * Find active recommendations for a user (not dismissed, not expired)
     */
    @Query("SELECT r FROM GroupRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now " +
           "ORDER BY r.compatibilityScore DESC, r.createdAt DESC")
    Page<GroupRecommendation> findActiveRecommendationsForUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            Pageable pageable);
    
    /**
     * Find top recommendations for a user (best compatibility scores)
     */
    @Query("SELECT r FROM GroupRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.isDismissed = false " +
           "AND r.isJoined = false " +
           "AND r.expiresAt > :now " +
           "AND r.compatibilityScore >= :minScore " +
           "ORDER BY r.compatibilityScore DESC")
    List<GroupRecommendation> findTopRecommendationsForUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now,
            @Param("minScore") BigDecimal minScore,
            Pageable pageable);
    
    /**
     * Find unviewed recommendations for a user
     */
    @Query("SELECT r FROM GroupRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.isViewed = false " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now " +
           "ORDER BY r.compatibilityScore DESC")
    List<GroupRecommendation> findUnviewedRecommendations(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);
    
    /**
     * Check if user already has recommendation for specific group
     */
    @Query("SELECT r FROM GroupRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.recommendedGroup.id = :groupId " +
           "AND r.expiresAt > :now")
    Optional<GroupRecommendation> findActiveRecommendationForUserAndGroup(
            @Param("userId") Long userId,
            @Param("groupId") Long groupId,
            @Param("now") LocalDateTime now);
    
    /**
     * Find recommendations expiring soon
     */
    @Query("SELECT r FROM GroupRecommendation r " +
           "WHERE r.expiresAt BETWEEN :now AND :cutoff " +
           "AND r.isDismissed = false " +
           "AND r.isJoined = false " +
           "ORDER BY r.expiresAt ASC")
    List<GroupRecommendation> findExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("cutoff") LocalDateTime cutoff);
    
    /**
     * Find recommendations by reason code for analysis
     */
    @Query("SELECT r FROM GroupRecommendation r " +
           "WHERE r.reasonCode = :reasonCode " +
           "AND r.createdAt >= :since " +
           "ORDER BY r.compatibilityScore DESC")
    List<GroupRecommendation> findByReasonCode(
            @Param("reasonCode") RecommendationReason reasonCode,
            @Param("since") LocalDateTime since);
    
    /**
     * Get recommendation success metrics
     */
    @Query("SELECT " +
           "COUNT(r), " +
           "SUM(CASE WHEN r.isViewed = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.isJoined = true THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN r.isDismissed = true THEN 1 ELSE 0 END), " +
           "AVG(r.compatibilityScore) " +
           "FROM GroupRecommendation r " +
           "WHERE r.createdAt >= :since")
    Object[] getRecommendationMetrics(@Param("since") LocalDateTime since);
    
    /**
     * Get success rate by compatibility score range
     */
    @Query("SELECT " +
           "CASE " +
           "  WHEN r.compatibilityScore >= 0.8 THEN 'HIGH' " +
           "  WHEN r.compatibilityScore >= 0.6 THEN 'MEDIUM' " +
           "  ELSE 'LOW' " +
           "END as scoreRange, " +
           "COUNT(r) as total, " +
           "SUM(CASE WHEN r.isJoined = true THEN 1 ELSE 0 END) as joined " +
           "FROM GroupRecommendation r " +
           "WHERE r.createdAt >= :since " +
           "GROUP BY scoreRange " +
           "ORDER BY scoreRange")
    List<Object[]> getSuccessRateByScore(@Param("since") LocalDateTime since);
    
    /**
     * Mark recommendation as viewed
     */
    @Modifying
    @Query("UPDATE GroupRecommendation r " +
           "SET r.isViewed = true " +
           "WHERE r.id = :recommendationId")
    void markAsViewed(@Param("recommendationId") Long recommendationId);
    
    /**
     * Mark recommendation as joined
     */
    @Modifying
    @Query("UPDATE GroupRecommendation r " +
           "SET r.isJoined = true, r.isViewed = true " +
           "WHERE r.id = :recommendationId")
    void markAsJoined(@Param("recommendationId") Long recommendationId);
    
    /**
     * Mark recommendation as dismissed
     */
    @Modifying
    @Query("UPDATE GroupRecommendation r " +
           "SET r.isDismissed = true " +
           "WHERE r.id = :recommendationId")
    void markAsDismissed(@Param("recommendationId") Long recommendationId);
    
    /**
     * Clean up expired recommendations
     */
    @Modifying
    @Query("DELETE FROM GroupRecommendation r " +
           "WHERE r.expiresAt < :cutoffDate")
    void deleteExpiredRecommendations(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count active recommendations for a user
     */
    @Query("SELECT COUNT(r) FROM GroupRecommendation r " +
           "WHERE r.user.id = :userId " +
           "AND r.isDismissed = false " +
           "AND r.expiresAt > :now")
    long countActiveRecommendationsForUser(
            @Param("userId") Long userId,
            @Param("now") LocalDateTime now);
    
    /**
     * Find recommendations for a specific group (for group analytics)
     */
    @Query("SELECT r FROM GroupRecommendation r " +
           "WHERE r.recommendedGroup.id = :groupId " +
           "AND r.createdAt >= :since " +
           "ORDER BY r.compatibilityScore DESC")
    List<GroupRecommendation> findRecommendationsForGroup(
            @Param("groupId") Long groupId,
            @Param("since") LocalDateTime since);
    
    /**
     * Find most successful recommendations (highest join rate)
     */
    @Query("SELECT r.reasonCode, COUNT(r) as total, " +
           "SUM(CASE WHEN r.isJoined = true THEN 1 ELSE 0 END) as joined " +
           "FROM GroupRecommendation r " +
           "WHERE r.createdAt >= :since " +
           "GROUP BY r.reasonCode " +
           "HAVING COUNT(r) >= :minCount " +
           "ORDER BY (SUM(CASE WHEN r.isJoined = true THEN 1 ELSE 0 END) * 1.0 / COUNT(r)) DESC")
    List<Object[]> getMostSuccessfulReasonCodes(
            @Param("since") LocalDateTime since,
            @Param("minCount") Long minCount);
    
    /**
     * Extend expiration for recommendations
     */
    @Modifying
    @Query("UPDATE GroupRecommendation r " +
           "SET r.expiresAt = :newExpirationDate " +
           "WHERE r.id IN :recommendationIds")
    void extendExpiration(
            @Param("recommendationIds") List<Long> recommendationIds,
            @Param("newExpirationDate") LocalDateTime newExpirationDate);
} 