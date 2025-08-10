package com.showsync.entity;

import com.showsync.entity.recommendation.RecommendationReason;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a group recommendation for a user.
 * Stores pre-calculated group suggestions with scoring and explanations.
 */
@Data
@Entity
@Table(name = "group_recommendations", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "recommended_group_id"}))
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"user", "recommendedGroup"})
@ToString(exclude = {"user", "recommendedGroup"})
public class GroupRecommendation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recommended_group_id", nullable = false)
    private Group recommendedGroup;
    
    // Scoring
    @Column(name = "compatibility_score", nullable = false, precision = 3, scale = 2)
    private BigDecimal compatibilityScore;
    
    @Column(name = "reason_code", length = 100)
    @Enumerated(EnumType.STRING)
    private RecommendationReason reasonCode;
    
    @Column(name = "explanation", columnDefinition = "TEXT")
    private String explanation;
    
    // User interaction tracking
    @Column(name = "is_viewed", nullable = false)
    private boolean isViewed = false;
    
    @Column(name = "is_dismissed", nullable = false)
    private boolean isDismissed = false;
    
    @Column(name = "is_joined", nullable = false)
    private boolean isJoined = false;
    
    // Timestamps
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    /**
     * Default constructor
     */
    public GroupRecommendation() {
        this.expiresAt = LocalDateTime.now().plusDays(7); // Default 7-day expiration
    }
    
    /**
     * Constructor with basic data
     */
    public GroupRecommendation(User user, Group recommendedGroup, double compatibilityScore, 
                              RecommendationReason reason, String explanation) {
        this();
        this.user = user;
        this.recommendedGroup = recommendedGroup;
        this.compatibilityScore = BigDecimal.valueOf(compatibilityScore);
        this.reasonCode = reason;
        this.explanation = explanation;
    }
    
    /**
     * Check if recommendation is still valid (not expired)
     */
    public boolean isValid() {
        return !isDismissed && LocalDateTime.now().isBefore(expiresAt);
    }
    
    /**
     * Check if recommendation is actionable (valid and not acted upon)
     */
    public boolean isActionable() {
        return isValid() && !isJoined;
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
     * Mark recommendation as acted upon (user joined the group)
     */
    public void markAsJoined() {
        this.isJoined = true;
        this.isViewed = true;
    }
    
    /**
     * Get compatibility percentage (0-100%)
     */
    public int getCompatibilityPercentage() {
        return (int) Math.round(compatibilityScore.doubleValue() * 100);
    }
    
    /**
     * Get display-friendly explanation with group name
     */
    public String getDisplayExplanation() {
        if (explanation != null && !explanation.trim().isEmpty()) {
            return explanation;
        }
        
        if (reasonCode != null && recommendedGroup != null) {
            return reasonCode.getExplanation(recommendedGroup.getName());
        }
        
        return "This group might be a good match for you";
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
     * Check if recommendation is expiring soon (within 24 hours)
     */
    public boolean isExpiringSoon() {
        return getHoursUntilExpiration() <= 24;
    }
} 