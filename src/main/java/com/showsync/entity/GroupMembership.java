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
 * Entity representing membership of a user in a group.
 * This is a junction table that manages the many-to-many relationship
 * between users and groups with additional metadata.
 */
@Data
@Entity
@Table(name = "group_memberships")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"user", "group"})
@ToString(exclude = {"user", "group"})
public class GroupMembership {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipRole role = MembershipRole.MEMBER;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Roles that define a member's permissions within a group
     */
    public enum MembershipRole {
        /**
         * Group creator with full administrative permissions
         */
        OWNER,
        
        /**
         * Administrative permissions delegated by owner
         */
        ADMIN,
        
        /**
         * Standard member with basic permissions
         */
        MEMBER
    }

    /**
     * Status of the membership
     */
    public enum MembershipStatus {
        /**
         * Active member with full access
         */
        ACTIVE,
        
        /**
         * Pending approval (for private groups)
         */
        PENDING,
        
        /**
         * Banned from the group
         */
        BANNED
    }

    /**
     * Check if this member has administrative privileges
     * @return true if member is owner or admin
     */
    public boolean hasAdminPrivileges() {
        return role == MembershipRole.OWNER || role == MembershipRole.ADMIN;
    }

    /**
     * Check if this member is the group owner
     * @return true if member is the owner
     */
    public boolean isOwner() {
        return role == MembershipRole.OWNER;
    }

    /**
     * Check if this membership is active
     * @return true if status is active
     */
    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }

    /**
     * Promote member to admin role (only if currently a member)
     */
    public void promoteToAdmin() {
        if (role == MembershipRole.MEMBER) {
            this.role = MembershipRole.ADMIN;
        }
    }

    /**
     * Demote admin to member role (only if currently an admin, not owner)
     */
    public void demoteToMember() {
        if (role == MembershipRole.ADMIN) {
            this.role = MembershipRole.MEMBER;
        }
    }
} 