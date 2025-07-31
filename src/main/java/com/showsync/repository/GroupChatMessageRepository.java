package com.showsync.repository;

import com.showsync.entity.GroupChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for GroupChatMessage entity.
 * Provides data access methods for chat message operations.
 */
@Repository
public interface GroupChatMessageRepository extends JpaRepository<GroupChatMessage, Long> {
    
    /**
     * Find all visible (non-deleted) messages for a group, ordered by creation time
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<GroupChatMessage> findByGroupIdAndVisibleOrderByCreatedAtDesc(
            @Param("groupId") Long groupId, 
            Pageable pageable);
    
    /**
     * Find recent messages for a group (last N messages)
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    List<GroupChatMessage> findRecentMessagesByGroupId(
            @Param("groupId") Long groupId, 
            Pageable pageable);
    
    /**
     * Find messages after a specific timestamp (for real-time updates)
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.isDeleted = false " +
           "AND m.createdAt > :since " +
           "ORDER BY m.createdAt ASC")
    List<GroupChatMessage> findMessagesSince(
            @Param("groupId") Long groupId, 
            @Param("since") LocalDateTime since);
    
    /**
     * Find a specific message by ID and group (for security validation)
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.id = :messageId AND m.group.id = :groupId")
    Optional<GroupChatMessage> findByIdAndGroupId(
            @Param("messageId") Long messageId, 
            @Param("groupId") Long groupId);
    
    /**
     * Count total messages in a group
     */
    @Query("SELECT COUNT(m) FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.isDeleted = false")
    long countByGroupIdAndVisible(@Param("groupId") Long groupId);
    
    /**
     * Count messages by user in a group
     */
    @Query("SELECT COUNT(m) FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.user.id = :userId AND m.isDeleted = false")
    long countByGroupIdAndUserId(
            @Param("groupId") Long groupId, 
            @Param("userId") Long userId);
    
    /**
     * Find messages by user in a group
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.user.id = :userId AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    Page<GroupChatMessage> findByGroupIdAndUserIdOrderByCreatedAtDesc(
            @Param("groupId") Long groupId, 
            @Param("userId") Long userId, 
            Pageable pageable);
    
    /**
     * Find messages that are replies to a specific message
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.replyToMessage.id = :messageId AND m.isDeleted = false " +
           "ORDER BY m.createdAt ASC")
    List<GroupChatMessage> findRepliesByMessageId(@Param("messageId") Long messageId);
    
    /**
     * Search messages by content (for future search functionality)
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.isDeleted = false " +
           "AND LOWER(m.messageContent) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "ORDER BY m.createdAt DESC")
    Page<GroupChatMessage> searchByContent(
            @Param("groupId") Long groupId, 
            @Param("searchTerm") String searchTerm, 
            Pageable pageable);
    
    /**
     * Find system messages for a group (for activity tracking)
     */
    @Query("SELECT m FROM GroupChatMessage m " +
           "WHERE m.group.id = :groupId AND m.messageType = 'SYSTEM' AND m.isDeleted = false " +
           "ORDER BY m.createdAt DESC")
    List<GroupChatMessage> findSystemMessagesByGroupId(
            @Param("groupId") Long groupId, 
            Pageable pageable);
} 