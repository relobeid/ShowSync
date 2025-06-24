package com.showsync.dto.group;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.showsync.entity.GroupMembership;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for group member information
 */
@Data
public class GroupMemberResponse {
    
    private Long membershipId;
    private Long userId;
    private String username;
    private String displayName;
    private String profilePictureUrl;
    private GroupMembership.MembershipRole role;
    private GroupMembership.MembershipStatus status;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime joinedAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
} 