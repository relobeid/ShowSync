package com.showsync.repository;

import com.showsync.entity.GroupUserPresence;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupUserPresence entity.
 * Provides data access methods for user presence and activity tracking.
 */
@Repository
public interface GroupUserPresenceRepository extends JpaRepository<GroupUserPresence, Long> {
    
    /**
     * Find presence record for a specific user in a specific group
     */
    @Query("SELECT p FROM GroupUserPresence p " +
           "WHERE p.group.id = :groupId AND p.user.id = :userId")
    Optional<GroupUserPresence> findByGroupIdAndUserId(
            @Param("groupId") Long groupId, 
            @Param("userId") Long userId);
    
    /**
     * Find all currently online users in a group
     */
    @Query("SELECT p FROM GroupUserPresence p " +
           "WHERE p.group.id = :groupId AND p.isOnline = true " +
           "ORDER BY p.lastActiveAt DESC")
    List<GroupUserPresence> findOnlineUsersByGroupId(@Param("groupId") Long groupId);
    
    /**
     * Count online users in a group
     */
    @Query("SELECT COUNT(p) FROM GroupUserPresence p " +
           "WHERE p.group.id = :groupId AND p.isOnline = true")
    long countOnlineUsersByGroupId(@Param("groupId") Long groupId);
    
    /**
     * Find recently active users in a group (last 15 minutes)
     */
    @Query("SELECT p FROM GroupUserPresence p " +
           "WHERE p.group.id = :groupId " +
           "AND p.lastActiveAt > :since " +
           "ORDER BY p.lastActiveAt DESC")
    List<GroupUserPresence> findRecentlyActiveUsers(
            @Param("groupId") Long groupId, 
            @Param("since") LocalDateTime since);
    
    /**
     * Find all presence records for a user across all groups
     */
    @Query("SELECT p FROM GroupUserPresence p " +
           "WHERE p.user.id = :userId " +
           "ORDER BY p.lastActiveAt DESC")
    List<GroupUserPresence> findByUserId(@Param("userId") Long userId);
    
    /**
     * Find all presence records for a group
     */
    @Query("SELECT p FROM GroupUserPresence p " +
           "WHERE p.group.id = :groupId " +
           "ORDER BY p.lastActiveAt DESC")
    List<GroupUserPresence> findByGroupId(@Param("groupId") Long groupId);
    
    /**
     * Mark all users as offline for a specific group (for maintenance/cleanup)
     */
    @Modifying
    @Query("UPDATE GroupUserPresence p " +
           "SET p.isOnline = false, p.lastSeenAt = :timestamp " +
           "WHERE p.group.id = :groupId")
    void markAllUsersOfflineInGroup(
            @Param("groupId") Long groupId, 
            @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Mark a specific user as offline in all groups
     */
    @Modifying
    @Query("UPDATE GroupUserPresence p " +
           "SET p.isOnline = false, p.lastSeenAt = :timestamp " +
           "WHERE p.user.id = :userId")
    void markUserOfflineInAllGroups(
            @Param("userId") Long userId, 
            @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Clean up old presence records (users not seen for more than 30 days)
     */
    @Modifying
    @Query("DELETE FROM GroupUserPresence p " +
           "WHERE p.lastSeenAt < :cutoffDate")
    void deleteOldPresenceRecords(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Update activity timestamp for a user in a group
     */
    @Modifying
    @Query("UPDATE GroupUserPresence p " +
           "SET p.lastActiveAt = :timestamp, " +
           "    p.lastSeenAt = CASE WHEN p.isOnline = true THEN :timestamp ELSE p.lastSeenAt END " +
           "WHERE p.group.id = :groupId AND p.user.id = :userId")
    void updateActivity(
            @Param("groupId") Long groupId, 
            @Param("userId") Long userId, 
            @Param("timestamp") LocalDateTime timestamp);
    
    /**
     * Find users who should be marked as offline (no activity for 15+ minutes)
     */
    @Query("SELECT p FROM GroupUserPresence p " +
           "WHERE p.isOnline = true " +
           "AND p.lastActiveAt < :cutoffTime")
    List<GroupUserPresence> findUsersToMarkOffline(@Param("cutoffTime") LocalDateTime cutoffTime);
} 