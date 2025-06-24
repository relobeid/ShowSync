package com.showsync.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Entity representing a user group for media discovery and discussion.
 * Groups allow users to collaborate on media recommendations and activities.
 */
@Data
@Entity
@Table(name = "groups")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"memberships"})
@ToString(exclude = {"memberships"})
public class Group {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(name = "privacy_setting", nullable = false)
    @Enumerated(EnumType.STRING)
    private PrivacySetting privacySetting = PrivacySetting.PUBLIC;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "max_members")
    private Integer maxMembers = 50;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<GroupMembership> memberships = new HashSet<>();

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Privacy settings for group visibility and access
     */
    public enum PrivacySetting {
        /**
         * Group is publicly visible and anyone can join
         */
        PUBLIC,
        
        /**
         * Group is visible but requires approval to join
         */
        PRIVATE
    }

    /**
     * Get the count of active members in this group
     * @return number of active members
     */
    public long getActiveMemberCount() {
        return memberships.stream()
                .filter(membership -> membership.getStatus() == GroupMembership.MembershipStatus.ACTIVE)
                .count();
    }

    /**
     * Check if the group has reached its maximum member capacity
     * @return true if group is at capacity
     */
    public boolean isAtCapacity() {
        return maxMembers != null && getActiveMemberCount() >= maxMembers;
    }

    /**
     * Check if a user is an active member of this group
     * @param user the user to check
     * @return true if user is an active member
     */
    public boolean hasActiveMember(User user) {
        return memberships.stream()
                .anyMatch(membership -> 
                    membership.getUser().equals(user) && 
                    membership.getStatus() == GroupMembership.MembershipStatus.ACTIVE);
    }

    /**
     * Get the membership for a specific user
     * @param user the user
     * @return the membership if found, null otherwise
     */
    public GroupMembership getMembershipForUser(User user) {
        return memberships.stream()
                .filter(membership -> membership.getUser().equals(user))
                .findFirst()
                .orElse(null);
    }
} 