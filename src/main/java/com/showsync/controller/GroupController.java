package com.showsync.controller;

import com.showsync.dto.group.*;
import com.showsync.entity.Group;
import com.showsync.entity.GroupMembership;
import com.showsync.entity.User;
import com.showsync.security.UserPrincipal;
import com.showsync.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for group management operations.
 * Provides endpoints for group CRUD operations and membership management.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Slf4j
@RestController
@RequestMapping("/api/groups")
@Tag(name = "Groups", description = "Group management and membership operations")
public class GroupController {

    private final GroupService groupService;
    private final GroupMapper groupMapper;

    @Autowired
    public GroupController(GroupService groupService, GroupMapper groupMapper) {
        this.groupService = groupService;
        this.groupMapper = groupMapper;
    }

    @PostMapping
    @Operation(summary = "Create a new group", description = "Create a new group with the authenticated user as owner")
    public ResponseEntity<?> createGroup(
            @Valid @RequestBody CreateGroupRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            Group group = groupService.createGroup(request, currentUser);
            GroupResponse response = groupMapper.toResponse(group, currentUser);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error creating group", e);
            return createErrorResponse("Failed to create group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{groupId}")
    @Operation(summary = "Get group details", description = "Retrieve detailed information about a specific group")
    public ResponseEntity<?> getGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal != null ? userPrincipal.getUser() : null;
            return groupService.getGroupById(groupId, currentUser)
                    .map(group -> {
                        GroupResponse response = groupMapper.toResponse(group, currentUser);
                        return ResponseEntity.ok(response);
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error retrieving group: {}", groupId, e);
            return createErrorResponse("Failed to retrieve group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{groupId}")
    @Operation(summary = "Update group", description = "Update group information (owners and admins only)")
    public ResponseEntity<?> updateGroup(
            @PathVariable Long groupId,
            @Valid @RequestBody UpdateGroupRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            Group group = groupService.updateGroup(groupId, request, currentUser);
            GroupResponse response = groupMapper.toResponse(group, currentUser);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error updating group: {}", groupId, e);
            return createErrorResponse("Failed to update group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{groupId}")
    @Operation(summary = "Delete group", description = "Soft delete a group (owners only)")
    public ResponseEntity<?> deleteGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            groupService.deleteGroup(groupId, currentUser);
            
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error deleting group: {}", groupId, e);
            return createErrorResponse("Failed to delete group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "Search groups", description = "Search for groups by name")
    public ResponseEntity<?> searchGroups(
            @Parameter(description = "Search term for group names") @RequestParam(required = false) String q,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal != null ? userPrincipal.getUser() : null;
            Pageable pageable = PageRequest.of(page, Math.min(size, 100)); // Limit max page size
            
            Page<Group> groups = groupService.searchGroups(q, pageable, currentUser);
            Page<GroupResponse> response = groups.map(group -> groupMapper.toResponse(group, currentUser));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error searching groups", e);
            return createErrorResponse("Failed to search groups", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/public")
    @Operation(summary = "Get public groups", description = "Retrieve all public groups with pagination")
    public ResponseEntity<?> getPublicGroups(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal != null ? userPrincipal.getUser() : null;
            Pageable pageable = PageRequest.of(page, Math.min(size, 100));
            
            Page<Group> groups = groupService.getPublicGroups(pageable, currentUser);
            Page<GroupResponse> response = groups.map(group -> groupMapper.toResponse(group, currentUser));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving public groups", e);
            return createErrorResponse("Failed to retrieve public groups", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/my")
    @Operation(summary = "Get user's groups", description = "Retrieve groups where the user is a member")
    public ResponseEntity<?> getMyGroups(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            Pageable pageable = PageRequest.of(page, Math.min(size, 100));
            
            Page<Group> groups = groupService.getUserGroups(currentUser, pageable);
            Page<GroupResponse> response = groups.map(group -> groupMapper.toResponse(group, currentUser));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving user groups", e);
            return createErrorResponse("Failed to retrieve your groups", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{groupId}/join")
    @Operation(summary = "Join a group", description = "Join a public group or request to join a private group")
    public ResponseEntity<?> joinGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            GroupMembership membership = groupService.joinGroup(groupId, currentUser);
            GroupMemberResponse response = groupMapper.toMemberResponse(membership);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error joining group: {}", groupId, e);
            return createErrorResponse("Failed to join group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{groupId}/leave")
    @Operation(summary = "Leave a group", description = "Leave a group (remove own membership)")
    public ResponseEntity<?> leaveGroup(
            @PathVariable Long groupId,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            groupService.leaveGroup(groupId, currentUser, currentUser);
            
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error leaving group: {}", groupId, e);
            return createErrorResponse("Failed to leave group", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{groupId}/members")
    @Operation(summary = "Get group members", description = "Retrieve members of a group with pagination")
    public ResponseEntity<?> getGroupMembers(
            @PathVariable Long groupId,
            @Parameter(description = "Filter by membership status") @RequestParam(required = false) GroupMembership.MembershipStatus status,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            Pageable pageable = PageRequest.of(page, Math.min(size, 100));
            
            Page<GroupMembership> memberships = groupService.getGroupMembers(groupId, status, pageable, currentUser);
            Page<GroupMemberResponse> response = memberships.map(groupMapper::toMemberResponse);
            
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (SecurityException e) {
            return createErrorResponse(e.getMessage(), HttpStatus.FORBIDDEN);
        } catch (Exception e) {
            log.error("Error retrieving group members: {}", groupId, e);
            return createErrorResponse("Failed to retrieve group members", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get recommended groups", description = "Get groups recommended for the user")
    public ResponseEntity<?> getRecommendedGroups(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        try {
            User currentUser = userPrincipal.getUser();
            Pageable pageable = PageRequest.of(page, Math.min(size, 100));
            
            Page<Group> groups = groupService.getRecommendedGroups(currentUser, pageable);
            Page<GroupResponse> response = groups.map(group -> groupMapper.toResponse(group, currentUser));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving recommended groups", e);
            return createErrorResponse("Failed to retrieve recommended groups", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/check-name")
    @Operation(summary = "Check group name availability", description = "Check if a group name is available")
    public ResponseEntity<?> checkGroupNameAvailability(
            @Parameter(description = "Group name to check") @RequestParam String name) {
        try {
            boolean available = groupService.isGroupNameAvailable(name);
            Map<String, Object> response = new HashMap<>();
            response.put("name", name);
            response.put("available", available);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error checking group name availability", e);
            return createErrorResponse("Failed to check name availability", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Helper method to create standardized error responses
     */
    private ResponseEntity<Map<String, String>> createErrorResponse(String message, HttpStatus status) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return ResponseEntity.status(status).body(error);
    }
} 