package com.showsync.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity representing a media item in a group's collection list.
 * Groups can collectively manage their media lists with different statuses.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Entity
@Table(name = "group_media_lists")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"group", "media", "addedBy"})
@ToString(exclude = {"group", "media", "addedBy"})
public class GroupMediaList {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "media_id", nullable = false)
    private Media media;

    @Column(name = "list_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ListType listType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", nullable = false)
    private User addedBy;

    @Column(name = "group_rating")
    private Double groupRating;

    @Column(name = "total_votes", nullable = false)
    private Integer totalVotes = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Types of media lists that groups can maintain
     */
    public enum ListType {
        /**
         * Media the group is currently watching/reading together
         */
        CURRENTLY_WATCHING,
        
        /**
         * Media the group has completed
         */
        COMPLETED,
        
        /**
         * Media the group plans to watch/read
         */
        PLAN_TO_WATCH,
        
        /**
         * Media the group decided to drop
         */
        DROPPED,
        
        /**
         * Media the group put on hold
         */
        ON_HOLD
    }

    /**
     * Updates the group rating and vote count.
     * This method should be called when member ratings change.
     * 
     * @param newRating the new aggregated rating
     * @param voteCount the total number of votes
     */
    public void updateGroupRating(Double newRating, Integer voteCount) {
        this.groupRating = newRating;
        this.totalVotes = voteCount != null ? voteCount : 0;
    }

    /**
     * Checks if the media has received any ratings from group members.
     * 
     * @return true if the media has group ratings
     */
    public boolean hasGroupRating() {
        return groupRating != null && totalVotes > 0;
    }

    /**
     * Gets a formatted rating string for display.
     * 
     * @return formatted rating string (e.g., "8.2 (5 votes)")
     */
    public String getFormattedRating() {
        if (!hasGroupRating()) {
            return "No ratings yet";
        }
        return String.format("%.1f (%d vote%s)", groupRating, totalVotes, totalVotes == 1 ? "" : "s");
    }
} 