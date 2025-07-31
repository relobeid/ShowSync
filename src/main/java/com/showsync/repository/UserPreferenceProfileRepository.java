package com.showsync.repository;

import com.showsync.entity.UserPreferenceProfile;
import com.showsync.entity.recommendation.ViewingPersonality;
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
 * Repository for UserPreferenceProfile entity.
 * Provides optimized queries for recommendation algorithms.
 */
@Repository
public interface UserPreferenceProfileRepository extends JpaRepository<UserPreferenceProfile, Long> {
    
    /**
     * Find preference profile by user ID
     */
    @Query("SELECT p FROM UserPreferenceProfile p WHERE p.user.id = :userId")
    Optional<UserPreferenceProfile> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find users with sufficient data for recommendations
     */
    @Query("SELECT p FROM UserPreferenceProfile p " +
           "WHERE p.totalInteractions >= :minInteractions " +
           "AND p.confidenceScore >= :minConfidence " +
           "ORDER BY p.confidenceScore DESC")
    List<UserPreferenceProfile> findUsersWithSufficientData(
            @Param("minInteractions") Integer minInteractions,
            @Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * Find users with similar viewing personalities
     */
    @Query("SELECT p FROM UserPreferenceProfile p " +
           "WHERE p.viewingPersonality = :personality " +
           "AND p.user.id != :excludeUserId " +
           "AND p.confidenceScore >= :minConfidence " +
           "ORDER BY p.confidenceScore DESC")
    List<UserPreferenceProfile> findSimilarPersonalities(
            @Param("personality") ViewingPersonality personality,
            @Param("excludeUserId") Long excludeUserId,
            @Param("minConfidence") BigDecimal minConfidence);
    
    /**
     * Find profiles that need recalculation (low confidence or old data)
     */
    @Query("SELECT p FROM UserPreferenceProfile p " +
           "WHERE p.confidenceScore < :minConfidence " +
           "OR p.lastCalculatedAt < :cutoffDate " +
           "ORDER BY p.lastCalculatedAt ASC")
    List<UserPreferenceProfile> findProfilesNeedingUpdate(
            @Param("minConfidence") BigDecimal minConfidence,
            @Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count profiles by viewing personality
     */
    @Query("SELECT p.viewingPersonality, COUNT(p) FROM UserPreferenceProfile p " +
           "WHERE p.viewingPersonality IS NOT NULL " +
           "GROUP BY p.viewingPersonality " +
           "ORDER BY COUNT(p) DESC")
    List<Object[]> countByPersonality();
    
    /**
     * Find users with high completion rates
     */
    @Query("SELECT p FROM UserPreferenceProfile p " +
           "WHERE p.completionRate >= :minCompletionRate " +
           "AND p.totalInteractions >= :minInteractions " +
           "ORDER BY p.completionRate DESC")
    List<UserPreferenceProfile> findActiveUsers(
            @Param("minCompletionRate") BigDecimal minCompletionRate,
            @Param("minInteractions") Integer minInteractions);
    
    /**
     * Find users for collaborative filtering (excluding specific user)
     */
    @Query("SELECT p FROM UserPreferenceProfile p " +
           "WHERE p.user.id != :userId " +
           "AND p.confidenceScore >= :minConfidence " +
           "AND p.totalInteractions >= :minInteractions " +
           "ORDER BY p.confidenceScore DESC, p.totalInteractions DESC")
    List<UserPreferenceProfile> findCandidatesForCollaborativeFiltering(
            @Param("userId") Long userId,
            @Param("minConfidence") BigDecimal minConfidence,
            @Param("minInteractions") Integer minInteractions);
    
    /**
     * Get average stats for platform analytics
     */
    @Query("SELECT " +
           "AVG(p.averageUserRating), " +
           "AVG(p.completionRate), " +
           "AVG(p.totalInteractions), " +
           "AVG(p.confidenceScore) " +
           "FROM UserPreferenceProfile p " +
           "WHERE p.totalInteractions > 0")
    Object[] getPlatformAverages();
    
    /**
     * Update confidence score for a specific user
     */
    @Modifying
    @Query("UPDATE UserPreferenceProfile p " +
           "SET p.confidenceScore = :confidenceScore, " +
           "    p.lastCalculatedAt = :timestamp " +
           "WHERE p.user.id = :userId")
    void updateConfidenceScore(
            @Param("userId") Long userId,
            @Param("confidenceScore") BigDecimal confidenceScore,
            @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Mark profiles for recalculation (reset confidence)
     */
    @Modifying
    @Query("UPDATE UserPreferenceProfile p " +
           "SET p.confidenceScore = 0.0 " +
           "WHERE p.user.id IN :userIds")
    void markForRecalculation(@Param("userIds") List<Long> userIds);
    
    /**
     * Clean up old profiles with no recent activity
     */
    @Modifying
    @Query("DELETE FROM UserPreferenceProfile p " +
           "WHERE p.totalInteractions = 0 " +
           "AND p.createdAt < :cutoffDate")
    void deleteInactiveProfiles(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Check if user has a preference profile
     */
    @Query("SELECT COUNT(p) > 0 FROM UserPreferenceProfile p WHERE p.user.id = :userId")
    boolean existsByUserId(@Param("userId") Long userId);
    
    /**
     * Get profiles with recent activity for real-time recommendations
     */
    @Query("SELECT p FROM UserPreferenceProfile p " +
           "WHERE p.lastCalculatedAt >= :since " +
           "AND p.confidenceScore >= :minConfidence " +
           "ORDER BY p.lastCalculatedAt DESC")
    List<UserPreferenceProfile> findRecentlyActiveProfiles(
            @Param("since") LocalDateTime since,
            @Param("minConfidence") BigDecimal minConfidence);
} 