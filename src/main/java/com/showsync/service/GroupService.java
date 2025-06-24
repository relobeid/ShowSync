package com.showsync.service;

import com.showsync.dto.group.CreateGroupRequest;
import com.showsync.dto.group.UpdateGroupRequest;
import com.showsync.entity.Group;
import com.showsync.entity.GroupMembership;
import com.showsync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing groups and group memberships.
 * Handles all operations related to group creation, membership management,
 * and group-related business logic.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
public interface GroupService {
    
    /**
     * Creates a new group with the authenticated user as the owner.
     * Validates group name uniqueness and creates initial owner membership.
     * 
     * @param request Group creation request with name, description, privacy settings
     * @param createdBy The authenticated user creating the group
     * @return Created Group entity with owner membership
     * @throws IllegalArgumentException if group name already exists or is invalid
     * @throws SecurityException if user is not authorized to create groups
     */
    Group createGroup(CreateGroupRequest request, User createdBy);
    
    /**
     * Updates an existing group's information.
     * Only group owners and admins can update group settings.
     * 
     * @param groupId The ID of the group to update
     * @param request Update request with new group information
     * @param currentUser The authenticated user making the request
     * @return Updated Group entity
     * @throws IllegalArgumentException if group not found or new name conflicts
     * @throws SecurityException if user lacks permission to update group
     */
    Group updateGroup(Long groupId, UpdateGroupRequest request, User currentUser);
    
    /**
     * Retrieves a group by ID with membership context for the current user.
     * 
     * @param groupId The ID of the group to retrieve
     * @param currentUser The authenticated user (optional for membership context)
     * @return Optional Group entity
     */
    Optional<Group> getGroupById(Long groupId, User currentUser);
    
    /**
     * Retrieves a group by ID without user context.
     * 
     * @param groupId The ID of the group to retrieve
     * @return Optional Group entity
     */
    Optional<Group> getGroupById(Long groupId);
    
    /**
     * Searches for groups by name with pagination.
     * 
     * @param searchTerm Term to search for in group names (case-insensitive)
     * @param pageable Pagination information
     * @param currentUser The authenticated user (for membership context)
     * @return Page of matching groups
     */
    Page<Group> searchGroups(String searchTerm, Pageable pageable, User currentUser);
    
    /**
     * Gets all public groups with pagination.
     * 
     * @param pageable Pagination information
     * @param currentUser The authenticated user (for membership context)
     * @return Page of public groups
     */
    Page<Group> getPublicGroups(Pageable pageable, User currentUser);
    
    /**
     * Gets groups created by a specific user.
     * 
     * @param createdBy The user who created the groups
     * @param pageable Pagination information
     * @param currentUser The authenticated user (for membership context)
     * @return Page of groups created by the user
     */
    Page<Group> getGroupsByCreator(User createdBy, Pageable pageable, User currentUser);
    
    /**
     * Gets groups where the user is an active member.
     * 
     * @param user The user whose groups to retrieve
     * @param pageable Pagination information
     * @return Page of groups where user is a member
     */
    Page<Group> getUserGroups(User user, Pageable pageable);
    
    /**
     * Adds a user to a group with appropriate membership status.
     * For public groups, user joins immediately. For private groups, creates pending request.
     * 
     * @param groupId The ID of the group to join
     * @param user The user requesting to join
     * @return Created GroupMembership entity
     * @throws IllegalArgumentException if group not found, user already member, or group at capacity
     * @throws SecurityException if user is banned from the group
     */
    GroupMembership joinGroup(Long groupId, User user);
    
    /**
     * Removes a user from a group.
     * Users can leave groups they're members of. Admins can remove other members.
     * 
     * @param groupId The ID of the group to leave
     * @param targetUser The user to remove from the group
     * @param currentUser The authenticated user making the request
     * @throws IllegalArgumentException if group not found or user not a member
     * @throws SecurityException if user lacks permission to remove target user
     */
    void leaveGroup(Long groupId, User targetUser, User currentUser);
    
    /**
     * Approves a pending membership request (private groups only).
     * Only group owners and admins can approve membership requests.
     * 
     * @param membershipId The ID of the pending membership to approve
     * @param currentUser The authenticated user making the request
     * @return Updated GroupMembership entity with ACTIVE status
     * @throws IllegalArgumentException if membership not found or not pending
     * @throws SecurityException if user lacks permission to approve memberships
     */
    GroupMembership approveMembership(Long membershipId, User currentUser);
    
    /**
     * Rejects a pending membership request.
     * Only group owners and admins can reject membership requests.
     * 
     * @param membershipId The ID of the pending membership to reject
     * @param currentUser The authenticated user making the request
     * @throws IllegalArgumentException if membership not found or not pending
     * @throws SecurityException if user lacks permission to reject memberships
     */
    void rejectMembership(Long membershipId, User currentUser);
    
    /**
     * Promotes a member to admin role.
     * Only group owners can promote members to admin.
     * 
     * @param membershipId The ID of the membership to promote
     * @param currentUser The authenticated user making the request
     * @return Updated GroupMembership entity with ADMIN role
     * @throws IllegalArgumentException if membership not found or user not eligible
     * @throws SecurityException if user lacks permission to promote members
     */
    GroupMembership promoteMemberToAdmin(Long membershipId, User currentUser);
    
    /**
     * Demotes an admin to member role.
     * Only group owners can demote admins.
     * 
     * @param membershipId The ID of the membership to demote
     * @param currentUser The authenticated user making the request
     * @return Updated GroupMembership entity with MEMBER role
     * @throws IllegalArgumentException if membership not found or user not admin
     * @throws SecurityException if user lacks permission to demote members
     */
    GroupMembership demoteAdminToMember(Long membershipId, User currentUser);
    
    /**
     * Gets all members of a group with pagination.
     * 
     * @param groupId The ID of the group
     * @param status Optional status filter (null for all statuses)
     * @param pageable Pagination information
     * @param currentUser The authenticated user making the request
     * @return Page of GroupMembership entities
     * @throws IllegalArgumentException if group not found
     * @throws SecurityException if user lacks permission to view members
     */
    Page<GroupMembership> getGroupMembers(Long groupId, GroupMembership.MembershipStatus status, 
                                         Pageable pageable, User currentUser);
    
    /**
     * Gets pending membership requests for a group.
     * Only group owners and admins can view pending requests.
     * 
     * @param groupId The ID of the group
     * @param pageable Pagination information
     * @param currentUser The authenticated user making the request
     * @return Page of pending GroupMembership entities
     * @throws IllegalArgumentException if group not found
     * @throws SecurityException if user lacks permission to view pending requests
     */
    Page<GroupMembership> getPendingMembershipRequests(Long groupId, Pageable pageable, User currentUser);
    
    /**
     * Soft deletes a group (sets isActive = false).
     * Only group owners can delete groups.
     * 
     * @param groupId The ID of the group to delete
     * @param currentUser The authenticated user making the request
     * @throws IllegalArgumentException if group not found
     * @throws SecurityException if user lacks permission to delete group
     */
    void deleteGroup(Long groupId, User currentUser);
    
    /**
     * Checks if a group name is available (case-insensitive).
     * 
     * @param name The group name to check
     * @return true if name is available, false if already taken
     */
    boolean isGroupNameAvailable(String name);
    
    /**
     * Gets groups that the user might be interested in joining.
     * Excludes groups the user is already a member of.
     * 
     * @param user The user to get recommendations for
     * @param pageable Pagination information
     * @return Page of recommended groups
     */
    Page<Group> getRecommendedGroups(User user, Pageable pageable);
} 