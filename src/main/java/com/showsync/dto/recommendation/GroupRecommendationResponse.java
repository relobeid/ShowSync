package com.showsync.dto.recommendation;

import com.showsync.entity.recommendation.RecommendationReason;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for group recommendation responses.
 * Contains information about suggested groups for users to join.
 */
@Data
public class GroupRecommendationResponse {
    
    // Recommendation metadata
    private Long recommendationId;
    private RecommendationReason reasonCode;
    private String explanation;
    
    // Scoring
    private BigDecimal compatibilityScore;
    private int compatibilityPercentage;
    
    // Group information
    private Long groupId;
    private String groupName;
    private String groupDescription;
    private String groupType; // PUBLIC, PRIVATE, etc.
    private Integer memberCount;
    private String groupImageUrl;
    private boolean isPublic;
    
    // Group activity metrics
    private Integer recentActivityCount;
    private LocalDateTime lastActivityAt;
    private Integer sharedInterestsCount;
    
    // User interaction status
    private boolean isViewed;
    private boolean isDismissed;
    private boolean isJoined;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isExpiringSoon;
    
    /**
     * Default constructor
     */
    public GroupRecommendationResponse() {}
    
    /**
     * Constructor with basic data
     */
    public GroupRecommendationResponse(Long recommendationId, RecommendationReason reason, 
                                     String explanation, BigDecimal compatibilityScore,
                                     Long groupId, String groupName, String groupDescription, 
                                     Integer memberCount, boolean isViewed) {
        this.recommendationId = recommendationId;
        this.reasonCode = reason;
        this.explanation = explanation;
        this.compatibilityScore = compatibilityScore;
        this.compatibilityPercentage = (int) Math.round(compatibilityScore.doubleValue() * 100);
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupDescription = groupDescription;
        this.memberCount = memberCount;
        this.isViewed = isViewed;
    }
    
    /**
     * Check if recommendation is actionable (not dismissed, not joined)
     */
    public boolean isActionable() {
        return !isDismissed && !isJoined;
    }
    
    /**
     * Get display-friendly recommendation reason
     */
    public String getDisplayReason() {
        if (explanation != null && !explanation.trim().isEmpty()) {
            return explanation;
        }
        
        if (reasonCode != null) {
            return reasonCode.getExplanation(groupName);
        }
        
        return "This group might be a good match for you";
    }
    
    /**
     * Get recommendation confidence level
     */
    public String getConfidenceLevel() {
        if (compatibilityScore == null) return "Unknown";
        
        double score = compatibilityScore.doubleValue();
        if (score >= 0.8) return "High";
        else if (score >= 0.6) return "Medium";
        else return "Low";
    }
    
    /**
     * Get activity level description
     */
    public String getActivityLevel() {
        if (recentActivityCount == null) return "Unknown";
        
        if (recentActivityCount >= 10) return "Very Active";
        else if (recentActivityCount >= 5) return "Active";
        else if (recentActivityCount >= 1) return "Moderate";
        else return "Quiet";
    }
    
    /**
     * Get member count description
     */
    public String getMemberCountDescription() {
        if (memberCount == null) return "Unknown size";
        
        if (memberCount >= 100) return memberCount + " members (Large)";
        else if (memberCount >= 20) return memberCount + " members (Medium)";
        else if (memberCount >= 5) return memberCount + " members (Small)";
        else return memberCount + " members (Intimate)";
    }
    
    /**
     * Get time until expiration in hours
     */
    public long getHoursUntilExpiration() {
        if (expiresAt == null) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toHours();
    }
    
    /**
     * Check if this is a high-priority recommendation
     */
    public boolean isPriorityRecommendation() {
        return !isViewed && compatibilityScore != null && compatibilityScore.doubleValue() >= 0.8;
    }
    
    /**
     * Get recommendation badge for UI
     */
    public String getRecommendationBadge() {
        if (compatibilityScore != null && compatibilityScore.doubleValue() >= 0.9) {
            return "Perfect Match";
        } else if (sharedInterestsCount != null && sharedInterestsCount >= 5) {
            return "Many Shared Interests";
        } else if (recentActivityCount != null && recentActivityCount >= 10) {
            return "Very Active";
        } else {
            return null;
        }
    }
    
    /**
     * Check if group is recently active
     */
    public boolean isRecentlyActive() {
        if (lastActivityAt == null) return false;
        return lastActivityAt.isAfter(LocalDateTime.now().minusDays(7));
    }
} 