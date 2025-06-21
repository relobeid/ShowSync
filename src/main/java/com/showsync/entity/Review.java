package com.showsync.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing detailed user reviews for media items.
 * This extends beyond the simple review text in UserMediaInteraction
 * to provide a full review system with voting and moderation capabilities.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Data
@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
public class Review {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    @Column(name = "rating")
    private Integer rating;

    @Column(name = "helpful_votes", nullable = false)
    private Integer helpfulVotes = 0;

    @Column(name = "total_votes", nullable = false)  
    private Integer totalVotes = 0;

    @Column(name = "is_spoiler", nullable = false)
    private boolean isSpoiler = false;

    @Column(name = "is_moderated", nullable = false)
    private boolean isModerated = false;

    @Column(name = "moderation_reason")
    private String moderationReason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Calculates the helpfulness percentage of this review.
     * 
     * @return Percentage of helpful votes (0.0 to 1.0), or 0.0 if no votes
     */
    public double getHelpfulnessRatio() {
        if (totalVotes == 0) {
            return 0.0;
        }
        return (double) helpfulVotes / totalVotes;
    }

    /**
     * Checks if this review has significant voting activity.
     * 
     * @return true if the review has at least 5 votes
     */
    public boolean hasSignificantVoting() {
        return totalVotes >= 5;
    }
} 