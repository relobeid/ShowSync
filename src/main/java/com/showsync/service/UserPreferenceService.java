package com.showsync.service;

import com.showsync.dto.recommendation.UserPreferenceResponse;
import com.showsync.entity.UserPreferenceProfile;
import com.showsync.entity.recommendation.ViewingPersonality;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing user preference profiles and taste analysis.
 * Handles the calculation and updating of user preferences for recommendations.
 */
public interface UserPreferenceService {
    
    // === PROFILE MANAGEMENT ===
    
    /**
     * Get or create user preference profile
     * @param userId User ID
     * @return User preference profile
     */
    UserPreferenceProfile getOrCreateUserProfile(Long userId);
    
    /**
     * Update user preferences based on recent activity
     * @param userId User ID
     * @return Updated confidence score
     */
    double updateUserPreferences(Long userId);
    
    /**
     * Force recalculation of user preference profile
     * @param userId User ID
     * @return New confidence score
     */
    double recalculateUserProfile(Long userId);
    
    /**
     * Get user preference profile as DTO
     * @param userId User ID
     * @return User preference response
     */
    UserPreferenceResponse getUserPreferenceResponse(Long userId);
    
    // === PREFERENCE CALCULATION ===
    
    /**
     * Calculate genre preferences from user's media interactions
     * @param userId User ID
     * @return Map of genre to preference score (0.0 to 1.0)
     */
    Map<String, Double> calculateGenrePreferences(Long userId);
    
    /**
     * Calculate platform preferences (Netflix, Disney+, etc.)
     * @param userId User ID
     * @return Map of platform to preference score
     */
    Map<String, Double> calculatePlatformPreferences(Long userId);
    
    /**
     * Calculate era preferences (decades, years)
     * @param userId User ID
     * @return Map of era to preference score
     */
    Map<String, Double> calculateEraPreferences(Long userId);
    
    /**
     * Determine user's viewing personality based on behavior patterns
     * @param userId User ID
     * @return Viewing personality classification
     */
    ViewingPersonality determineViewingPersonality(Long userId);
    
    // === COMPATIBILITY ANALYSIS ===
    
    /**
     * Calculate compatibility between two users
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Compatibility score (0.0 to 1.0)
     */
    double calculateUserCompatibility(Long userId1, Long userId2);
    
    /**
     * Find users with similar taste preferences
     * @param userId User ID
     * @param limit Maximum number of similar users to return
     * @return List of similar user IDs with compatibility scores
     */
    List<UserCompatibility> findSimilarUsers(Long userId, int limit);
    
    /**
     * Calculate user's diversity score (how varied their taste is)
     * @param userId User ID
     * @return Diversity score (0.0 = very focused, 1.0 = very diverse)
     */
    double calculateDiversityScore(Long userId);
    
    // === CONFIDENCE & QUALITY ===
    
    /**
     * Calculate confidence score for user's preference profile
     * @param userId User ID
     * @return Confidence score (0.0 to 1.0)
     */
    double calculateConfidenceScore(Long userId);
    
    /**
     * Check if user has sufficient data for reliable recommendations
     * @param userId User ID
     * @return True if user has enough interaction data
     */
    boolean hasSufficientData(Long userId);
    
    /**
     * Get recommendations for improving user's preference profile
     * @param userId User ID
     * @return List of improvement suggestions
     */
    List<String> getProfileImprovementSuggestions(Long userId);
    
    // === BATCH OPERATIONS ===
    
    /**
     * Update preference profiles for all users with recent activity
     * @param daysBack How many days back to consider "recent"
     * @return Number of profiles updated
     */
    int updateActiveUserProfiles(int daysBack);
    
    /**
     * Recalculate profiles with low confidence scores
     * @param minConfidenceThreshold Minimum confidence to trigger recalculation
     * @return Number of profiles recalculated
     */
    int recalculateLowConfidenceProfiles(double minConfidenceThreshold);
    
    /**
     * Clean up old or inactive preference profiles
     * @param daysInactive Number of days without activity to consider inactive
     * @return Number of profiles cleaned up
     */
    int cleanupInactiveProfiles(int daysInactive);
    
    // === ANALYTICS ===
    
    /**
     * Get overall platform preference analytics
     * @return Map of statistics about user preferences across the platform
     */
    Map<String, Object> getPlatformPreferenceAnalytics();
    
    /**
     * Get most common viewing personalities
     * @return List of viewing personalities with counts
     */
    List<PersonalityCount> getViewingPersonalityDistribution();
    
    /**
     * DTO for user compatibility results
     */
    record UserCompatibility(Long userId, String username, double compatibilityScore) {}
    
    /**
     * DTO for personality distribution analytics
     */
    record PersonalityCount(ViewingPersonality personality, long count, double percentage) {}
} 