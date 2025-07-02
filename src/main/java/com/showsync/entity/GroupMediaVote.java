package com.showsync.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a vote on a media item within a group.
 * Group members can vote on what to watch next, skip, or prioritize.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Entity
@Table(name = "group_media_votes")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"group", "media", "user"})
@ToString(exclude = {"group", "media", "user"})
public class GroupMediaVote {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "vote_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private VoteType voteType;

    @Column(name = "suggested_at", nullable = false)
    private LocalDateTime suggestedAt = LocalDateTime.now();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Types of votes that can be cast on media within a group
     */
    public enum VoteType {
        /**
         * Vote to watch this media next
         */
        WATCH_NEXT,
        
        /**
         * Vote to skip this media
         */
        SKIP,
        
        /**
         * Mark as high priority
         */
        PRIORITY_HIGH,
        
        /**
         * Mark as low priority
         */
        PRIORITY_LOW
    }

    /**
     * Checks if this is a positive vote (supporting the media).
     * 
     * @return true if the vote is positive
     */
    public boolean isPositiveVote() {
        return voteType == VoteType.WATCH_NEXT || voteType == VoteType.PRIORITY_HIGH;
    }

    /**
     * Checks if this is a negative vote (opposing the media).
     * 
     * @return true if the vote is negative
     */
    public boolean isNegativeVote() {
        return voteType == VoteType.SKIP || voteType == VoteType.PRIORITY_LOW;
    }

    /**
     * Gets the weight of this vote for scoring calculations.
     * 
     * @return vote weight (positive for support, negative for opposition)
     */
    public int getVoteWeight() {
        return switch (voteType) {
            case WATCH_NEXT -> 3;
            case PRIORITY_HIGH -> 2;
            case PRIORITY_LOW -> -1;
            case SKIP -> -2;
        };
    }
} 