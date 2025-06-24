package com.showsync.dto.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.showsync.entity.Group;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for group information
 */
@Data
public class GroupResponse {
    
    private Long id;
    private String name;
    private String description;
    private Group.PrivacySetting privacySetting;
    private Long createdById;
    private String createdByUsername;
    private String createdByDisplayName;
    private Integer maxMembers;
    private long activeMemberCount;
    private boolean isActive;
    private boolean isUserMember;
    private String userRole; // User's role in this group (if member)
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
} 