package com.showsync.service;

import com.showsync.dto.chat.ChatMessageRequest;
import com.showsync.dto.chat.ChatMessageResponse;
import com.showsync.dto.chat.UserPresenceResponse;
import com.showsync.entity.GroupChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for group chat functionality.
 * Handles messaging, presence tracking, and real-time communication.
 */
public interface GroupChatService {
    
    // Message Operations
    
    /**
     * Send a message to a group
     */
    ChatMessageResponse sendMessage(Long groupId, Long userId, ChatMessageRequest request);
    
    /**
     * Edit an existing message
     */
    ChatMessageResponse editMessage(Long groupId, Long userId, Long messageId, String newContent);
    
    /**
     * Delete a message (soft delete)
     */
    void deleteMessage(Long groupId, Long userId, Long messageId);
    
    /**
     * Get chat history for a group with pagination
     */
    Page<ChatMessageResponse> getChatHistory(Long groupId, Long userId, Pageable pageable);
    
    /**
     * Get recent messages (for initial load)
     */
    List<ChatMessageResponse> getRecentMessages(Long groupId, Long userId, int limit);
    
    /**
     * Get messages since a specific timestamp (for real-time sync)
     */
    List<ChatMessageResponse> getMessagesSince(Long groupId, Long userId, LocalDateTime since);
    
    /**
     * Get a specific message by ID
     */
    ChatMessageResponse getMessage(Long groupId, Long userId, Long messageId);
    
    /**
     * Search messages in a group
     */
    Page<ChatMessageResponse> searchMessages(Long groupId, Long userId, String searchTerm, Pageable pageable);
    
    // Presence Operations
    
    /**
     * Mark user as online in a group
     */
    void markUserOnline(Long groupId, Long userId);
    
    /**
     * Mark user as offline in a group
     */
    void markUserOffline(Long groupId, Long userId);
    
    /**
     * Update user activity timestamp
     */
    void updateUserActivity(Long groupId, Long userId);
    
    /**
     * Get online users in a group
     */
    List<UserPresenceResponse> getOnlineUsers(Long groupId);
    
    /**
     * Get all user presence status in a group
     */
    List<UserPresenceResponse> getAllUserPresence(Long groupId);
    
    /**
     * Get user's presence status in a group
     */
    UserPresenceResponse getUserPresence(Long groupId, Long userId);
    
    // System Messages
    
    /**
     * Send a system message (user joined, left, etc.)
     */
    ChatMessageResponse sendSystemMessage(Long groupId, String content);
    
    /**
     * Send user joined message
     */
    ChatMessageResponse sendUserJoinedMessage(Long groupId, String username);
    
    /**
     * Send user left message
     */
    ChatMessageResponse sendUserLeftMessage(Long groupId, String username);
    
    // Statistics and Utility
    
    /**
     * Get total message count for a group
     */
    long getMessageCount(Long groupId);
    
    /**
     * Get online user count for a group
     */
    long getOnlineUserCount(Long groupId);
    
    /**
     * Validate if user can access group chat
     */
    boolean canUserAccessGroupChat(Long groupId, Long userId);
    
    /**
     * Validate if user can edit/delete message
     */
    boolean canUserModifyMessage(Long groupId, Long userId, Long messageId);
    
    // Cleanup Operations
    
    /**
     * Clean up offline users (mark users as offline after inactivity)
     */
    void cleanupOfflineUsers();
    
    /**
     * Clean up old presence records
     */
    void cleanupOldPresenceRecords();
} 