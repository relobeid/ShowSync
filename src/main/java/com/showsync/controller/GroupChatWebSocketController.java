package com.showsync.controller;

import com.showsync.config.WebSocketConfig;
import com.showsync.dto.chat.ChatMessageRequest;
import com.showsync.dto.chat.ChatMessageResponse;
import com.showsync.service.GroupChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

/**
 * WebSocket controller for real-time group chat functionality.
 * Handles real-time messaging, presence updates, and live notifications.
 */
@Controller
public class GroupChatWebSocketController {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupChatWebSocketController.class);
    
    @Autowired
    private GroupChatService chatService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    /**
     * Handle incoming chat messages via WebSocket
     * Endpoint: /app/chat/{groupId}/send
     */
    @MessageMapping("/chat/{groupId}/send")
    public void sendMessage(
            @DestinationVariable Long groupId,
            @Payload ChatMessageRequest request,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            // Extract user ID from WebSocket session
            Long userId = WebSocketConfig.getUserIdFromSession(headerAccessor);
            if (userId == null) {
                logger.warn("No user ID found in WebSocket session for group {}", groupId);
                return;
            }
            
            logger.debug("Received message from user {} to group {}: {}", userId, groupId, request.getContent());
            
            // Send message through service (which will broadcast to all subscribers)
            ChatMessageResponse response = chatService.sendMessage(groupId, userId, request);
            
            // Update user activity
            chatService.updateUserActivity(groupId, userId);
            
            logger.debug("Message sent successfully: {}", response.getId());
            
        } catch (Exception e) {
            logger.error("Error sending message to group {}: {}", groupId, e.getMessage(), e);
            
            // Send error message back to sender
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getUser().getName(),
                "/queue/errors",
                "Failed to send message: " + e.getMessage()
            );
        }
    }
    
    /**
     * Handle user joining a group chat
     * Endpoint: /app/chat/{groupId}/join
     */
    @MessageMapping("/chat/{groupId}/join")
    public void joinGroup(
            @DestinationVariable Long groupId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Long userId = WebSocketConfig.getUserIdFromSession(headerAccessor);
            String username = WebSocketConfig.getUsernameFromSession(headerAccessor);
            
            if (userId == null || username == null) {
                logger.warn("Invalid user session for group join: groupId={}", groupId);
                return;
            }
            
            logger.debug("User {} joining group chat {}", username, groupId);
            
            // Validate access
            if (!chatService.canUserAccessGroupChat(groupId, userId)) {
                logger.warn("User {} does not have access to group {}", userId, groupId);
                return;
            }
            
            // Mark user as online
            chatService.markUserOnline(groupId, userId);
            
            // Send system message about user joining
            chatService.sendUserJoinedMessage(groupId, username);
            
            logger.debug("User {} successfully joined group chat {}", username, groupId);
            
        } catch (Exception e) {
            logger.error("Error joining group {}: {}", groupId, e.getMessage(), e);
        }
    }
    
    /**
     * Handle user leaving a group chat
     * Endpoint: /app/chat/{groupId}/leave
     */
    @MessageMapping("/chat/{groupId}/leave")
    public void leaveGroup(
            @DestinationVariable Long groupId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Long userId = WebSocketConfig.getUserIdFromSession(headerAccessor);
            String username = WebSocketConfig.getUsernameFromSession(headerAccessor);
            
            if (userId == null || username == null) {
                logger.warn("Invalid user session for group leave: groupId={}", groupId);
                return;
            }
            
            logger.debug("User {} leaving group chat {}", username, groupId);
            
            // Mark user as offline
            chatService.markUserOffline(groupId, userId);
            
            // Send system message about user leaving
            chatService.sendUserLeftMessage(groupId, username);
            
            logger.debug("User {} successfully left group chat {}", username, groupId);
            
        } catch (Exception e) {
            logger.error("Error leaving group {}: {}", groupId, e.getMessage(), e);
        }
    }
    
    /**
     * Handle typing indicators
     * Endpoint: /app/chat/{groupId}/typing
     */
    @MessageMapping("/chat/{groupId}/typing")
    public void handleTyping(
            @DestinationVariable Long groupId,
            @Payload String action, // "start" or "stop"
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Long userId = WebSocketConfig.getUserIdFromSession(headerAccessor);
            String username = WebSocketConfig.getUsernameFromSession(headerAccessor);
            
            if (userId == null || username == null) {
                return;
            }
            
            // Update activity
            chatService.updateUserActivity(groupId, userId);
            
            // Broadcast typing indicator to other users
            messagingTemplate.convertAndSend(
                "/topic/group/" + groupId + "/typing",
                java.util.Map.of(
                    "userId", userId,
                    "username", username,
                    "action", action
                )
            );
            
        } catch (Exception e) {
            logger.error("Error handling typing indicator: {}", e.getMessage());
        }
    }
    
    /**
     * Handle subscription to group chat (when user first connects)
     * This sends initial data to the newly connected user
     */
    @SubscribeMapping("/topic/group/{groupId}/messages")
    public List<ChatMessageResponse> onSubscribeToMessages(
            @DestinationVariable Long groupId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Long userId = WebSocketConfig.getUserIdFromSession(headerAccessor);
            if (userId == null) {
                logger.warn("No user ID for subscription to group {}", groupId);
                return java.util.Collections.emptyList();
            }
            
            // Validate access
            if (!chatService.canUserAccessGroupChat(groupId, userId)) {
                logger.warn("User {} does not have access to group {}", userId, groupId);
                return java.util.Collections.emptyList();
            }
            
            // Return recent messages for initial load
            List<ChatMessageResponse> recentMessages = chatService.getRecentMessages(groupId, userId, 50);
            logger.debug("Sending {} recent messages to user {} for group {}", 
                        recentMessages.size(), userId, groupId);
            
            return recentMessages;
            
        } catch (Exception e) {
            logger.error("Error handling subscription to group {}: {}", groupId, e.getMessage(), e);
            return java.util.Collections.emptyList();
        }
    }
    
    /**
     * Handle subscription to presence updates
     */
    @SubscribeMapping("/topic/group/{groupId}/presence")
    public void onSubscribeToPresence(
            @DestinationVariable Long groupId,
            SimpMessageHeaderAccessor headerAccessor) {
        
        try {
            Long userId = WebSocketConfig.getUserIdFromSession(headerAccessor);
            if (userId == null) {
                return;
            }
            
            // Validate access
            if (!chatService.canUserAccessGroupChat(groupId, userId)) {
                return;
            }
            
            // Send current online users to the subscriber
            var onlineUsers = chatService.getOnlineUsers(groupId);
            messagingTemplate.convertAndSendToUser(
                headerAccessor.getUser().getName(),
                "/queue/group/" + groupId + "/presence/initial",
                onlineUsers
            );
            
        } catch (Exception e) {
            logger.error("Error handling presence subscription: {}", e.getMessage());
        }
    }
} 