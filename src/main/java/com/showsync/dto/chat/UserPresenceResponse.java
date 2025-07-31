package com.showsync.dto.chat;

import com.showsync.entity.GroupUserPresence;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Response DTO for user presence information in groups.
 * Used for showing online status, activity, and presence indicators.
 */
@Data
public class UserPresenceResponse {
    
    private Long userId;
    private String username;
    private String displayName;
    private boolean isOnline;
    private LocalDateTime lastSeenAt;
    private LocalDateTime lastActiveAt;
    private boolean isRecentlyActive;
    private boolean isRecentlySeen;
    
    /**
     * Default constructor
     */
    public UserPresenceResponse() {}
    
    /**
     * Constructor from entity
     */
    public UserPresenceResponse(GroupUserPresence presence) {
        this.userId = presence.getUser().getId();
        this.username = presence.getUser().getUsername();
        this.displayName = presence.getUser().getDisplayName();
        this.isOnline = presence.isOnline();
        this.lastSeenAt = presence.getLastSeenAt();
        this.lastActiveAt = presence.getLastActiveAt();
        this.isRecentlyActive = presence.isRecentlyActive();
        this.isRecentlySeen = presence.isRecentlySeen();
    }
    
    /**
     * Get status text for display
     */
    public String getStatusText() {
        if (isOnline) {
            return "Online";
        } else if (isRecentlySeen) {
            return "Recently active";
        } else {
            return "Offline";
        }
    }
    
    /**
     * Get status indicator color
     */
    public String getStatusColor() {
        if (isOnline) {
            return "green";
        } else if (isRecentlySeen) {
            return "yellow";
        } else {
            return "gray";
        }
    }
    
    /**
     * Static factory method for creating response from entity
     */
    public static UserPresenceResponse fromEntity(GroupUserPresence presence) {
        return new UserPresenceResponse(presence);
    }
    
    /**
     * Convert list of entities to list of responses
     */
    public static List<UserPresenceResponse> fromEntities(List<GroupUserPresence> presences) {
        return presences.stream()
                .map(UserPresenceResponse::fromEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Create an offline status response
     */
    public static UserPresenceResponse createOfflineStatus(Long userId, String username, String displayName) {
        UserPresenceResponse response = new UserPresenceResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setDisplayName(displayName);
        response.setOnline(false);
        response.setLastSeenAt(LocalDateTime.now());
        response.setLastActiveAt(LocalDateTime.now());
        response.setRecentlyActive(false);
        response.setRecentlySeen(false);
        return response;
    }
} 