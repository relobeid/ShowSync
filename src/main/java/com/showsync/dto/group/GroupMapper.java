package com.showsync.dto.group;

import com.showsync.entity.Group;
import com.showsync.entity.GroupMembership;
import com.showsync.entity.User;
import com.showsync.repository.GroupMembershipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between Group entities and DTOs.
 * Handles safe conversion with null checks and prevents exposure of sensitive data.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Component
public class GroupMapper {
    
    private final GroupMembershipRepository membershipRepository;
    
    @Autowired
    public GroupMapper(GroupMembershipRepository membershipRepository) {
        this.membershipRepository = membershipRepository;
    }
    
    /**
     * Converts a Group entity to GroupResponse DTO.
     * 
     * @param group The entity to convert
     * @param currentUser The current user (to determine membership status)
     * @return GroupResponse DTO
     */
    public GroupResponse toResponse(Group group, User currentUser) {
        if (group == null) {
            return null;
        }
        
        GroupResponse response = new GroupResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setPrivacySetting(group.getPrivacySetting());
        response.setMaxMembers(group.getMaxMembers());
        response.setActiveMemberCount(group.getActiveMemberCount());
        response.setActive(group.isActive());
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());
        
        // Creator information
        if (group.getCreatedBy() != null) {
            response.setCreatedById(group.getCreatedBy().getId());
            response.setCreatedByUsername(group.getCreatedBy().getUsername());
            response.setCreatedByDisplayName(group.getCreatedBy().getDisplayName());
        }
        
        // Current user's membership information - use repository instead of lazy collection
        if (currentUser != null) {
            Optional<GroupMembership> membership = membershipRepository.findByUserAndGroup(currentUser, group);
            if (membership.isPresent()) {
                response.setUserMember(true);
                response.setUserRole(membership.get().getRole().name());
            } else {
                response.setUserMember(false);
            }
        }
        
        return response;
    }
    
    /**
     * Converts a Group entity to GroupResponse DTO without user context.
     * 
     * @param group The entity to convert
     * @return GroupResponse DTO
     */
    public GroupResponse toResponse(Group group) {
        return toResponse(group, null);
    }
    
    /**
     * Converts a list of Group entities to GroupResponse DTOs.
     * 
     * @param groups List of entities to convert
     * @param currentUser The current user (to determine membership status)
     * @return List of GroupResponse DTOs
     */
    public List<GroupResponse> toResponseList(List<Group> groups, User currentUser) {
        if (groups == null) {
            return List.of();
        }
        
        return groups.stream()
                .map(group -> toResponse(group, currentUser))
                .collect(Collectors.toList());
    }
    
    /**
     * Converts a list of Group entities to GroupResponse DTOs without user context.
     * 
     * @param groups List of entities to convert
     * @return List of GroupResponse DTOs
     */
    public List<GroupResponse> toResponseList(List<Group> groups) {
        return toResponseList(groups, null);
    }
    
    /**
     * Converts a GroupMembership entity to GroupMemberResponse DTO.
     * 
     * @param membership The entity to convert
     * @return GroupMemberResponse DTO
     */
    public GroupMemberResponse toMemberResponse(GroupMembership membership) {
        if (membership == null) {
            return null;
        }
        
        GroupMemberResponse response = new GroupMemberResponse();
        response.setMembershipId(membership.getId());
        response.setRole(membership.getRole());
        response.setStatus(membership.getStatus());
        response.setJoinedAt(membership.getJoinedAt());
        response.setCreatedAt(membership.getCreatedAt());
        
        // User information
        if (membership.getUser() != null) {
            User user = membership.getUser();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setDisplayName(user.getDisplayName());
            response.setProfilePictureUrl(user.getProfilePictureUrl());
        }
        
        return response;
    }
    
    /**
     * Converts a list of GroupMembership entities to GroupMemberResponse DTOs.
     * 
     * @param memberships List of entities to convert
     * @return List of GroupMemberResponse DTOs
     */
    public List<GroupMemberResponse> toMemberResponseList(List<GroupMembership> memberships) {
        if (memberships == null) {
            return List.of();
        }
        
        return memberships.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Applies update request to group entity.
     * 
     * @param group The group to update
     * @param request The update request
     */
    public void updateGroupFromRequest(Group group, UpdateGroupRequest request) {
        if (group == null || request == null) {
            return;
        }
        
        if (request.getName() != null) {
            group.setName(request.getName());
        }
        
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription());
        }
        
        if (request.getPrivacySetting() != null) {
            group.setPrivacySetting(request.getPrivacySetting());
        }
        
        if (request.getMaxMembers() != null) {
            group.setMaxMembers(request.getMaxMembers());
        }
    }
} 