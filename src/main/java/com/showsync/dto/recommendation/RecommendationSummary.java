package com.showsync.dto.recommendation;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO containing a quick summary of recommendations for user dashboards.
 * Provides essential information without overwhelming details.
 */
@Data
public class RecommendationSummary {
    
    // User context
    private Long userId;
    private String username;
    
    // Quick stats
    private int totalActiveRecommendations;
    private int unviewedRecommendations;
    private int newRecommendationsToday;
    private int priorityRecommendations;
    
    // Content breakdown
    private int personalContentRecommendations;
    private int groupContentRecommendations;
    private int groupSuggestions;
    private int trendingRecommendations;
    
    // Quality indicators
    private boolean hasHighQualityRecommendations;
    private double averageRelevanceScore;
    private String topRecommendationReason;
    
    // Recent activity
    private LocalDateTime lastRecommendationAt;
    private LocalDateTime lastInteractionAt;
    private List<String> recentGenres;
    
    // Actionable items
    private List<QuickRecommendation> topRecommendations;
    private List<QuickRecommendation> expiringSoon;
    
    // Profile status
    private boolean profileNeedsAttention;
    private String profileStatus;
    private List<String> quickActions;
    
    /**
     * Default constructor
     */
    public RecommendationSummary() {}
    
    /**
     * Constructor with basic data
     */
    public RecommendationSummary(Long userId, String username, 
                               int totalActiveRecommendations, 
                               int unviewedRecommendations) {
        this.userId = userId;
        this.username = username;
        this.totalActiveRecommendations = totalActiveRecommendations;
        this.unviewedRecommendations = unviewedRecommendations;
    }
    
    /**
     * Check if user has new activity to review
     */
    public boolean hasNewActivity() {
        return unviewedRecommendations > 0 || newRecommendationsToday > 0;
    }
    
    /**
     * Check if user needs immediate attention
     */
    public boolean needsImmediateAttention() {
        return profileNeedsAttention || 
               (expiringSoon != null && !expiringSoon.isEmpty()) ||
               priorityRecommendations > 3;
    }
    
    /**
     * Get dashboard notification count
     */
    public int getNotificationCount() {
        int count = unviewedRecommendations;
        if (expiringSoon != null) count += expiringSoon.size();
        if (profileNeedsAttention) count += 1;
        return count;
    }
    
    /**
     * Get activity level description
     */
    public String getActivityLevel() {
        if (lastInteractionAt == null) return "Inactive";
        
        long daysSinceActivity = java.time.Duration.between(lastInteractionAt, LocalDateTime.now()).toDays();
        if (daysSinceActivity <= 1) return "Very Active";
        else if (daysSinceActivity <= 7) return "Active";
        else if (daysSinceActivity <= 30) return "Moderate";
        else return "Inactive";
    }
    
    /**
     * Get recommendation freshness description
     */
    public String getRecommendationFreshness() {
        if (lastRecommendationAt == null) return "No recommendations";
        
        long hoursSinceRecommendation = java.time.Duration.between(lastRecommendationAt, LocalDateTime.now()).toHours();
        if (hoursSinceRecommendation <= 24) return "Fresh";
        else if (hoursSinceRecommendation <= 168) return "Recent"; // 1 week
        else return "Stale";
    }
    
    /**
     * Get priority action for user
     */
    public String getPriorityAction() {
        if (priorityRecommendations > 0) {
            return String.format("Check %d high-priority recommendations", priorityRecommendations);
        } else if (unviewedRecommendations > 0) {
            return String.format("Review %d new recommendations", unviewedRecommendations);
        } else if (profileNeedsAttention) {
            return "Update your preferences for better recommendations";
        } else if (totalActiveRecommendations == 0) {
            return "Rate some content to get personalized recommendations";
        } else {
            return "All caught up! Check back later for new recommendations";
        }
    }
    
    /**
     * Get engagement prompt for user
     */
    public String getEngagementPrompt() {
        if (hasHighQualityRecommendations) {
            return "We found some great matches for you!";
        } else if (newRecommendationsToday > 0) {
            return String.format("You have %d new recommendations today", newRecommendationsToday);
        } else if (totalActiveRecommendations > 5) {
            return "Lots of content waiting for you to explore";
        } else {
            return "Discover something new today";
        }
    }
    
    /**
     * Check if recommendations are getting stale
     */
    public boolean areRecommendationsStale() {
        return lastRecommendationAt != null && 
               java.time.Duration.between(lastRecommendationAt, LocalDateTime.now()).toDays() > 7;
    }
    
    /**
     * DTO for quick recommendation display
     */
    @Data
    public static class QuickRecommendation {
        private Long recommendationId;
        private String type; // "CONTENT" or "GROUP"
        private String title;
        private String reason;
        private double relevanceScore;
        private boolean isPriority;
        private boolean isExpiring;
        private String badge; // "New", "Hot", "Expiring", etc.
        
        public QuickRecommendation(Long recommendationId, String type, String title, 
                                 String reason, double relevanceScore) {
            this.recommendationId = recommendationId;
            this.type = type;
            this.title = title;
            this.reason = reason;
            this.relevanceScore = relevanceScore;
            this.isPriority = relevanceScore >= 0.8;
        }
        
        /**
         * Get display-friendly relevance
         */
        public String getRelevanceDisplay() {
            if (relevanceScore >= 0.9) return "Perfect Match";
            else if (relevanceScore >= 0.8) return "Great Match";
            else if (relevanceScore >= 0.7) return "Good Match";
            else return "Might Interest You";
        }
    }
} 