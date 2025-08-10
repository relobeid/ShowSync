package com.showsync.dto.recommendation;

import com.showsync.entity.recommendation.RecommendationReason;
import com.showsync.entity.recommendation.RecommendationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for content recommendation responses.
 * Contains all information needed to display a content recommendation to users.
 */
@Data
public class ContentRecommendationResponse {
    
    // Recommendation metadata
    private Long recommendationId;
    private RecommendationType recommendationType;
    private RecommendationReason reasonCode;
    private String explanation;
    private String contextDescription;
    
    // Scoring and relevance
    private BigDecimal relevanceScore;
    private int relevancePercentage;
    
    // Recommended content
    private Long mediaId;
    private String mediaTitle;
    private String mediaType; // MOVIE, TV_SHOW, BOOK
    private String mediaPoster;
    private String mediaOverview;
    private Double mediaRating;
    private String mediaGenres;
    private Integer mediaYear;
    private Integer mediaRuntime;
    
    // Context information
    private Long sourceMediaId; // "Because you liked X"
    private String sourceMediaTitle;
    private Long sourceGroupId; // "Popular in group Y"
    private String sourceGroupName;
    
    // Group context (if group recommendation)
    private Long groupId;
    private String groupName;
    
    // User interaction status
    private boolean isViewed;
    private boolean isDismissed;
    private boolean isAddedToLibrary;
    private Integer userFeedback; // 1-5 rating
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean isExpiringSoon;
    
    /**
     * Default constructor
     */
    public ContentRecommendationResponse() {}
    
    /**
     * Constructor for personal recommendations
     */
    public ContentRecommendationResponse(Long recommendationId, RecommendationType type, 
                                       RecommendationReason reason, String explanation,
                                       BigDecimal relevanceScore, Long mediaId, String mediaTitle, 
                                       String mediaType, boolean isViewed) {
        this.recommendationId = recommendationId;
        this.recommendationType = type;
        this.reasonCode = reason;
        this.explanation = explanation;
        this.relevanceScore = relevanceScore;
        this.relevancePercentage = (int) Math.round(relevanceScore.doubleValue() * 100);
        this.mediaId = mediaId;
        this.mediaTitle = mediaTitle;
        this.mediaType = mediaType;
        this.isViewed = isViewed;
    }
    
    /**
     * Check if this is a personal recommendation
     */
    public boolean isPersonalRecommendation() {
        return groupId == null;
    }
    
    /**
     * Check if this is a group recommendation
     */
    public boolean isGroupRecommendation() {
        return groupId != null;
    }
    
    /**
     * Check if recommendation is actionable (not dismissed, not in library)
     */
    public boolean isActionable() {
        return !isDismissed && !isAddedToLibrary;
    }
    
    /**
     * Check if user has provided feedback
     */
    public boolean hasFeedback() {
        return userFeedback != null;
    }
    
    /**
     * Get display-friendly recommendation reason
     */
    public String getDisplayReason() {
        if (explanation != null && !explanation.trim().isEmpty()) {
            return explanation;
        }
        
        if (reasonCode != null) {
            if (sourceMediaTitle != null) {
                return reasonCode.getExplanation(sourceMediaTitle);
            } else if (sourceGroupName != null) {
                return reasonCode.getExplanation(sourceGroupName);
            } else {
                return reasonCode.getDescription();
            }
        }
        
        return "We think you might enjoy this";
    }
    
    /**
     * Get context badge text for UI
     */
    public String getContextBadge() {
        if (recommendationType == RecommendationType.TRENDING) {
            return "Trending";
        } else if (sourceMediaTitle != null) {
            return "Similar to " + sourceMediaTitle;
        } else if (sourceGroupName != null) {
            return "Popular in " + sourceGroupName;
        } else if (groupName != null) {
            return "For " + groupName;
        } else {
            return "For You";
        }
    }
    
    /**
     * Get recommendation confidence level
     */
    public String getConfidenceLevel() {
        if (relevanceScore == null) return "Unknown";
        
        double score = relevanceScore.doubleValue();
        if (score >= 0.8) return "High";
        else if (score >= 0.6) return "Medium";
        else return "Low";
    }
    
    /**
     * Get time until expiration in hours
     */
    public long getHoursUntilExpiration() {
        if (expiresAt == null) return Long.MAX_VALUE;
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toHours();
    }
    
    /**
     * Check if recommendation priority should be boosted in UI
     */
    public boolean isPriorityRecommendation() {
        return !isViewed && relevanceScore != null && relevanceScore.doubleValue() >= 0.8;
    }
} 