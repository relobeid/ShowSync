package com.showsync.repository;

import com.showsync.entity.Group;
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
 * Repository interface for Group entity operations.
 * Provides methods for querying groups with various filters and conditions.
 */
@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    /**
     * Find all active groups with pagination
     * @param pageable pagination information
     * @return page of active groups
     */
    Page<Group> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    /**
     * Find groups by privacy setting and active status
     * @param privacySetting the privacy setting to filter by
     * @param pageable pagination information
     * @return page of matching groups
     */
    Page<Group> findByPrivacySettingAndIsActiveTrueOrderByCreatedAtDesc(
            Group.PrivacySetting privacySetting, Pageable pageable);

    /**
     * Find groups created by a specific user
     * @param createdBy the user who created the groups
     * @param pageable pagination information
     * @return page of groups created by the user
     */
    Page<Group> findByCreatedByAndIsActiveTrueOrderByCreatedAtDesc(User createdBy, Pageable pageable);

    /**
     * Search groups by name containing search term (case-insensitive)
     * @param searchTerm the term to search for in group names
     * @param pageable pagination information
     * @return page of matching groups
     */
    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) " +
           "AND g.isActive = true ORDER BY g.createdAt DESC")
    Page<Group> searchByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find groups where a user is an active member
     * @param userId the user ID to search for
     * @param pageable pagination information
     * @return page of groups where the user is a member
     */
    @Query("SELECT g FROM Group g JOIN g.memberships m WHERE m.user.id = :userId " +
           "AND m.status = 'ACTIVE' AND g.isActive = true ORDER BY m.joinedAt DESC")
    Page<Group> findGroupsByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find public groups that a user is not a member of (for recommendations)
     * @param userId the user ID to exclude memberships for
     * @param pageable pagination information
     * @return page of groups the user could potentially join
     */
    @Query("SELECT g FROM Group g WHERE g.privacySetting = 'PUBLIC' AND g.isActive = true " +
           "AND g.id NOT IN (SELECT m.group.id FROM GroupMembership m WHERE m.user.id = :userId) " +
           "ORDER BY g.createdAt DESC")
    Page<Group> findPublicGroupsNotJoinedByUser(@Param("userId") Long userId, Pageable pageable);

    /**
     * Check if a group name already exists (case-insensitive)
     * @param name the group name to check
     * @return true if a group with this name exists
     */
    @Query("SELECT COUNT(g) > 0 FROM Group g WHERE LOWER(g.name) = LOWER(:name) AND g.isActive = true")
    boolean existsByNameIgnoreCase(@Param("name") String name);

    /**
     * Get groups with member count for analytics
     * @param pageable pagination information
     * @return page of groups with member counts
     */
    @Query("SELECT g, COUNT(m) as memberCount FROM Group g LEFT JOIN g.memberships m " +
           "WHERE g.isActive = true AND (m.status = 'ACTIVE' OR m.status IS NULL) " +
           "GROUP BY g ORDER BY memberCount DESC, g.createdAt DESC")
    Page<Object[]> findGroupsWithMemberCount(Pageable pageable);

    /**
     * Find groups that are near their capacity limit
     * @param capacityThreshold the threshold percentage (e.g., 0.8 for 80%)
     * @return list of groups near capacity
     */
    @Query("SELECT g FROM Group g WHERE g.isActive = true AND g.maxMembers IS NOT NULL " +
           "AND (SELECT COUNT(m) FROM GroupMembership m WHERE m.group = g AND m.status = 'ACTIVE') " +
           ">= (g.maxMembers * :capacityThreshold)")
    List<Group> findGroupsNearCapacity(@Param("capacityThreshold") double capacityThreshold);
} 