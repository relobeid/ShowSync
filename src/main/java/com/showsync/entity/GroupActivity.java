package com.showsync.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Entity representing an activity within a group for the activity feed.
 * Tracks various group activities like media additions, ratings, votes, etc.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Entity
@Table(name = "group_activities")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"group", "user", "targetMedia", "targetUser"})
@ToString(exclude = {"group", "user", "targetMedia", "targetUser"})
public class GroupActivity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "activity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_media_id")
    private Media targetMedia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_user_id")
    private User targetUser;

    @Column(name = "activity_data", columnDefinition = "TEXT")
    private String activityData;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Types of activities that can occur within a group
     */
    public enum ActivityType {
        // Media-related activities
        MEDIA_ADDED_TO_LIST,
        MEDIA_COMPLETED,
        MEDIA_RATING_UPDATED,
        MEDIA_REVIEW_POSTED,
        MEDIA_STATUS_CHANGED,
        MEDIA_VOTE_CAST,
        
        // Member-related activities  
        MEMBER_JOINED,
        MEMBER_LEFT,
        MEMBER_PROMOTED,
        MEMBER_DEMOTED,
        
        // Group-related activities
        GROUP_CREATED,
        GROUP_UPDATED,
        
        // Discussion activities (for future use)
        DISCUSSION_STARTED,
        COMMENT_POSTED
    }

    /**
     * Creates a simple activity without additional data.
     * 
     * @param group the group where the activity occurred
     * @param user the user who performed the activity
     * @param activityType the type of activity
     * @return new GroupActivity instance
     */
    public static GroupActivity create(Group group, User user, ActivityType activityType) {
        GroupActivity activity = new GroupActivity();
        activity.setGroup(group);
        activity.setUser(user);
        activity.setActivityType(activityType);
        return activity;
    }

    /**
     * Creates an activity related to a media item.
     * 
     * @param group the group where the activity occurred
     * @param user the user who performed the activity
     * @param activityType the type of activity
     * @param targetMedia the media item involved
     * @return new GroupActivity instance
     */
    public static GroupActivity createMediaActivity(Group group, User user, ActivityType activityType, Media targetMedia) {
        GroupActivity activity = create(group, user, activityType);
        activity.setTargetMedia(targetMedia);
        return activity;
    }

    /**
     * Creates an activity related to another user.
     * 
     * @param group the group where the activity occurred
     * @param user the user who performed the activity
     * @param activityType the type of activity
     * @param targetUser the user who was affected
     * @return new GroupActivity instance
     */
    public static GroupActivity createUserActivity(Group group, User user, ActivityType activityType, User targetUser) {
        GroupActivity activity = create(group, user, activityType);
        activity.setTargetUser(targetUser);
        return activity;
    }

    /**
     * Sets activity data as a JSON string.
     * 
     * @param data the activity data to store
     */
    public void setActivityDataMap(Map<String, Object> data) {
        // In a real implementation, you'd use ObjectMapper to convert to JSON
        // For now, we'll use a simple string representation
        this.activityData = data != null ? data.toString() : null;
    }

    /**
     * Checks if this activity is media-related.
     * 
     * @return true if the activity involves media
     */
    public boolean isMediaActivity() {
        return targetMedia != null || activityType.name().startsWith("MEDIA_");
    }

    /**
     * Checks if this activity is member-related.
     * 
     * @return true if the activity involves group membership
     */
    public boolean isMemberActivity() {
        return activityType.name().startsWith("MEMBER_");
    }
} 