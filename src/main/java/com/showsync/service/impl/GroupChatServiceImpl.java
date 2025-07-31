package com.showsync.service.impl;

import com.showsync.dto.chat.ChatMessageRequest;
import com.showsync.dto.chat.ChatMessageResponse;
import com.showsync.dto.chat.UserPresenceResponse;
import com.showsync.entity.*;
import com.showsync.repository.*;
import com.showsync.service.GroupChatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of GroupChatService.
 * Provides complete chat functionality with messaging, presence tracking, and real-time features.
 */
@Service
@Transactional
public class GroupChatServiceImpl implements GroupChatService {
    
    private static final Logger logger = LoggerFactory.getLogger(GroupChatServiceImpl.class);
    
    @Autowired
    private GroupChatMessageRepository messageRepository;
    
    @Autowired
    private GroupUserPresenceRepository presenceRepository;
    
    @Autowired
    private GroupRepository groupRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GroupMembershipRepository membershipRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    // Message Operations
    
    @Override
    public ChatMessageResponse sendMessage(Long groupId, Long userId, ChatMessageRequest request) {
        logger.debug("Sending message to group {} from user {}", groupId, userId);
        
        // Validate access
        if (!canUserAccessGroupChat(groupId, userId)) {
            throw new IllegalArgumentException("User does not have access to this group chat");
        }
        
        // Validate message
        if (!request.isValid()) {
            throw new IllegalArgumentException("Invalid message content or type");
        }
        
        // Get entities
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        // Create message
        GroupChatMessage message = new GroupChatMessage(group, user, request.getContent());
        message.setMessageType(request.getMessageType());
        
        // Handle reply
        if (request.isReply()) {
            GroupChatMessage replyToMessage = messageRepository.findByIdAndGroupId(
                    request.getReplyToMessageId(), groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Reply message not found"));
            message.setReplyToMessage(replyToMessage);
        }
        
        // Save message
        message = messageRepository.save(message);
        
        // Update user activity
        updateUserActivity(groupId, userId);
        
        // Create response
        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);
        
        // Send real-time notification
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/messages", response);
        
        logger.debug("Message sent successfully: {}", message.getId());
        return response;
    }
    
    @Override
    public ChatMessageResponse editMessage(Long groupId, Long userId, Long messageId, String newContent) {
        logger.debug("Editing message {} in group {} by user {}", messageId, groupId, userId);
        
        if (!canUserModifyMessage(groupId, userId, messageId)) {
            throw new IllegalArgumentException("User cannot edit this message");
        }
        
        GroupChatMessage message = messageRepository.findByIdAndGroupId(messageId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        if (message.isDeleted()) {
            throw new IllegalArgumentException("Cannot edit deleted message");
        }
        
        // Update message
        message.setMessageContent(newContent);
        message.setEdited(true);
        message = messageRepository.save(message);
        
        // Update user activity
        updateUserActivity(groupId, userId);
        
        // Create response
        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);
        
        // Send real-time notification
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/messages/edited", response);
        
        logger.debug("Message edited successfully: {}", messageId);
        return response;
    }
    
    @Override
    public void deleteMessage(Long groupId, Long userId, Long messageId) {
        logger.debug("Deleting message {} in group {} by user {}", messageId, groupId, userId);
        
        if (!canUserModifyMessage(groupId, userId, messageId)) {
            throw new IllegalArgumentException("User cannot delete this message");
        }
        
        GroupChatMessage message = messageRepository.findByIdAndGroupId(messageId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        // Soft delete
        message.setDeleted(true);
        messageRepository.save(message);
        
        // Update user activity
        updateUserActivity(groupId, userId);
        
        // Send real-time notification
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/messages/deleted", 
                java.util.Map.of("messageId", messageId));
        
        logger.debug("Message deleted successfully: {}", messageId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> getChatHistory(Long groupId, Long userId, Pageable pageable) {
        if (!canUserAccessGroupChat(groupId, userId)) {
            throw new IllegalArgumentException("User does not have access to this group chat");
        }
        
        Page<GroupChatMessage> messages = messageRepository.findByGroupIdAndVisibleOrderByCreatedAtDesc(
                groupId, pageable);
        
        return messages.map(ChatMessageResponse::fromEntity);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getRecentMessages(Long groupId, Long userId, int limit) {
        if (!canUserAccessGroupChat(groupId, userId)) {
            throw new IllegalArgumentException("User does not have access to this group chat");
        }
        
        Pageable pageable = PageRequest.of(0, limit);
        List<GroupChatMessage> messages = messageRepository.findRecentMessagesByGroupId(groupId, pageable);
        
        // Reverse to get chronological order
        return messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getMessagesSince(Long groupId, Long userId, LocalDateTime since) {
        if (!canUserAccessGroupChat(groupId, userId)) {
            throw new IllegalArgumentException("User does not have access to this group chat");
        }
        
        List<GroupChatMessage> messages = messageRepository.findMessagesSince(groupId, since);
        return messages.stream()
                .map(ChatMessageResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(readOnly = true)
    public ChatMessageResponse getMessage(Long groupId, Long userId, Long messageId) {
        if (!canUserAccessGroupChat(groupId, userId)) {
            throw new IllegalArgumentException("User does not have access to this group chat");
        }
        
        GroupChatMessage message = messageRepository.findByIdAndGroupId(messageId, groupId)
                .orElseThrow(() -> new IllegalArgumentException("Message not found"));
        
        return ChatMessageResponse.fromEntity(message);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<ChatMessageResponse> searchMessages(Long groupId, Long userId, String searchTerm, Pageable pageable) {
        if (!canUserAccessGroupChat(groupId, userId)) {
            throw new IllegalArgumentException("User does not have access to this group chat");
        }
        
        Page<GroupChatMessage> messages = messageRepository.searchByContent(groupId, searchTerm, pageable);
        return messages.map(ChatMessageResponse::fromEntity);
    }
    
    // Presence Operations
    
    @Override
    public void markUserOnline(Long groupId, Long userId) {
        logger.debug("Marking user {} online in group {}", userId, groupId);
        
        Optional<GroupUserPresence> existingPresence = presenceRepository.findByGroupIdAndUserId(groupId, userId);
        
        GroupUserPresence presence;
        if (existingPresence.isPresent()) {
            presence = existingPresence.get();
            presence.markOnline();
        } else {
            Group group = groupRepository.findById(groupId)
                    .orElseThrow(() -> new IllegalArgumentException("Group not found"));
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            presence = new GroupUserPresence(group, user);
        }
        
        presenceRepository.save(presence);
        
        // Notify other users
        UserPresenceResponse response = UserPresenceResponse.fromEntity(presence);
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/presence", response);
    }
    
    @Override
    public void markUserOffline(Long groupId, Long userId) {
        logger.debug("Marking user {} offline in group {}", userId, groupId);
        
        Optional<GroupUserPresence> presence = presenceRepository.findByGroupIdAndUserId(groupId, userId);
        if (presence.isPresent()) {
            presence.get().markOffline();
            presenceRepository.save(presence.get());
            
            // Notify other users
            UserPresenceResponse response = UserPresenceResponse.fromEntity(presence.get());
            messagingTemplate.convertAndSend("/topic/group/" + groupId + "/presence", response);
        }
    }
    
    @Override
    public void updateUserActivity(Long groupId, Long userId) {
        Optional<GroupUserPresence> presence = presenceRepository.findByGroupIdAndUserId(groupId, userId);
        if (presence.isPresent()) {
            presence.get().updateActivity();
            presenceRepository.save(presence.get());
        } else {
            // User not tracked yet, mark as online
            markUserOnline(groupId, userId);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserPresenceResponse> getOnlineUsers(Long groupId) {
        List<GroupUserPresence> onlineUsers = presenceRepository.findOnlineUsersByGroupId(groupId);
        return UserPresenceResponse.fromEntities(onlineUsers);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserPresenceResponse> getAllUserPresence(Long groupId) {
        List<GroupUserPresence> allPresence = presenceRepository.findByGroupId(groupId);
        return UserPresenceResponse.fromEntities(allPresence);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserPresenceResponse getUserPresence(Long groupId, Long userId) {
        Optional<GroupUserPresence> presence = presenceRepository.findByGroupIdAndUserId(groupId, userId);
        return presence.map(UserPresenceResponse::fromEntity)
                .orElse(null);
    }
    
    // System Messages
    
    @Override
    public ChatMessageResponse sendSystemMessage(Long groupId, String content) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
        
        GroupChatMessage message = GroupChatMessage.createSystemMessage(group, content);
        message = messageRepository.save(message);
        
        ChatMessageResponse response = ChatMessageResponse.fromEntity(message);
        
        // Send real-time notification
        messagingTemplate.convertAndSend("/topic/group/" + groupId + "/messages", response);
        
        return response;
    }
    
    @Override
    public ChatMessageResponse sendUserJoinedMessage(Long groupId, String username) {
        return sendSystemMessage(groupId, username + " joined the group");
    }
    
    @Override
    public ChatMessageResponse sendUserLeftMessage(Long groupId, String username) {
        return sendSystemMessage(groupId, username + " left the group");
    }
    
    // Statistics and Utility
    
    @Override
    @Transactional(readOnly = true)
    public long getMessageCount(Long groupId) {
        return messageRepository.countByGroupIdAndVisible(groupId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public long getOnlineUserCount(Long groupId) {
        return presenceRepository.countOnlineUsersByGroupId(groupId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessGroupChat(Long groupId, Long userId) {
        // Check if user is a member of the group
        Group group = groupRepository.findById(groupId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if (group == null || user == null) {
            return false;
        }
        return membershipRepository.findByUserAndGroup(user, group).isPresent();
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean canUserModifyMessage(Long groupId, Long userId, Long messageId) {
        if (!canUserAccessGroupChat(groupId, userId)) {
            return false;
        }
        
        Optional<GroupChatMessage> message = messageRepository.findByIdAndGroupId(messageId, groupId);
        if (message.isEmpty()) {
            return false;
        }
        
        // User can modify their own messages or group admins can modify any message
        return message.get().getUser().getId().equals(userId) || 
               isUserGroupAdmin(groupId, userId);
    }
    
    private boolean isUserGroupAdmin(Long groupId, Long userId) {
        Group group = groupRepository.findById(groupId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if (group == null || user == null) {
            return false;
        }
        
        Optional<GroupMembership> membership = membershipRepository.findByUserAndGroup(user, group);
        return membership.isPresent() && 
               (membership.get().getRole() == GroupMembership.MembershipRole.OWNER || 
                membership.get().getRole() == GroupMembership.MembershipRole.ADMIN);
    }
    
    // Cleanup Operations
    
    @Override
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void cleanupOfflineUsers() {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(15);
        List<GroupUserPresence> usersToMarkOffline = presenceRepository.findUsersToMarkOffline(cutoffTime);
        
        for (GroupUserPresence presence : usersToMarkOffline) {
            presence.markOffline();
            presenceRepository.save(presence);
            
            // Notify group about user going offline
            UserPresenceResponse response = UserPresenceResponse.fromEntity(presence);
            messagingTemplate.convertAndSend("/topic/group/" + presence.getGroup().getId() + "/presence", response);
        }
        
        if (!usersToMarkOffline.isEmpty()) {
            logger.debug("Marked {} users as offline due to inactivity", usersToMarkOffline.size());
        }
    }
    
    @Override
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldPresenceRecords() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        presenceRepository.deleteOldPresenceRecords(cutoffDate);
        logger.info("Cleaned up old presence records older than {}", cutoffDate);
    }
} 