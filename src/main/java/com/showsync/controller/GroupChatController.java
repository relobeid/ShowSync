package com.showsync.controller;

import com.showsync.dto.chat.ChatMessageRequest;
import com.showsync.dto.chat.ChatMessageResponse;
import com.showsync.dto.chat.UserPresenceResponse;
import com.showsync.security.UserPrincipal;
import com.showsync.service.GroupChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST API controller for group chat functionality.
 * Provides endpoints for chat history, message management, and presence information.
 */
@RestController
@RequestMapping("/api/groups/{groupId}/chat")
@Tag(name = "Group Chat", description = "Group chat messaging and history management")
public class GroupChatController {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupChatController.class);
    
    @Autowired
    private GroupChatService chatService;
    
    /**
     * Get chat message history for a group
     */
    @GetMapping("/messages")
    @Operation(summary = "Get chat history", description = "Retrieve paginated chat message history for a group")
    public ResponseEntity<Page<ChatMessageResponse>> getChatHistory(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.debug("Getting chat history for group {} by user {}", groupId, userPrincipal.getId());
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageResponse> messages = chatService.getChatHistory(groupId, userPrincipal.getId(), pageable);
        
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get recent messages for initial chat load
     */
    @GetMapping("/messages/recent")
    @Operation(summary = "Get recent messages", description = "Get the most recent messages for initial chat loading")
    public ResponseEntity<List<ChatMessageResponse>> getRecentMessages(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "Number of messages to retrieve") @RequestParam(defaultValue = "50") int limit,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.debug("Getting {} recent messages for group {} by user {}", limit, groupId, userPrincipal.getId());
        
        List<ChatMessageResponse> messages = chatService.getRecentMessages(groupId, userPrincipal.getId(), limit);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get messages since a specific timestamp (for sync)
     */
    @GetMapping("/messages/since")
    @Operation(summary = "Get messages since timestamp", description = "Get messages created after a specific timestamp for real-time sync")
    public ResponseEntity<List<ChatMessageResponse>> getMessagesSince(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "ISO timestamp") @RequestParam String since,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        try {
            LocalDateTime sinceTime = LocalDateTime.parse(since);
            List<ChatMessageResponse> messages = chatService.getMessagesSince(groupId, userPrincipal.getId(), sinceTime);
            return ResponseEntity.ok(messages);
        } catch (Exception e) {
            logger.error("Invalid timestamp format: {}", since);
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Send a message via REST API (alternative to WebSocket)
     */
    @PostMapping("/messages")
    @Operation(summary = "Send message", description = "Send a chat message via REST API")
    public ResponseEntity<ChatMessageResponse> sendMessage(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Valid @RequestBody ChatMessageRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        logger.debug("Sending message to group {} from user {} via REST", groupId, userPrincipal.getId());
        
        ChatMessageResponse response = chatService.sendMessage(groupId, userPrincipal.getId(), request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Edit a message
     */
    @PutMapping("/messages/{messageId}")
    @Operation(summary = "Edit message", description = "Edit an existing chat message")
    public ResponseEntity<ChatMessageResponse> editMessage(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "Message ID") @PathVariable Long messageId,
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        String newContent = request.get("content");
        if (newContent == null || newContent.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        ChatMessageResponse response = chatService.editMessage(groupId, userPrincipal.getId(), messageId, newContent);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Delete a message
     */
    @DeleteMapping("/messages/{messageId}")
    @Operation(summary = "Delete message", description = "Delete a chat message (soft delete)")
    public ResponseEntity<Void> deleteMessage(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "Message ID") @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        chatService.deleteMessage(groupId, userPrincipal.getId(), messageId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Get a specific message
     */
    @GetMapping("/messages/{messageId}")
    @Operation(summary = "Get message", description = "Get a specific message by ID")
    public ResponseEntity<ChatMessageResponse> getMessage(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "Message ID") @PathVariable Long messageId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        ChatMessageResponse message = chatService.getMessage(groupId, userPrincipal.getId(), messageId);
        return ResponseEntity.ok(message);
    }
    
    /**
     * Search messages in a group
     */
    @GetMapping("/messages/search")
    @Operation(summary = "Search messages", description = "Search for messages containing specific text")
    public ResponseEntity<Page<ChatMessageResponse>> searchMessages(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @Parameter(description = "Search term") @RequestParam String query,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatMessageResponse> messages = chatService.searchMessages(groupId, userPrincipal.getId(), query, pageable);
        return ResponseEntity.ok(messages);
    }
    
    /**
     * Get online users in the group
     */
    @GetMapping("/presence/online")
    @Operation(summary = "Get online users", description = "Get list of currently online users in the group")
    public ResponseEntity<List<UserPresenceResponse>> getOnlineUsers(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // Validate access
        if (!chatService.canUserAccessGroupChat(groupId, userPrincipal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UserPresenceResponse> onlineUsers = chatService.getOnlineUsers(groupId);
        return ResponseEntity.ok(onlineUsers);
    }
    
    /**
     * Get all user presence information for the group
     */
    @GetMapping("/presence")
    @Operation(summary = "Get user presence", description = "Get presence information for all users in the group")
    public ResponseEntity<List<UserPresenceResponse>> getAllUserPresence(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // Validate access
        if (!chatService.canUserAccessGroupChat(groupId, userPrincipal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        List<UserPresenceResponse> allPresence = chatService.getAllUserPresence(groupId);
        return ResponseEntity.ok(allPresence);
    }
    
    /**
     * Get chat statistics for the group
     */
    @GetMapping("/stats")
    @Operation(summary = "Get chat statistics", description = "Get chat statistics and metrics for the group")
    public ResponseEntity<Map<String, Object>> getChatStats(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        // Validate access
        if (!chatService.canUserAccessGroupChat(groupId, userPrincipal.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        long messageCount = chatService.getMessageCount(groupId);
        long onlineUserCount = chatService.getOnlineUserCount(groupId);
        
        Map<String, Object> stats = Map.of(
            "totalMessages", messageCount,
            "onlineUsers", onlineUserCount,
            "timestamp", LocalDateTime.now()
        );
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Mark user as online (for manual presence management)
     */
    @PostMapping("/presence/online")
    @Operation(summary = "Mark online", description = "Manually mark user as online in the group")
    public ResponseEntity<Void> markOnline(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        chatService.markUserOnline(groupId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
    
    /**
     * Mark user as offline (for manual presence management)
     */
    @PostMapping("/presence/offline")
    @Operation(summary = "Mark offline", description = "Manually mark user as offline in the group")
    public ResponseEntity<Void> markOffline(
            @Parameter(description = "Group ID") @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        
        chatService.markUserOffline(groupId, userPrincipal.getId());
        return ResponseEntity.ok().build();
    }
} 