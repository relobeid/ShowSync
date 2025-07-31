package com.showsync.dto.recommendation;

import com.showsync.entity.recommendation.ContentLength;
import com.showsync.entity.recommendation.ViewingPersonality;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO containing user preference profile information.
 * Shows calculated taste preferences and viewing patterns.
 */
@Data
public class UserPreferenceResponse {
    
    // User identification
    private Long userId;
    private String username;
    
    // Profile metadata
    private double confidenceScore;
    private int confidencePercentage;
    private boolean hasSufficientData;
    private LocalDateTime lastCalculatedAt;
    private LocalDateTime profileCreatedAt;
    
    // Viewing personality
    private ViewingPersonality viewingPersonality;
    private String personalityDescription;
    private String recommendationStyle;
    
    // Content preferences
    private Map<String, Double> genrePreferences;
    private Map<String, Double> platformPreferences;
    private Map<String, Double> eraPreferences;
    private ContentLength preferredContentLength;
    
    // Rating patterns
    private BigDecimal averageUserRating;
    private BigDecimal ratingVariance;
    private String ratingPersonality; // "Generous", "Critical", "Balanced"
    
    // Activity metrics
    private int totalInteractions;
    private int totalCompleted;
    private double completionRate;
    private int completionPercentage;
    
    // Top preferences (for display)
    private List<PreferenceItem> topGenres;
    private List<PreferenceItem> topPlatforms;
    private List<PreferenceItem> topEras;
    
    // Compatibility insights
    private List<String> similarUsers;
    private List<String> compatibleGroups;
    private double diversityScore;
    
    // Profile insights
    private List<String> profileInsights;
    private List<String> improvementSuggestions;
    private List<String> dataGaps;
    
    /**
     * Default constructor
     */
    public UserPreferenceResponse() {}
    
    /**
     * Constructor with basic data
     */
    public UserPreferenceResponse(Long userId, String username, 
                                double confidenceScore, 
                                ViewingPersonality personality,
                                int totalInteractions) {
        this.userId = userId;
        this.username = username;
        this.confidenceScore = confidenceScore;
        this.confidencePercentage = (int) Math.round(confidenceScore * 100);
        this.viewingPersonality = personality;
        this.totalInteractions = totalInteractions;
        this.hasSufficientData = totalInteractions >= 10 && confidenceScore >= 0.5;
    }
    
    /**
     * Get confidence level description
     */
    public String getConfidenceLevel() {
        if (confidenceScore >= 0.8) return "High";
        else if (confidenceScore >= 0.6) return "Medium";
        else if (confidenceScore >= 0.4) return "Low";
        else return "Very Low";
    }
    
    /**
     * Get profile completeness description
     */
    public String getProfileCompleteness() {
        if (confidenceScore >= 0.9) return "Excellent";
        else if (confidenceScore >= 0.7) return "Good";
        else if (confidenceScore >= 0.5) return "Fair";
        else if (confidenceScore >= 0.3) return "Basic";
        else return "Minimal";
    }
    
    /**
     * Get rating behavior description
     */
    public String getRatingBehavior() {
        if (averageUserRating == null) return "No ratings yet";
        
        double avg = averageUserRating.doubleValue();
        double variance = ratingVariance != null ? ratingVariance.doubleValue() : 0.0;
        
        if (avg >= 8.0 && variance <= 1.0) return "Generous rater (loves most content)";
        else if (avg <= 5.0 && variance <= 1.5) return "Critical rater (selective taste)";
        else if (variance >= 2.5) return "Diverse rater (wide range of opinions)";
        else return "Balanced rater (thoughtful ratings)";
    }
    
    /**
     * Get completion behavior description
     */
    public String getCompletionBehavior() {
        if (totalInteractions == 0) return "No viewing history";
        
        if (completionRate >= 80.0) return "Finisher (completes most content)";
        else if (completionRate >= 60.0) return "Committed viewer (usually finishes)";
        else if (completionRate >= 40.0) return "Selective viewer (finishes what you like)";
        else if (completionRate >= 20.0) return "Browser (samples lots of content)";
        else return "Explorer (tries many things briefly)";
    }
    
    /**
     * Get top genre name
     */
    public String getTopGenre() {
        return topGenres != null && !topGenres.isEmpty() ? 
            topGenres.get(0).getName() : "Unknown";
    }
    
    /**
     * Get content length preference description
     */
    public String getContentLengthPreference() {
        if (preferredContentLength == null) return "No preference";
        return preferredContentLength.getDisplayName() + " content (" + 
               preferredContentLength.getDescription() + ")";
    }
    
    /**
     * Check if profile needs updating
     */
    public boolean needsProfileUpdate() {
        return confidenceScore < 0.5 || 
               totalInteractions < 10 ||
               (lastCalculatedAt != null && 
                lastCalculatedAt.isBefore(LocalDateTime.now().minusDays(30)));
    }
    
    /**
     * Get days since last calculation
     */
    public long getDaysSinceLastCalculation() {
        if (lastCalculatedAt == null) return Long.MAX_VALUE;
        return java.time.Duration.between(lastCalculatedAt, LocalDateTime.now()).toDays();
    }
    
    /**
     * Get diversity description
     */
    public String getDiversityDescription() {
        if (diversityScore >= 0.8) return "Very diverse taste (enjoys many genres)";
        else if (diversityScore >= 0.6) return "Moderately diverse (open to different content)";
        else if (diversityScore >= 0.4) return "Somewhat focused (has clear preferences)";
        else return "Highly focused (strong genre preferences)";
    }
    
    /**
     * Get profile status for UI
     */
    public String getProfileStatus() {
        if (hasSufficientData && confidenceScore >= 0.7) {
            return "Profile Complete";
        } else if (totalInteractions >= 5) {
            return "Building Profile";
        } else {
            return "Getting Started";
        }
    }
    
    /**
     * Get recommendation readiness
     */
    public boolean isReadyForRecommendations() {
        return hasSufficientData && confidenceScore >= 0.4;
    }
    
    /**
     * DTO for preference items (genres, platforms, eras)
     */
    @Data
    public static class PreferenceItem {
        private String name;
        private String displayName;
        private double score;
        private int percentage;
        private String description;
        
        public PreferenceItem(String name, String displayName, double score) {
            this.name = name;
            this.displayName = displayName;
            this.score = score;
            this.percentage = (int) Math.round(score * 100);
        }
        
        public PreferenceItem(String name, double score) {
            this(name, name, score);
        }
        
        /**
         * Get preference strength description
         */
        public String getStrengthDescription() {
            if (score >= 0.8) return "Strong preference";
            else if (score >= 0.6) return "Moderate preference";
            else if (score >= 0.4) return "Some interest";
            else return "Occasional interest";
        }
    }
} 