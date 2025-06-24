package com.showsync.repository;

import com.showsync.entity.Group;
import com.showsync.entity.GroupMembership;
import com.showsync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GroupMembership entity operations.
 * Manages the relationships between users and groups.
 */
@Repository
public interface GroupMembershipRepository extends JpaRepository<GroupMembership, Long> {

    /**
     * Find membership by user and group
     * @param user the user
     * @param group the group
     * @return optional membership if found
     */
    Optional<GroupMembership> findByUserAndGroup(User user, Group group);

    /**
     * Find all active memberships for a user
     * @param user the user
     * @param pageable pagination information
     * @return page of active memberships
     */
    Page<GroupMembership> findByUserAndStatusOrderByJoinedAtDesc(
            User user, GroupMembership.MembershipStatus status, Pageable pageable);

    /**
     * Find all memberships for a group with specific status
     * @param group the group
     * @param status the membership status
     * @param pageable pagination information
     * @return page of memberships
     */
    Page<GroupMembership> findByGroupAndStatusOrderByJoinedAtDesc(
            Group group, GroupMembership.MembershipStatus status, Pageable pageable);

    /**
     * Find all memberships for a group
     * @param group the group
     * @param pageable pagination information
     * @return page of all memberships
     */
    Page<GroupMembership> findByGroupOrderByJoinedAtAsc(Group group, Pageable pageable);

    /**
     * Check if a user is an active member of a group
     * @param user the user
     * @param group the group
     * @return true if user is an active member
     */
    boolean existsByUserAndGroupAndStatus(User user, Group group, GroupMembership.MembershipStatus status);

    /**
     * Count active members in a group
     * @param group the group
     * @param status the membership status
     * @return count of members with the specified status
     */
    long countByGroupAndStatus(Group group, GroupMembership.MembershipStatus status);

    /**
     * Find group owners (there should be exactly one per group)
     * @param group the group
     * @return list of owner memberships
     */
    List<GroupMembership> findByGroupAndRole(Group group, GroupMembership.MembershipRole role);

    /**
     * Find all admin and owner memberships for a group
     * @param group the group
     * @return list of admin-level memberships
     */
    @Query("SELECT m FROM GroupMembership m WHERE m.group = :group " +
           "AND (m.role = 'OWNER' OR m.role = 'ADMIN') AND m.status = 'ACTIVE' " +
           "ORDER BY m.role ASC, m.joinedAt ASC")
    List<GroupMembership> findAdminMembershipsByGroup(@Param("group") Group group);

    /**
     * Find pending membership requests for a group
     * @param group the group
     * @param pageable pagination information
     * @return page of pending memberships
     */
    Page<GroupMembership> findByGroupAndStatusOrderByCreatedAtAsc(
            Group group, GroupMembership.MembershipStatus status, Pageable pageable);

    /**
     * Delete all memberships for a group (used when deleting a group)
     * @param group the group
     */
    void deleteByGroup(Group group);

    /**
     * Delete all memberships for a user (used when deleting a user)
     * @param user the user
     */
    void deleteByUser(User user);

    /**
     * Find users who can be promoted to admin in a group (active members only)
     * @param group the group
     * @return list of eligible memberships
     */
    @Query("SELECT m FROM GroupMembership m WHERE m.group = :group " +
           "AND m.role = 'MEMBER' AND m.status = 'ACTIVE' " +
           "ORDER BY m.joinedAt ASC")
    List<GroupMembership> findEligibleForPromotion(@Param("group") Group group);

    /**
     * Get membership statistics for a group
     * @param group the group
     * @return array with [totalMembers, activeMembers, pendingMembers, adminCount]
     */
    @Query("SELECT " +
           "COUNT(m), " +
           "SUM(CASE WHEN m.status = 'ACTIVE' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN m.status = 'PENDING' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN m.status = 'ACTIVE' AND (m.role = 'OWNER' OR m.role = 'ADMIN') THEN 1 ELSE 0 END) " +
           "FROM GroupMembership m WHERE m.group = :group")
    Object[] getMembershipStatistics(@Param("group") Group group);
} 