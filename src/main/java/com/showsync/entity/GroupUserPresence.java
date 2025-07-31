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
 * Entity representing user presence and activity status within a group.
 * Tracks online status, last seen times, and activity for real-time features.
 */
@Data
@Entity
@Table(name = "group_user_presence", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "user_id"}))
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"group", "user"})
@ToString(exclude = {"group", "user"})
public class GroupUserPresence {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "is_online", nullable = false)
    private boolean isOnline = false;
    
    @Column(name = "last_seen_at", nullable = false)
    private LocalDateTime lastSeenAt;
    
    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;
    
    /**
     * Constructor for creating presence tracking
     */
    public GroupUserPresence(Group group, User user) {
        this.group = group;
        this.user = user;
        this.isOnline = true;
        this.lastSeenAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /**
     * Default constructor for JPA
     */
    public GroupUserPresence() {
        this.lastSeenAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /**
     * Mark user as online and update activity timestamp
     */
    public void markOnline() {
        this.isOnline = true;
        this.lastSeenAt = LocalDateTime.now();
        this.lastActiveAt = LocalDateTime.now();
    }
    
    /**
     * Mark user as offline
     */
    public void markOffline() {
        this.isOnline = false;
        this.lastSeenAt = LocalDateTime.now();
    }
    
    /**
     * Update last activity timestamp (for typing indicators, message sending, etc.)
     */
    public void updateActivity() {
        this.lastActiveAt = LocalDateTime.now();
        if (this.isOnline) {
            this.lastSeenAt = LocalDateTime.now();
        }
    }
    
    /**
     * Check if user was recently active (within last 5 minutes)
     */
    public boolean isRecentlyActive() {
        return LocalDateTime.now().minusMinutes(5).isBefore(lastActiveAt);
    }
    
    /**
     * Check if user was recently seen (within last 15 minutes)
     */
    public boolean isRecentlySeen() {
        return LocalDateTime.now().minusMinutes(15).isBefore(lastSeenAt);
    }
} 