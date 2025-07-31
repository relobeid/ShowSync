package com.showsync.dto.recommendation;

import com.showsync.entity.recommendation.ViewingPersonality;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO containing personalized recommendation insights for a specific user.
 * Shows their recommendation history, preferences, and performance metrics.
 */
@Data
public class UserRecommendationInsights {
    
    // User identification
    private Long userId;
    private String username;
    
    // Recommendation statistics
    private int totalRecommendationsReceived;
    private int recommendationsViewed;
    private int recommendationsActedUpon;
    private int recommendationsDismissed;
    
    // Engagement metrics
    private double engagementRate;
    private double actionRate;
    private double dismissalRate;
    private double averageFeedbackScore;
    
    // Preference profile
    private ViewingPersonality viewingPersonality;
    private double confidenceScore;
    private Map<String, Double> topGenres;
    private Map<String, Double> topPlatforms;
    private String preferredContentLength;
    
    // Interaction patterns
    private int totalInteractions;
    private int completedContent;
    private double completionRate;
    private double averageRating;
    private double ratingVariance;
    
    // Recent activity
    private int recentRecommendationsCount;
    private LocalDateTime lastRecommendationAt;
    private LocalDateTime lastActionAt;
    private List<String> recentActions;
    
    // Recommendation breakdown
    private Map<String, Integer> recommendationsByType;
    private Map<String, Integer> recommendationsByReason;
    private Map<String, Double> successRateByType;
    
    // Personalization insights
    private List<String> topRecommendationReasons;
    private List<String> leastEffectiveReasons;
    private double personalizedVsTrendingPreference;
    
    // Profile quality
    private boolean hasSufficientData;
    private LocalDateTime profileLastUpdated;
    private List<String> dataGaps;
    private List<String> improvementSuggestions;
    
    /**
     * Default constructor
     */
    public UserRecommendationInsights() {}
    
    /**
     * Constructor with basic user data
     */
    public UserRecommendationInsights(Long userId, String username, 
                                    int totalRecommendationsReceived, 
                                    ViewingPersonality personality, 
                                    double confidenceScore) {
        this.userId = userId;
        this.username = username;
        this.totalRecommendationsReceived = totalRecommendationsReceived;
        this.viewingPersonality = personality;
        this.confidenceScore = confidenceScore;
    }
    
    /**
     * Calculate engagement metrics
     */
    public void calculateMetrics() {
        if (totalRecommendationsReceived > 0) {
            this.engagementRate = (double) recommendationsViewed / totalRecommendationsReceived * 100.0;
            this.actionRate = (double) recommendationsActedUpon / totalRecommendationsReceived * 100.0;
            this.dismissalRate = (double) recommendationsDismissed / totalRecommendationsReceived * 100.0;
        }
        
        if (totalInteractions > 0) {
            this.completionRate = (double) completedContent / totalInteractions * 100.0;
        }
    }
    
    /**
     * Get user engagement level
     */
    public String getEngagementLevel() {
        if (engagementRate >= 80.0) return "Very High";
        else if (engagementRate >= 60.0) return "High";
        else if (engagementRate >= 40.0) return "Medium";
        else if (engagementRate >= 20.0) return "Low";
        else return "Very Low";
    }
    
    /**
     * Get recommendation effectiveness
     */
    public String getRecommendationEffectiveness() {
        if (actionRate >= 15.0) return "Excellent";
        else if (actionRate >= 10.0) return "Good";
        else if (actionRate >= 5.0) return "Fair";
        else return "Poor";
    }
    
    /**
     * Get profile confidence level
     */
    public String getProfileConfidenceLevel() {
        if (confidenceScore >= 0.8) return "High";
        else if (confidenceScore >= 0.6) return "Medium";
        else if (confidenceScore >= 0.4) return "Low";
        else return "Very Low";
    }
    
    /**
     * Check if user is a power user (high engagement + feedback)
     */
    public boolean isPowerUser() {
        return engagementRate >= 70.0 && 
               averageFeedbackScore >= 3.5 && 
               totalRecommendationsReceived >= 20;
    }
    
    /**
     * Check if user needs onboarding improvement
     */
    public boolean needsOnboardingImprovement() {
        return totalRecommendationsReceived >= 10 && 
               engagementRate <= 30.0 && 
               confidenceScore <= 0.4;
    }
    
    /**
     * Get personalized insights summary
     */
    public List<String> getPersonalizedInsights() {
        return List.of(
            String.format("You've engaged with %.1f%% of your recommendations", engagementRate),
            String.format("Your viewing personality: %s", 
                viewingPersonality != null ? viewingPersonality.getDisplayName() : "Not yet determined"),
            String.format("Profile confidence: %s (%.1f%%)", 
                getProfileConfidenceLevel(), confidenceScore * 100),
            String.format("Most effective recommendation type: %s", getMostEffectiveType()),
            String.format("You complete %.1f%% of content you start", completionRate)
        );
    }
    
    /**
     * Get most effective recommendation type
     */
    public String getMostEffectiveType() {
        return successRateByType.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(entry -> entry.getKey().replace("_", " ").toLowerCase())
            .orElse("Unknown");
    }
    
    /**
     * Get recommendation optimization suggestions
     */
    public List<String> getOptimizationSuggestions() {
        return List.of(
            dismissalRate > 50.0 ? "Try providing feedback to improve recommendations" : null,
            confidenceScore < 0.5 ? "Rate more content to improve personalization" : null,
            completionRate < 30.0 ? "Consider shorter content recommendations" : null,
            averageFeedbackScore < 3.0 ? "Explore different genres or content types" : null,
            engagementRate < 40.0 ? "We'll focus on higher-confidence recommendations" : null
        ).stream().filter(s -> s != null).toList();
    }
    
    /**
     * Check if user has been active recently
     */
    public boolean isRecentlyActive() {
        return lastActionAt != null && 
               lastActionAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * Get days since last recommendation
     */
    public long getDaysSinceLastRecommendation() {
        if (lastRecommendationAt == null) return Long.MAX_VALUE;
        return java.time.Duration.between(lastRecommendationAt, LocalDateTime.now()).toDays();
    }
    
    /**
     * Get profile completeness percentage
     */
    public int getProfileCompleteness() {
        int score = 0;
        if (viewingPersonality != null) score += 20;
        if (confidenceScore >= 0.5) score += 30;
        if (totalInteractions >= 10) score += 25;
        if (averageFeedbackScore > 0) score += 15;
        if (topGenres != null && !topGenres.isEmpty()) score += 10;
        return score;
    }
} 