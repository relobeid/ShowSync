package com.showsync.dto.recommendation;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO containing summary of batch recommendation generation process.
 * Used for monitoring and analytics of the recommendation system.
 */
@Data
public class RecommendationGenerationSummary {
    
    // Overall statistics
    private int totalUsersProcessed;
    private int totalRecommendationsGenerated;
    private long processingTimeMs;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    // Breakdown by type
    private int contentRecommendationsGenerated;
    private int groupRecommendationsGenerated;
    
    // Quality metrics
    private int usersWithSufficientData;
    private int usersWithInsufficientData;
    private double averageConfidenceScore;
    
    // Error tracking
    private int successfulUsers;
    private int failedUsers;
    private Map<String, Integer> errorCounts = new HashMap<>();
    
    // Performance metrics
    private double averageProcessingTimePerUser;
    private int recommendationsPerUser;
    
    /**
     * Default constructor
     */
    public RecommendationGenerationSummary() {
        this.startTime = LocalDateTime.now();
    }
    
    /**
     * Constructor with basic data
     */
    public RecommendationGenerationSummary(int totalUsersProcessed, int totalRecommendationsGenerated) {
        this();
        this.totalUsersProcessed = totalUsersProcessed;
        this.totalRecommendationsGenerated = totalRecommendationsGenerated;
    }
    
    /**
     * Mark the generation process as completed
     */
    public void markCompleted() {
        this.endTime = LocalDateTime.now();
        this.processingTimeMs = java.time.Duration.between(startTime, endTime).toMillis();
        this.averageProcessingTimePerUser = totalUsersProcessed > 0 ? 
            (double) processingTimeMs / totalUsersProcessed : 0.0;
        this.recommendationsPerUser = totalUsersProcessed > 0 ? 
            totalRecommendationsGenerated / totalUsersProcessed : 0;
    }
    
    /**
     * Add an error to the tracking
     */
    public void addError(String errorType) {
        errorCounts.put(errorType, errorCounts.getOrDefault(errorType, 0) + 1);
        failedUsers++;
    }
    
    /**
     * Record a successful user processing
     */
    public void recordSuccess() {
        successfulUsers++;
    }
    
    /**
     * Add content recommendations
     */
    public void addContentRecommendations(int count) {
        this.contentRecommendationsGenerated += count;
        this.totalRecommendationsGenerated += count;
    }
    
    /**
     * Add group recommendations
     */
    public void addGroupRecommendations(int count) {
        this.groupRecommendationsGenerated += count;
        this.totalRecommendationsGenerated += count;
    }
    
    /**
     * Get success rate percentage
     */
    public double getSuccessRate() {
        if (totalUsersProcessed == 0) return 0.0;
        return (double) successfulUsers / totalUsersProcessed * 100.0;
    }
    
    /**
     * Get processing time in seconds
     */
    public double getProcessingTimeSeconds() {
        return processingTimeMs / 1000.0;
    }
    
    /**
     * Get most common error type
     */
    public String getMostCommonError() {
        return errorCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse("None");
    }
    
    /**
     * Check if generation was successful overall
     */
    public boolean isSuccessful() {
        return getSuccessRate() >= 90.0 && totalRecommendationsGenerated > 0;
    }
    
    /**
     * Get summary description for logs
     */
    public String getSummaryDescription() {
        return String.format(
            "Generated %d recommendations for %d users in %.2f seconds (%.1f%% success rate)",
            totalRecommendationsGenerated, 
            totalUsersProcessed, 
            getProcessingTimeSeconds(), 
            getSuccessRate()
        );
    }
    
    /**
     * Get detailed breakdown
     */
    public Map<String, Object> getDetailedBreakdown() {
        Map<String, Object> breakdown = new HashMap<>();
        breakdown.put("totalUsers", totalUsersProcessed);
        breakdown.put("successfulUsers", successfulUsers);
        breakdown.put("failedUsers", failedUsers);
        breakdown.put("totalRecommendations", totalRecommendationsGenerated);
        breakdown.put("contentRecommendations", contentRecommendationsGenerated);
        breakdown.put("groupRecommendations", groupRecommendationsGenerated);
        breakdown.put("processingTimeMs", processingTimeMs);
        breakdown.put("averageConfidenceScore", averageConfidenceScore);
        breakdown.put("usersWithSufficientData", usersWithSufficientData);
        breakdown.put("usersWithInsufficientData", usersWithInsufficientData);
        breakdown.put("errors", errorCounts);
        return breakdown;
    }
} 