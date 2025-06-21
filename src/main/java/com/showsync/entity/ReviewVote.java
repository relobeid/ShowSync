package com.showsync.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing votes on reviews (helpful/not helpful).
 * Ensures each user can only vote once per review.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Data
@Entity
@Table(name = "review_votes",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "review_id"}))
@EntityListeners(AuditingEntityListener.class)
public class ReviewVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "is_helpful", nullable = false)
    private boolean isHelpful;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Creates a helpful vote.
     * 
     * @param user The user casting the vote
     * @param review The review being voted on
     * @return ReviewVote instance with helpful vote
     */
    public static ReviewVote createHelpfulVote(User user, Review review) {
        ReviewVote vote = new ReviewVote();
        vote.setUser(user);
        vote.setReview(review);
        vote.setHelpful(true);
        return vote;
    }

    /**
     * Creates a not helpful vote.
     * 
     * @param user The user casting the vote
     * @param review The review being voted on
     * @return ReviewVote instance with not helpful vote
     */
    public static ReviewVote createNotHelpfulVote(User user, Review review) {
        ReviewVote vote = new ReviewVote();
        vote.setUser(user);
        vote.setReview(review);
        vote.setHelpful(false);
        return vote;
    }
} 