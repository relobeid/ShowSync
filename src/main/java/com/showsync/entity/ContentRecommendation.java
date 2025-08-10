package com.showsync.entity;

import com.showsync.entity.recommendation.RecommendationReason;
import com.showsync.entity.recommendation.RecommendationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a content recommendation for a user or group.
 * Stores media suggestions with scoring, context, and user feedback tracking.
 */
@Data
@Entity
@Table(name = "content_recommendations")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"user", "group", "recommendedMedia", "sourceMedia", "sourceGroup"})
@ToString(exclude = {"user", "group", "recommendedMedia", "sourceMedia", "sourceGroup"})
public class ContentRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group; // NULL for personal recommendations
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_media_id", nullable = false)
    private Media recommendedMedia;
    
    // Scoring
    @Column(name = "relevance_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal relevanceScore;
    
    @Column(name = "reason_code", length = 100)
    @Enumerated(EnumType.STRING)
    private RecommendationReason reasonCode;
    
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
    
    // Context and source
    @Column(name = "recommendation_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private RecommendationType recommendationType = RecommendationType.PERSONAL;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_media_id")
    private Media sourceMedia; // "Because you liked X"
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_group_id")
    private Group sourceGroup; // "Popular in group Y"
    
    // User interaction tracking
    @Column(name = "is_viewed", nullable = false)
    private boolean isViewed = false;
    
    @Column(name = "is_dismissed", nullable = false)
    private boolean isDismissed = false;
    
    @Column(name = "is_added_to_library", nullable = false)
    private boolean isAddedToLibrary = false;
    
    @Column(name = "user_feedback")
    private Integer userFeedback; // 1-5 star feedback on recommendation quality
    
    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Default constructor
     */
    public ContentRecommendation() {
        this.expiresAt = LocalDateTime.now().plusDays(14); // Default 14-day expiration
    }
    
    /**
     * Constructor for personal recommendations
     */
    public ContentRecommendation(User user, Media recommendedMedia, double relevanceScore,
                                RecommendationReason reason, String explanation) {
        this();
        this.user = user;
        this.recommendedMedia = recommendedMedia;
        this.relevanceScore = BigDecimal.valueOf(relevanceScore);
        this.reasonCode = reason;
        this.explanation = explanation;
        this.recommendationType = RecommendationType.PERSONAL;
    }
    
    /**
     * Constructor for group recommendations
     */
    public ContentRecommendation(User user, Group group, Media recommendedMedia, double relevanceScore,
                                RecommendationReason reason, String explanation) {
        this(user, recommendedMedia, relevanceScore, reason, explanation);
        this.group = group;
        this.recommendationType = RecommendationType.GROUP;
    }
    
    /**
     * Constructor with source context
     */
    public ContentRecommendation(User user, Media recommendedMedia, double relevanceScore,
                                RecommendationReason reason, String explanation, Media sourceMedia) {
        this(user, recommendedMedia, relevanceScore, reason, explanation);
        this.sourceMedia = sourceMedia;
        this.recommendationType = RecommendationType.CONTENT_BASED;
    }
    
    /**
     * Check if recommendation is still valid (not expired or dismissed)
     */
    public boolean isValid() {
        return !isDismissed && LocalDateTime.now().isBefore(expiresAt);
    }
    
    /**
     * Check if recommendation is actionable (valid and not acted upon)
     */
    public boolean isActionable() {
        return isValid() && !isAddedToLibrary;
    }
    
    /**
     * Check if this is a personal recommendation
     */
    public boolean isPersonalRecommendation() {
        return group == null;
    }
    
    /**
     * Check if this is a group recommendation
     */
    public boolean isGroupRecommendation() {
        return group != null;
    }
    
    /**
     * Mark recommendation as viewed
     */
    public void markAsViewed() {
        this.isViewed = true;
    }
    
    /**
     * Mark recommendation as dismissed by user
     */
    public void dismiss() {
        this.isDismissed = true;
    }
    
    /**
     * Mark recommendation as acted upon (user added to library)
     */
    public void markAsAddedToLibrary() {
        this.isAddedToLibrary = true;
        this.isViewed = true;
    }
    
    /**
     * Set user feedback (1-5 stars)
     */
    public void setUserFeedback(int feedback) {
        if (feedback < 1 || feedback > 5) {
            throw new IllegalArgumentException("Feedback must be between 1 and 5");
        }
        this.userFeedback = feedback;
        this.isViewed = true;
    }
    
    /**
     * Get relevance percentage (0-100%)
     */
    public int getRelevancePercentage() {
        return (int) Math.round(relevanceScore.doubleValue() * 100);
    }
    
    /**
     * Get display-friendly explanation with context
     */
    public String getDisplayExplanation() {
        if (explanation != null && !explanation.trim().isEmpty()) {
            return explanation;
        }
        
        if (reasonCode != null && sourceMedia != null) {
            return reasonCode.getExplanation(sourceMedia.getTitle());
        }
        
        if (reasonCode != null && sourceGroup != null) {
            return reasonCode.getExplanation(sourceGroup.getName());
        }
        
        if (reasonCode != null && recommendedMedia != null) {
            return reasonCode.getExplanation(recommendedMedia.getTitle());
        }
        
        return "We think you might enjoy this";
    }
    
    /**
     * Get recommendation context description
     */
    public String getContextDescription() {
        if (isGroupRecommendation()) {
            return "Recommended for " + group.getName();
        } else if (sourceMedia != null) {
            return "Because you liked " + sourceMedia.getTitle();
        } else if (recommendationType == RecommendationType.TRENDING) {
            return "Trending now";
        } else {
            return "Personal recommendation";
        }
    }
    
    /**
     * Check if user has provided feedback
     */
    public boolean hasFeedback() {
        return userFeedback != null;
    }
    
    /**
     * Check if feedback was positive (4-5 stars)
     */
    public boolean hasPositiveFeedback() {
        return userFeedback != null && userFeedback >= 4;
    }
    
    /**
     * Check if feedback was negative (1-2 stars)
     */
    public boolean hasNegativeFeedback() {
        return userFeedback != null && userFeedback <= 2;
    }
    
    /**
     * Extend expiration by specified days
     */
    public void extendExpiration(int days) {
        this.expiresAt = this.expiresAt.plusDays(days);
    }
    
    /**
     * Get time until expiration in hours
     */
    public long getHoursUntilExpiration() {
        return java.time.Duration.between(LocalDateTime.now(), expiresAt).toHours();
    }
    
    /**
     * Check if recommendation is expiring soon (within 48 hours)
     */
    public boolean isExpiringSoon() {
        return getHoursUntilExpiration() <= 48;
    }
} 