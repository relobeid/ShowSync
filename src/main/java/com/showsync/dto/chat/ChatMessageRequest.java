package com.showsync.dto.chat;

import com.showsync.entity.GroupChatMessage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for sending chat messages to a group.
 * Used in both WebSocket and REST API endpoints.
 */
@Data
public class ChatMessageRequest {
    
    @NotBlank(message = "Message content cannot be empty")
    @Size(max = 5000, message = "Message content cannot exceed 5000 characters")
    private String content;
    
    @NotNull(message = "Message type is required")
    private GroupChatMessage.MessageType messageType = GroupChatMessage.MessageType.TEXT;
    
    private Long replyToMessageId;
    
    /**
     * Default constructor
     */
    public ChatMessageRequest() {}
    
    /**
     * Constructor for text messages
     */
    public ChatMessageRequest(String content) {
        this.content = content;
        this.messageType = GroupChatMessage.MessageType.TEXT;
    }
    
    /**
     * Constructor for reply messages
     */
    public ChatMessageRequest(String content, Long replyToMessageId) {
        this.content = content;
        this.messageType = GroupChatMessage.MessageType.TEXT;
        this.replyToMessageId = replyToMessageId;
    }
    
    /**
     * Constructor with message type
     */
    public ChatMessageRequest(String content, GroupChatMessage.MessageType messageType) {
        this.content = content;
        this.messageType = messageType;
    }
    
    /**
     * Check if this is a reply message
     */
    public boolean isReply() {
        return replyToMessageId != null;
    }
    
    /**
     * Validate message content based on type
     */
    public boolean isValid() {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Different validation rules for different message types
        switch (messageType) {
            case TEXT:
            case ANNOUNCEMENT:
                return content.length() <= 5000;
            case EMOJI:
                return content.length() <= 100; // Emoji messages should be short
            case SYSTEM:
                return false; // System messages cannot be sent by users
            case MEDIA_SHARE:
                return content.length() <= 1000; // Media share with description
            default:
                return true;
        }
    }
} 