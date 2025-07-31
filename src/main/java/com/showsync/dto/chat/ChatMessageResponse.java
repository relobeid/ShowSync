package com.showsync.dto.chat;

import com.showsync.entity.GroupChatMessage;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for chat messages.
 * Used for both WebSocket and REST API responses.
 */
@Data
public class ChatMessageResponse {
    
    private Long id;
    private Long groupId;
    private Long userId;
    private String username;
    private String displayName;
    private String content;
    private GroupChatMessage.MessageType messageType;
    private boolean isEdited;
    private boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Reply information
    private Long replyToMessageId;
    private String replyToContent;
    private String replyToUsername;
    
    /**
     * Default constructor
     */
    public ChatMessageResponse() {}
    
    /**
     * Constructor from entity
     */
    public ChatMessageResponse(GroupChatMessage message) {
        this.id = message.getId();
        this.groupId = message.getGroup().getId();
        this.userId = message.getUser().getId();
        this.username = message.getUser().getUsername();
        this.displayName = message.getUser().getDisplayName();
        this.content = message.getMessageContent();
        this.messageType = message.getMessageType();
        this.isEdited = message.isEdited();
        this.isDeleted = message.isDeleted();
        this.createdAt = message.getCreatedAt();
        this.updatedAt = message.getUpdatedAt();
        
        // Handle reply information
        if (message.getReplyToMessage() != null) {
            this.replyToMessageId = message.getReplyToMessage().getId();
            this.replyToContent = message.getReplyToMessage().getMessageContent();
            this.replyToUsername = message.getReplyToMessage().getUser().getUsername();
        }
    }
    
    /**
     * Check if this is a reply message
     */
    public boolean isReply() {
        return replyToMessageId != null;
    }
    
    /**
     * Check if message is visible (not deleted)
     */
    public boolean isVisible() {
        return !isDeleted;
    }
    
    /**
     * Get display content (handles deleted messages)
     */
    public String getDisplayContent() {
        if (isDeleted) {
            return "[Message deleted]";
        }
        return content;
    }
    
    /**
     * Static factory method for creating response from entity
     */
    public static ChatMessageResponse fromEntity(GroupChatMessage message) {
        return new ChatMessageResponse(message);
    }
    
    /**
     * Create a system message response
     */
    public static ChatMessageResponse createSystemMessage(Long groupId, String content, LocalDateTime timestamp) {
        ChatMessageResponse response = new ChatMessageResponse();
        response.setGroupId(groupId);
        response.setContent(content);
        response.setMessageType(GroupChatMessage.MessageType.SYSTEM);
        response.setUsername("System");
        response.setDisplayName("System");
        response.setCreatedAt(timestamp);
        response.setUpdatedAt(timestamp);
        return response;
    }
} 