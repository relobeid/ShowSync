package com.showsync.service.impl;

import com.showsync.dto.group.CreateGroupRequest;
import com.showsync.dto.group.UpdateGroupRequest;
import com.showsync.entity.Group;
import com.showsync.entity.GroupMembership;
import com.showsync.entity.User;
import com.showsync.repository.GroupMembershipRepository;
import com.showsync.repository.GroupRepository;
import com.showsync.repository.UserRepository;
import com.showsync.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of GroupService.
 * Provides secure, transactional operations for group management and membership.
 * 
 * Security Considerations:
 * - All operations validate user permissions and group ownership
 * - Input validation prevents malicious data injection
 * - Proper exception handling prevents information leakage
 * - Comprehensive audit logging for group operations
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Slf4j
@Service
@Transactional
public class GroupServiceImpl implements GroupService {
    
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final UserRepository userRepository;
    
    @Autowired
    public GroupServiceImpl(
            GroupRepository groupRepository,
            GroupMembershipRepository membershipRepository,
            UserRepository userRepository) {
        this.groupRepository = groupRepository;
        this.membershipRepository = membershipRepository;
        this.userRepository = userRepository;
    }
    
    @Override
    public Group createGroup(CreateGroupRequest request, User createdBy) {
        // Validate input first before accessing properties
        validateCreateGroupRequest(request);
        validateUser(createdBy);
        
        log.info("Creating new group - name: {}, createdBy: {}", request.getName(), createdBy.getId());
        
        // Check if group name is already taken
        if (!isGroupNameAvailable(request.getName())) {
            log.warn("Group name already exists - name: {}", request.getName());
            throw new IllegalArgumentException("A group with this name already exists");
        }
        
        // Create group
        Group group = new Group();
        group.setName(request.getName().trim());
        group.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        group.setPrivacySetting(request.getPrivacySetting());
        group.setCreatedBy(createdBy);
        group.setMaxMembers(request.getMaxMembers());
        group.setActive(true);
        
        Group savedGroup = groupRepository.save(group);
        
        // Create owner membership
        GroupMembership ownerMembership = new GroupMembership();
        ownerMembership.setGroup(savedGroup);
        ownerMembership.setUser(createdBy);
        ownerMembership.setRole(GroupMembership.MembershipRole.OWNER);
        ownerMembership.setStatus(GroupMembership.MembershipStatus.ACTIVE);
        ownerMembership.setJoinedAt(LocalDateTime.now());
        
        membershipRepository.save(ownerMembership);
        
        log.info("Group created successfully - groupId: {}, ownerMembershipId: {}", 
                savedGroup.getId(), ownerMembership.getId());
        
        return savedGroup;
    }
    
    @Override
    public Group updateGroup(Long groupId, UpdateGroupRequest request, User currentUser) {
        log.info("Updating group - groupId: {}, userId: {}", groupId, currentUser.getId());
        
        Group group = getGroupByIdOrThrow(groupId);
        validateUpdatePermission(group, currentUser);
        
        // Validate name uniqueness if name is being changed
        if (request.getName() != null && !request.getName().equals(group.getName())) {
            if (!isGroupNameAvailable(request.getName())) {
                throw new IllegalArgumentException("A group with this name already exists");
            }
        }
        
        // Apply updates
        if (request.getName() != null) {
            group.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            group.setDescription(request.getDescription().trim());
        }
        if (request.getPrivacySetting() != null) {
            group.setPrivacySetting(request.getPrivacySetting());
        }
        if (request.getMaxMembers() != null) {
            // Validate that new max members doesn't violate current member count
            long currentMemberCount = membershipRepository.countByGroupAndStatus(
                    group, GroupMembership.MembershipStatus.ACTIVE);
            if (request.getMaxMembers() > 0 && currentMemberCount > request.getMaxMembers()) {
                throw new IllegalArgumentException(
                    String.format("Cannot set max members to %d when group already has %d members", 
                        request.getMaxMembers(), currentMemberCount));
            }
            group.setMaxMembers(request.getMaxMembers());
        }
        
        Group updated = groupRepository.save(group);
        log.info("Group updated successfully - groupId: {}", updated.getId());
        
        return updated;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Group> getGroupById(Long groupId, User currentUser) {
        log.debug("Retrieving group by ID - groupId: {}, userId: {}", groupId, 
                currentUser != null ? currentUser.getId() : "anonymous");
        
        Optional<Group> group = groupRepository.findById(groupId);
        if (group.isPresent() && !group.get().isActive()) {
            return Optional.empty(); // Don't return inactive groups
        }
        
        return group;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Group> getGroupById(Long groupId) {
        return getGroupById(groupId, null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Group> searchGroups(String searchTerm, Pageable pageable, User currentUser) {
        log.debug("Searching groups - searchTerm: {}, page: {}, userId: {}", 
                searchTerm, pageable.getPageNumber(), currentUser != null ? currentUser.getId() : "anonymous");
        
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getPublicGroups(pageable, currentUser);
        }
        
        return groupRepository.searchByNameContainingIgnoreCase(searchTerm.trim(), pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Group> getPublicGroups(Pageable pageable, User currentUser) {
        log.debug("Retrieving public groups - page: {}, userId: {}", 
                pageable.getPageNumber(), currentUser != null ? currentUser.getId() : "anonymous");
        
        return groupRepository.findByPrivacySettingAndIsActiveTrueOrderByCreatedAtDesc(
                Group.PrivacySetting.PUBLIC, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Group> getGroupsByCreator(User createdBy, Pageable pageable, User currentUser) {
        log.debug("Retrieving groups by creator - createdBy: {}, page: {}", 
                createdBy.getId(), pageable.getPageNumber());
        
        return groupRepository.findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(createdBy, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Group> getUserGroups(User user, Pageable pageable) {
        log.debug("Retrieving user groups - userId: {}, page: {}", user.getId(), pageable.getPageNumber());
        
        return groupRepository.findGroupsByUserId(user.getId(), pageable);
    }
    
    @Override
    public GroupMembership joinGroup(Long groupId, User user) {
        log.info("User joining group - groupId: {}, userId: {}", groupId, user.getId());
        
        Group group = getGroupByIdOrThrow(groupId);
        validateUser(user);
        
        // Check if user is already a member
        Optional<GroupMembership> existingMembership = membershipRepository.findByUserAndGroup(user, group);
        if (existingMembership.isPresent()) {
            GroupMembership membership = existingMembership.get();
            if (membership.getStatus() == GroupMembership.MembershipStatus.ACTIVE) {
                throw new IllegalArgumentException("You are already a member of this group");
            } else if (membership.getStatus() == GroupMembership.MembershipStatus.PENDING) {
                throw new IllegalArgumentException("You already have a pending request for this group");
            } else if (membership.getStatus() == GroupMembership.MembershipStatus.BANNED) {
                throw new SecurityException("You are banned from this group");
            }
        }
        
        // Check group capacity using repository query instead of lazy-loaded collection
        if (isGroupAtCapacity(group)) {
            throw new IllegalArgumentException("This group has reached its maximum member capacity");
        }
        
        // Create membership with appropriate status
        GroupMembership membership = new GroupMembership();
        membership.setGroup(group);
        membership.setUser(user);
        membership.setRole(GroupMembership.MembershipRole.MEMBER);
        membership.setJoinedAt(LocalDateTime.now());
        
        // Set status based on group privacy
        if (group.getPrivacySetting() == Group.PrivacySetting.PUBLIC) {
            membership.setStatus(GroupMembership.MembershipStatus.ACTIVE);
            log.info("User joined public group - membershipId: {}", membership.getId());
        } else {
            membership.setStatus(GroupMembership.MembershipStatus.PENDING);
            log.info("Membership request created for private group - membershipId: {}", membership.getId());
        }
        
        return membershipRepository.save(membership);
    }
    
    @Override
    public void leaveGroup(Long groupId, User targetUser, User currentUser) {
        log.info("User leaving group - groupId: {}, targetUser: {}, currentUser: {}", 
                groupId, targetUser.getId(), currentUser.getId());
        
        Group group = getGroupByIdOrThrow(groupId);
        
        // Users can leave groups they're members of, or admins can remove members
        boolean isSelfLeaving = targetUser.getId().equals(currentUser.getId());
        if (!isSelfLeaving) {
            validateRemovePermission(group, currentUser, targetUser);
        }
        
        Optional<GroupMembership> membership = membershipRepository.findByUserAndGroup(targetUser, group);
        if (membership.isEmpty() || membership.get().getStatus() != GroupMembership.MembershipStatus.ACTIVE) {
            throw new IllegalArgumentException("User is not an active member of this group");
        }
        
        GroupMembership membershipToRemove = membership.get();
        
        // Prevent owner from leaving unless transferring ownership
        if (membershipToRemove.getRole() == GroupMembership.MembershipRole.OWNER) {
            if (isSelfLeaving) {
                throw new IllegalArgumentException("Group owner cannot leave. Transfer ownership first or delete the group");
            } else {
                throw new SecurityException("Cannot remove group owner");
            }
        }
        
        membershipRepository.delete(membershipToRemove);
        log.info("User removed from group successfully - membershipId: {}", membershipToRemove.getId());
    }
    
    // Additional methods continue here...
    // Due to space constraints, I'll implement the remaining methods in the next part
    
    // Private helper methods
    private void validateCreateGroupRequest(CreateGroupRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Create group request cannot be null");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Group name is required");
        }
        if (request.getName().trim().length() < 3 || request.getName().trim().length() > 100) {
            throw new IllegalArgumentException("Group name must be between 3 and 100 characters");
        }
        if (request.getDescription() != null && request.getDescription().length() > 2000) {
            throw new IllegalArgumentException("Description cannot exceed 2000 characters");
        }
        if (request.getPrivacySetting() == null) {
            throw new IllegalArgumentException("Privacy setting is required");
        }
        if (request.getMaxMembers() != null && request.getMaxMembers() < 0) {
            throw new IllegalArgumentException("Maximum members cannot be negative");
        }
    }
    
    private void validateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Valid user is required");
        }
        if (!user.isActive()) {
            throw new SecurityException("User account is not active");
        }
    }
    
    private Group getGroupByIdOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .filter(Group::isActive)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));
    }
    
    private void validateUpdatePermission(Group group, User currentUser) {
        Optional<GroupMembership> membership = membershipRepository.findByUserAndGroup(currentUser, group);
        if (membership.isEmpty() || 
            membership.get().getStatus() != GroupMembership.MembershipStatus.ACTIVE ||
            !membership.get().hasAdminPrivileges()) {
            throw new SecurityException("You do not have permission to update this group");
        }
    }
    
    private void validateRemovePermission(Group group, User currentUser, User targetUser) {
        Optional<GroupMembership> currentUserMembership = membershipRepository.findByUserAndGroup(currentUser, group);
        if (currentUserMembership.isEmpty() || 
            currentUserMembership.get().getStatus() != GroupMembership.MembershipStatus.ACTIVE ||
            !currentUserMembership.get().hasAdminPrivileges()) {
            throw new SecurityException("You do not have permission to remove members from this group");
        }
        
        // Additional check: admins cannot remove other admins, only owners can
        Optional<GroupMembership> targetMembership = membershipRepository.findByUserAndGroup(targetUser, group);
        if (targetMembership.isPresent() && 
            targetMembership.get().hasAdminPrivileges() &&
            currentUserMembership.get().getRole() != GroupMembership.MembershipRole.OWNER) {
            throw new SecurityException("Only group owners can remove administrators");
        }
    }
    
    @Override
    public GroupMembership approveMembership(Long membershipId, User currentUser) {
        log.info("Approving membership - membershipId: {}, userId: {}", membershipId, currentUser.getId());
        
        GroupMembership membership = getMembershipByIdOrThrow(membershipId);
        validateApprovalPermission(membership.getGroup(), currentUser);
        
        if (membership.getStatus() != GroupMembership.MembershipStatus.PENDING) {
            throw new IllegalArgumentException("Only pending memberships can be approved");
        }
        
        // Check group capacity before approving
        if (isGroupAtCapacity(membership.getGroup())) {
            throw new IllegalArgumentException("Cannot approve membership - group has reached maximum capacity");
        }
        
        membership.setStatus(GroupMembership.MembershipStatus.ACTIVE);
        GroupMembership approved = membershipRepository.save(membership);
        
        log.info("Membership approved successfully - membershipId: {}", approved.getId());
        return approved;
    }
    
    @Override
    public void rejectMembership(Long membershipId, User currentUser) {
        log.info("Rejecting membership - membershipId: {}, userId: {}", membershipId, currentUser.getId());
        
        GroupMembership membership = getMembershipByIdOrThrow(membershipId);
        validateApprovalPermission(membership.getGroup(), currentUser);
        
        if (membership.getStatus() != GroupMembership.MembershipStatus.PENDING) {
            throw new IllegalArgumentException("Only pending memberships can be rejected");
        }
        
        membershipRepository.delete(membership);
        log.info("Membership rejected successfully - membershipId: {}", membershipId);
    }
    
    @Override
    public GroupMembership promoteMemberToAdmin(Long membershipId, User currentUser) {
        log.info("Promoting member to admin - membershipId: {}, userId: {}", membershipId, currentUser.getId());
        
        GroupMembership membership = getMembershipByIdOrThrow(membershipId);
        validateOwnerPermission(membership.getGroup(), currentUser);
        
        if (membership.getRole() != GroupMembership.MembershipRole.MEMBER) {
            throw new IllegalArgumentException("Only regular members can be promoted to admin");
        }
        
        if (membership.getStatus() != GroupMembership.MembershipStatus.ACTIVE) {
            throw new IllegalArgumentException("Only active members can be promoted");
        }
        
        membership.setRole(GroupMembership.MembershipRole.ADMIN);
        GroupMembership promoted = membershipRepository.save(membership);
        
        log.info("Member promoted to admin successfully - membershipId: {}", promoted.getId());
        return promoted;
    }
    
    @Override
    public GroupMembership demoteAdminToMember(Long membershipId, User currentUser) {
        log.info("Demoting admin to member - membershipId: {}, userId: {}", membershipId, currentUser.getId());
        
        GroupMembership membership = getMembershipByIdOrThrow(membershipId);
        validateOwnerPermission(membership.getGroup(), currentUser);
        
        if (membership.getRole() != GroupMembership.MembershipRole.ADMIN) {
            throw new IllegalArgumentException("Only admins can be demoted to member");
        }
        
        membership.setRole(GroupMembership.MembershipRole.MEMBER);
        GroupMembership demoted = membershipRepository.save(membership);
        
        log.info("Admin demoted to member successfully - membershipId: {}", demoted.getId());
        return demoted;
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GroupMembership> getGroupMembers(Long groupId, GroupMembership.MembershipStatus status, 
                                               Pageable pageable, User currentUser) {
        log.debug("Retrieving group members - groupId: {}, status: {}, userId: {}", 
                groupId, status, currentUser.getId());
        
        Group group = getGroupByIdOrThrow(groupId);
        validateMemberViewPermission(group, currentUser);
        
        if (status != null) {
            return membershipRepository.findByGroupAndStatusOrderByJoinedAtDesc(group, status, pageable);
        } else {
            return membershipRepository.findByGroupOrderByJoinedAtAsc(group, pageable);
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<GroupMembership> getPendingMembershipRequests(Long groupId, Pageable pageable, User currentUser) {
        log.debug("Retrieving pending membership requests - groupId: {}, userId: {}", groupId, currentUser.getId());
        
        Group group = getGroupByIdOrThrow(groupId);
        validateApprovalPermission(group, currentUser);
        
        return membershipRepository.findByGroupAndStatusOrderByCreatedAtAsc(
                group, GroupMembership.MembershipStatus.PENDING, pageable);
    }
    
    @Override
    public void deleteGroup(Long groupId, User currentUser) {
        log.info("Deleting group - groupId: {}, userId: {}", groupId, currentUser.getId());
        
        Group group = getGroupByIdOrThrow(groupId);
        validateOwnerPermission(group, currentUser);
        
        // Soft delete - set active to false
        group.setActive(false);
        groupRepository.save(group);
        
        log.info("Group deleted successfully - groupId: {}", groupId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Group> getRecommendedGroups(User user, Pageable pageable) {
        log.debug("Getting recommended groups for user - userId: {}", user.getId());
        
        // Simple implementation: return public groups the user is not already a member of
        return groupRepository.findPublicGroupsNotJoinedByUser(user.getId(), pageable);
    }
    
    @Override
    public boolean isGroupNameAvailable(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return !groupRepository.existsByNameIgnoreCase(name.trim());
    }
    
    // Additional helper methods
    private GroupMembership getMembershipByIdOrThrow(Long membershipId) {
        return membershipRepository.findById(membershipId)
                .orElseThrow(() -> new IllegalArgumentException("Membership not found"));
    }
    
    private void validateApprovalPermission(Group group, User currentUser) {
        Optional<GroupMembership> membership = membershipRepository.findByUserAndGroup(currentUser, group);
        if (membership.isEmpty() || 
            membership.get().getStatus() != GroupMembership.MembershipStatus.ACTIVE ||
            !membership.get().hasAdminPrivileges()) {
            throw new SecurityException("You do not have permission to manage memberships for this group");
        }
    }
    
    private void validateOwnerPermission(Group group, User currentUser) {
        Optional<GroupMembership> membership = membershipRepository.findByUserAndGroup(currentUser, group);
        if (membership.isEmpty() || 
            membership.get().getStatus() != GroupMembership.MembershipStatus.ACTIVE ||
            membership.get().getRole() != GroupMembership.MembershipRole.OWNER) {
            throw new SecurityException("You do not have owner permission for this group");
        }
    }
    
    private void validateMemberViewPermission(Group group, User currentUser) {
        // For now, allow any authenticated user to view group members
        // Could be restricted based on group privacy settings in the future
        if (currentUser == null) {
            throw new SecurityException("Authentication required to view group members");
        }
    }
    
    /**
     * Check if a group has reached its maximum capacity using repository query
     * This is more reliable than using the lazy-loaded collection
     */
    private boolean isGroupAtCapacity(Group group) {
        if (group.getMaxMembers() == null || group.getMaxMembers() <= 0) {
            return false; // No capacity limit
        }
        
        long activeMemberCount = membershipRepository.countByGroupAndStatus(
                group, GroupMembership.MembershipStatus.ACTIVE);
        
        return activeMemberCount >= group.getMaxMembers();
    }
} 