package com.showsync.dto.recommendation;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;

/**
 * DTO containing comprehensive analytics about recommendation system performance.
 * Used for dashboards, monitoring, and system optimization.
 */
@Data
public class RecommendationAnalytics {
    
    // Time period
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private int analysisWindowDays;
    
    // Overall metrics
    private long totalRecommendationsGenerated;
    private long totalUsersWithRecommendations;
    private double overallEngagementRate;
    private double overallConversionRate;
    
    // Content recommendations
    private long contentRecommendationsGenerated;
    private long contentRecommendationsViewed;
    private long contentRecommendationsAdded;
    private long contentRecommendationsDismissed;
    private double contentEngagementRate;
    private double contentConversionRate;
    
    // Group recommendations
    private long groupRecommendationsGenerated;
    private long groupRecommendationsViewed;
    private long groupRecommendationsJoined;
    private long groupRecommendationsDismissed;
    private double groupEngagementRate;
    private double groupConversionRate;
    
    // Quality metrics
    private double averageFeedbackScore;
    private long positiveFeedbackCount;
    private long negativeFeedbackCount;
    private double feedbackParticipationRate;
    
    // Performance by type
    private Map<String, Double> engagementByRecommendationType;
    private Map<String, Double> conversionByRecommendationType;
    private Map<String, Long> volumeByRecommendationType;
    
    // Top performing reasons
    private List<ReasonPerformance> topPerformingReasons;
    private List<ReasonPerformance> poorPerformingReasons;
    
    // User segments
    private Map<String, UserSegmentMetrics> userSegmentPerformance;
    
    // Trends
    private List<DailyMetrics> dailyTrends;
    
    /**
     * Default constructor
     */
    public RecommendationAnalytics() {}
    
    /**
     * Constructor with time period
     */
    public RecommendationAnalytics(LocalDateTime startDate, LocalDateTime endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.analysisWindowDays = (int) java.time.Duration.between(startDate, endDate).toDays();
    }
    
    /**
     * Calculate overall engagement rate
     */
    public void calculateEngagementRate() {
        if (totalRecommendationsGenerated > 0) {
            long totalViewed = contentRecommendationsViewed + groupRecommendationsViewed;
            this.overallEngagementRate = (double) totalViewed / totalRecommendationsGenerated * 100.0;
        }
    }
    
    /**
     * Calculate overall conversion rate
     */
    public void calculateConversionRate() {
        if (totalRecommendationsGenerated > 0) {
            long totalConverted = contentRecommendationsAdded + groupRecommendationsJoined;
            this.overallConversionRate = (double) totalConverted / totalRecommendationsGenerated * 100.0;
        }
    }
    
    /**
     * Calculate content-specific rates
     */
    public void calculateContentRates() {
        if (contentRecommendationsGenerated > 0) {
            this.contentEngagementRate = (double) contentRecommendationsViewed / contentRecommendationsGenerated * 100.0;
            this.contentConversionRate = (double) contentRecommendationsAdded / contentRecommendationsGenerated * 100.0;
        }
    }
    
    /**
     * Calculate group-specific rates
     */
    public void calculateGroupRates() {
        if (groupRecommendationsGenerated > 0) {
            this.groupEngagementRate = (double) groupRecommendationsViewed / groupRecommendationsGenerated * 100.0;
            this.groupConversionRate = (double) groupRecommendationsJoined / groupRecommendationsGenerated * 100.0;
        }
    }
    
    /**
     * Calculate feedback participation rate
     */
    public void calculateFeedbackRate() {
        long totalFeedback = positiveFeedbackCount + negativeFeedbackCount;
        if (totalRecommendationsGenerated > 0) {
            this.feedbackParticipationRate = (double) totalFeedback / totalRecommendationsGenerated * 100.0;
        }
    }
    
    /**
     * Get system health status
     */
    public String getSystemHealthStatus() {
        if (overallConversionRate >= 10.0 && averageFeedbackScore >= 3.5) {
            return "Excellent";
        } else if (overallConversionRate >= 5.0 && averageFeedbackScore >= 3.0) {
            return "Good";
        } else if (overallConversionRate >= 2.0 && averageFeedbackScore >= 2.5) {
            return "Fair";
        } else {
            return "Needs Improvement";
        }
    }
    
    /**
     * Get key insights summary
     */
    public List<String> getKeyInsights() {
        return List.of(
            String.format("Generated %,d recommendations for %,d users", 
                totalRecommendationsGenerated, totalUsersWithRecommendations),
            String.format("%.1f%% engagement rate, %.1f%% conversion rate", 
                overallEngagementRate, overallConversionRate),
            String.format("Average feedback score: %.1f/5", averageFeedbackScore),
            String.format("Content performs %.1f%% conversion, Groups %.1f%% conversion", 
                contentConversionRate, groupConversionRate)
        );
    }
    
    /**
     * DTO for reason-based performance metrics
     */
    @Data
    public static class ReasonPerformance {
        private String reasonCode;
        private String reasonDescription;
        private long count;
        private double engagementRate;
        private double conversionRate;
        private double averageFeedback;
        
        public ReasonPerformance(String reasonCode, String reasonDescription, 
                               long count, double engagementRate, double conversionRate) {
            this.reasonCode = reasonCode;
            this.reasonDescription = reasonDescription;
            this.count = count;
            this.engagementRate = engagementRate;
            this.conversionRate = conversionRate;
        }
    }
    
    /**
     * DTO for user segment metrics
     */
    @Data
    public static class UserSegmentMetrics {
        private String segmentName;
        private long userCount;
        private double avgRecommendationsPerUser;
        private double engagementRate;
        private double conversionRate;
        private double satisfactionScore;
        
        public UserSegmentMetrics(String segmentName, long userCount, 
                                double avgRecommendationsPerUser, double engagementRate) {
            this.segmentName = segmentName;
            this.userCount = userCount;
            this.avgRecommendationsPerUser = avgRecommendationsPerUser;
            this.engagementRate = engagementRate;
        }
    }
    
    /**
     * DTO for daily trend data
     */
    @Data
    public static class DailyMetrics {
        private LocalDateTime date;
        private long recommendationsGenerated;
        private long engagements;
        private long conversions;
        private double engagementRate;
        private double conversionRate;
        
        public DailyMetrics(LocalDateTime date, long recommendationsGenerated, 
                          long engagements, long conversions) {
            this.date = date;
            this.recommendationsGenerated = recommendationsGenerated;
            this.engagements = engagements;
            this.conversions = conversions;
            this.engagementRate = recommendationsGenerated > 0 ? 
                (double) engagements / recommendationsGenerated * 100.0 : 0.0;
            this.conversionRate = recommendationsGenerated > 0 ? 
                (double) conversions / recommendationsGenerated * 100.0 : 0.0;
        }
    }
} 