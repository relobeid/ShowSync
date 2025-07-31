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
 * Entity representing a chat message within a group.
 * Supports text messages, system messages, and future message types.
 */
@Data
@Entity
@Table(name = "group_chat_messages")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"group", "user", "replyToMessage"})
@ToString(exclude = {"group", "user", "replyToMessage"})
public class GroupChatMessage {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Column(name = "message_content", nullable = false, columnDefinition = "TEXT")
    private String messageContent;
    
    @Column(name = "message_type", nullable = false, length = 50)
    @Enumerated(EnumType.STRING)
    private MessageType messageType = MessageType.TEXT;
    
    @Column(name = "is_edited", nullable = false)
    private boolean isEdited = false;
    
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_message_id")
    private GroupChatMessage replyToMessage;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * Message types for different kinds of chat messages
     */
    public enum MessageType {
        TEXT,           // Regular text message
        SYSTEM,         // System-generated message (user joined, left, etc.)
        MEDIA_SHARE,    // Shared media (movie, TV show, book)
        EMOJI,          // Emoji-only message
        ANNOUNCEMENT    // Important group announcements
    }
    
    /**
     * Convenience constructor for creating text messages
     */
    public GroupChatMessage(Group group, User user, String messageContent) {
        this.group = group;
        this.user = user;
        this.messageContent = messageContent;
        this.messageType = MessageType.TEXT;
    }
    
    /**
     * Convenience constructor for creating system messages
     */
    public static GroupChatMessage createSystemMessage(Group group, String messageContent) {
        GroupChatMessage message = new GroupChatMessage();
        message.setGroup(group);
        message.setMessageContent(messageContent);
        message.setMessageType(MessageType.SYSTEM);
        return message;
    }
    
    /**
     * Default constructor for JPA
     */
    public GroupChatMessage() {}
    
    /**
     * Check if this message is a reply to another message
     */
    public boolean isReply() {
        return replyToMessage != null;
    }
    
    /**
     * Check if this message is visible (not deleted)
     */
    public boolean isVisible() {
        return !isDeleted;
    }
} 