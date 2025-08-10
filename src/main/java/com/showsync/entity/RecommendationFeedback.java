package com.showsync.entity;

import com.showsync.entity.recommendation.FeedbackType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity for tracking user feedback on recommendations.
 * Used for machine learning and algorithm improvement.
 */
@Data
@Entity
@Table(name = "recommendation_feedback")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user"})
public class RecommendationFeedback {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "recommendation_type", nullable = false, length = 50)
    private String recommendationType; // "GROUP" or "CONTENT"
    
    @Column(name = "recommendation_id", nullable = false)
    private Long recommendationId; // ID of GroupRecommendation or ContentRecommendation
    
    // Feedback data
    @Column(name = "feedback_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeedbackType feedbackType;
    
    @Column(name = "feedback_reason", length = 100)
    private String feedbackReason; // NOT_INTERESTED, ALREADY_SEEN, GOOD_MATCH, etc.
    
    @Column(name = "feedback_score")
    private Integer feedbackScore; // 1-5 rating
    
    @Column(name = "feedback_text", columnDefinition = "TEXT")
    private String feedbackText; // Free-form feedback
    
    // Context
    @Column(name = "action_taken", length = 50)
    private String actionTaken; // JOINED_GROUP, ADDED_TO_LIBRARY, DISMISSED, etc.
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * Default constructor
     */
    public RecommendationFeedback() {}
    
    /**
     * Constructor for group recommendation feedback
     */
    public RecommendationFeedback(User user, GroupRecommendation groupRecommendation, 
                                 FeedbackType feedbackType, String actionTaken) {
        this.user = user;
        this.recommendationType = "GROUP";
        this.recommendationId = groupRecommendation.getId();
        this.feedbackType = feedbackType;
        this.actionTaken = actionTaken;
    }
    
    /**
     * Constructor for content recommendation feedback
     */
    public RecommendationFeedback(User user, ContentRecommendation contentRecommendation,
                                 FeedbackType feedbackType, String actionTaken) {
        this.user = user;
        this.recommendationType = "CONTENT";
        this.recommendationId = contentRecommendation.getId();
        this.feedbackType = feedbackType;
        this.actionTaken = actionTaken;
    }
    
    /**
     * Constructor with rating
     */
    public RecommendationFeedback(User user, String recommendationType, Long recommendationId,
                                 int rating, String actionTaken) {
        this.user = user;
        this.recommendationType = recommendationType;
        this.recommendationId = recommendationId;
        this.feedbackScore = rating;
        this.feedbackType = FeedbackType.fromRating(rating);
        this.actionTaken = actionTaken;
    }
    
    /**
     * Check if this is feedback for a group recommendation
     */
    public boolean isGroupFeedback() {
        return "GROUP".equals(recommendationType);
    }
    
    /**
     * Check if this is feedback for a content recommendation
     */
    public boolean isContentFeedback() {
        return "CONTENT".equals(recommendationType);
    }
    
    /**
     * Check if feedback includes a rating
     */
    public boolean hasRating() {
        return feedbackScore != null;
    }
    
    /**
     * Check if feedback includes text
     */
    public boolean hasText() {
        return feedbackText != null && !feedbackText.trim().isEmpty();
    }
    
    /**
     * Get feedback weight for algorithm learning (-1.0 to 1.0)
     */
    public double getFeedbackWeight() {
        if (feedbackType != null) {
            return feedbackType.getWeight();
        }
        return 0.0;
    }
    
    /**
     * Check if this represents a positive outcome
     */
    public boolean isPositiveOutcome() {
        return "JOINED_GROUP".equals(actionTaken) || 
               "ADDED_TO_LIBRARY".equals(actionTaken) ||
               (feedbackScore != null && feedbackScore >= 4);
    }
    
    /**
     * Check if this represents a negative outcome
     */
    public boolean isNegativeOutcome() {
        return "DISMISSED".equals(actionTaken) ||
               (feedbackScore != null && feedbackScore <= 2);
    }
    
    /**
     * Get feedback summary for analysis
     */
    public String getFeedbackSummary() {
        StringBuilder summary = new StringBuilder();
        
        if (feedbackType != null) {
            summary.append(feedbackType.getDisplayName());
        }
        
        if (feedbackScore != null) {
            summary.append(" (").append(feedbackScore).append("/5)");
        }
        
        if (actionTaken != null) {
            summary.append(" - ").append(actionTaken.replace("_", " ").toLowerCase());
        }
        
        return summary.toString();
    }
    
    /**
     * Set feedback with validation
     */
    public void setFeedbackScore(Integer score) {
        if (score != null && (score < 1 || score > 5)) {
            throw new IllegalArgumentException("Feedback score must be between 1 and 5");
        }
        this.feedbackScore = score;
        
        // Auto-set feedback type based on score
        if (score != null) {
            this.feedbackType = FeedbackType.fromRating(score);
        }
    }
    
    /**
     * Add detailed feedback text
     */
    public void addFeedbackText(String text) {
        if (text != null && text.length() > 1000) {
            text = text.substring(0, 1000); // Truncate if too long
        }
        this.feedbackText = text;
    }
} 