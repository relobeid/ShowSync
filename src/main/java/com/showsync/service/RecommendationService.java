package com.showsync.service;

import com.showsync.dto.recommendation.*;
import com.showsync.entity.recommendation.RecommendationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * Service interface for managing AI-powered recommendations.
 * Provides both real-time and pre-calculated recommendation retrieval.
 */
public interface RecommendationService {
    
    // === PERSONAL CONTENT RECOMMENDATIONS ===
    
    /**
     * Get personalized content recommendations for a user
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of content recommendations
     */
    Page<ContentRecommendationResponse> getPersonalRecommendations(Long userId, Pageable pageable);
    
    /**
     * Get real-time content recommendations based on current context
     * @param userId User ID
     * @param contextMediaId Media the user is currently viewing (optional)
     * @param limit Maximum number of recommendations
     * @return List of content recommendations
     */
    List<ContentRecommendationResponse> getRealTimeRecommendations(Long userId, Long contextMediaId, int limit);
    
    /**
     * Get trending content recommendations across the platform
     * @param userId User ID (for personalization)
     * @param limit Maximum number of recommendations
     * @return List of trending content recommendations
     */
    List<ContentRecommendationResponse> getTrendingRecommendations(Long userId, int limit);
    
    // === GROUP RECOMMENDATIONS ===
    
    /**
     * Get group suggestions for a user
     * @param userId User ID
     * @param pageable Pagination parameters
     * @return Page of group recommendations
     */
    Page<GroupRecommendationResponse> getGroupRecommendations(Long userId, Pageable pageable);
    
    /**
     * Get group recommendations for content discovery within a group
     * @param userId User ID
     * @param groupId Group ID
     * @param pageable Pagination parameters
     * @return Page of content recommendations for the group
     */
    Page<ContentRecommendationResponse> getGroupContentRecommendations(Long userId, Long groupId, Pageable pageable);
    
    // === RECOMMENDATION MANAGEMENT ===
    
    /**
     * Mark a recommendation as viewed by the user
     * @param userId User ID
     * @param recommendationType Type of recommendation (GROUP or CONTENT)
     * @param recommendationId Recommendation ID
     */
    void markRecommendationAsViewed(Long userId, String recommendationType, Long recommendationId);
    
    /**
     * Dismiss a recommendation (user not interested)
     * @param userId User ID
     * @param recommendationType Type of recommendation
     * @param recommendationId Recommendation ID
     * @param reason Optional reason for dismissal
     */
    void dismissRecommendation(Long userId, String recommendationType, Long recommendationId, String reason);
    
    /**
     * Record positive feedback when user acts on recommendation
     * @param userId User ID
     * @param recommendationType Type of recommendation
     * @param recommendationId Recommendation ID
     * @param actionTaken Action taken (JOINED_GROUP, ADDED_TO_LIBRARY, etc.)
     */
    void recordPositiveFeedback(Long userId, String recommendationType, Long recommendationId, String actionTaken);
    
    /**
     * Submit explicit user feedback on recommendation quality
     * @param userId User ID
     * @param recommendationType Type of recommendation
     * @param recommendationId Recommendation ID
     * @param rating Rating from 1-5
     * @param feedbackText Optional text feedback
     */
    void submitFeedback(Long userId, String recommendationType, Long recommendationId, 
                       int rating, String feedbackText);
    
    // === BATCH RECOMMENDATION GENERATION ===
    
    /**
     * Generate fresh recommendations for a specific user (async)
     * @param userId User ID
     * @return Number of recommendations generated
     */
    int generateRecommendationsForUser(Long userId);
    
    /**
     * Generate recommendations for all active users (scheduled task)
     * @return Summary of generation results
     */
    RecommendationGenerationSummary generateRecommendationsForAllUsers();
    
    /**
     * Refresh recommendations for users with recent activity
     * @param hoursBack How many hours back to consider "recent"
     * @return Number of users processed
     */
    int refreshRecommendationsForActiveUsers(int hoursBack);
    
    // === ANALYTICS & INSIGHTS ===
    
    /**
     * Get recommendation performance metrics
     * @param days Number of days to analyze
     * @return Analytics data
     */
    RecommendationAnalytics getRecommendationAnalytics(int days);
    
    /**
     * Get user's recommendation statistics and preferences
     * @param userId User ID
     * @return User recommendation insights
     */
    UserRecommendationInsights getUserRecommendationInsights(Long userId);
    
    /**
     * Get recommendations summary for user dashboard
     * @param userId User ID
     * @return Quick summary of unviewed/actionable recommendations
     */
    RecommendationSummary getRecommendationSummary(Long userId);
    
    // === PREFERENCE MANAGEMENT ===
    
    /**
     * Update user's preference profile based on recent activity
     * @param userId User ID
     * @return Updated confidence score
     */
    double updateUserPreferences(Long userId);
    
    /**
     * Get user's current preference profile
     * @param userId User ID
     * @return Preference profile data
     */
    UserPreferenceResponse getUserPreferences(Long userId);
    
    /**
     * Calculate compatibility between two users (for group matching)
     * @param userId1 First user ID
     * @param userId2 Second user ID
     * @return Compatibility score (0.0 to 1.0)
     */
    double calculateUserCompatibility(Long userId1, Long userId2);
    
    // === RECOMMENDATION DISCOVERY ===
    
    /**
     * Find similar content based on user's viewing history
     * @param userId User ID
     * @param mediaId Media to find similar content for
     * @param limit Maximum number of similar items
     * @return List of similar content recommendations
     */
    List<ContentRecommendationResponse> findSimilarContent(Long userId, Long mediaId, int limit);
    
    /**
     * Get recommendations by specific type and reason
     * @param userId User ID
     * @param type Recommendation type
     * @param limit Maximum number of recommendations
     * @return Filtered recommendations
     */
    List<ContentRecommendationResponse> getRecommendationsByType(Long userId, RecommendationType type, int limit);
} 