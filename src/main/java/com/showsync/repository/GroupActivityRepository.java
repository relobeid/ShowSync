package com.showsync.repository;

import com.showsync.entity.Group;
import com.showsync.entity.GroupActivity;
import com.showsync.entity.Media;
import com.showsync.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository interface for GroupActivity entity operations.
 * Manages group activity feed and activity tracking.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Repository
public interface GroupActivityRepository extends JpaRepository<GroupActivity, Long> {

    /**
     * Find activities for a group with pagination (activity feed)
     * @param group the group
     * @param pageable pagination information
     * @return page of activities ordered by creation date descending
     */
    Page<GroupActivity> findByGroupOrderByCreatedAtDesc(Group group, Pageable pageable);

    /**
     * Find activities by a specific user in a group
     * @param group the group
     * @param user the user
     * @param pageable pagination information
     * @return page of user's activities in the group
     */
    Page<GroupActivity> findByGroupAndUserOrderByCreatedAtDesc(Group group, User user, Pageable pageable);

    /**
     * Find activities by type in a group
     * @param group the group
     * @param activityType the activity type
     * @param pageable pagination information
     * @return page of activities of the specified type
     */
    Page<GroupActivity> findByGroupAndActivityTypeOrderByCreatedAtDesc(
            Group group, GroupActivity.ActivityType activityType, Pageable pageable);

    /**
     * Find activities related to specific media in a group
     * @param group the group
     * @param media the media
     * @param pageable pagination information
     * @return page of media-related activities
     */
    Page<GroupActivity> findByGroupAndTargetMediaOrderByCreatedAtDesc(Group group, Media media, Pageable pageable);

    /**
     * Find recent activities in a group
     * @param group the group
     * @param since only activities after this date
     * @param pageable pagination information
     * @return page of recent activities
     */
    @Query("SELECT a FROM GroupActivity a WHERE a.group = :group " +
           "AND a.createdAt >= :since ORDER BY a.createdAt DESC")
    Page<GroupActivity> findRecentActivities(@Param("group") Group group, 
                                           @Param("since") LocalDateTime since, 
                                           Pageable pageable);

    /**
     * Find media-related activities in a group
     * @param group the group
     * @param pageable pagination information
     * @return page of media activities
     */
    @Query("SELECT a FROM GroupActivity a WHERE a.group = :group " +
           "AND a.targetMedia IS NOT NULL ORDER BY a.createdAt DESC")
    Page<GroupActivity> findMediaActivities(@Param("group") Group group, Pageable pageable);

    /**
     * Find member-related activities in a group
     * @param group the group
     * @param pageable pagination information
     * @return page of member activities
     */
    @Query("SELECT a FROM GroupActivity a WHERE a.group = :group " +
           "AND (a.activityType LIKE 'MEMBER_%' OR a.targetUser IS NOT NULL) " +
           "ORDER BY a.createdAt DESC")
    Page<GroupActivity> findMemberActivities(@Param("group") Group group, Pageable pageable);

    /**
     * Get activity count by type for a group
     * @param group the group
     * @param activityType the activity type
     * @return count of activities
     */
    long countByGroupAndActivityType(Group group, GroupActivity.ActivityType activityType);

    /**
     * Get activity statistics for a group
     * @param group the group
     * @return array with [total_activities, unique_users, media_activities, member_activities]
     */
    @Query("SELECT " +
           "COUNT(a), " +
           "COUNT(DISTINCT a.user), " +
           "SUM(CASE WHEN a.targetMedia IS NOT NULL THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN a.activityType LIKE 'MEMBER_%' THEN 1 ELSE 0 END) " +
           "FROM GroupActivity a WHERE a.group = :group")
    Object[] getGroupActivityStatistics(@Param("group") Group group);

    /**
     * Find most active users in a group
     * @param group the group
     * @param since only activities after this date
     * @return list of user IDs with activity counts
     */
    @Query("SELECT a.user.id, COUNT(a) as activityCount " +
           "FROM GroupActivity a WHERE a.group = :group " +
           "AND a.createdAt >= :since " +
           "GROUP BY a.user.id ORDER BY activityCount DESC")
    List<Object[]> findMostActiveUsers(@Param("group") Group group, @Param("since") LocalDateTime since);

    /**
     * Find groups where a user has been active
     * @param user the user
     * @param since only activities after this date
     * @return list of groups ordered by last activity
     */
    @Query("SELECT DISTINCT a.group FROM GroupActivity a WHERE a.user = :user " +
           "AND a.createdAt >= :since ORDER BY MAX(a.createdAt) DESC")
    List<Group> findActiveGroupsByUser(@Param("user") User user, @Param("since") LocalDateTime since);

    /**
     * Get daily activity counts for a group
     * @param group the group
     * @param since only activities after this date
     * @return list with [date, activity_count] for each day
     */
    @Query("SELECT DATE(a.createdAt), COUNT(a) " +
           "FROM GroupActivity a WHERE a.group = :group " +
           "AND a.createdAt >= :since " +
           "GROUP BY DATE(a.createdAt) ORDER BY DATE(a.createdAt) DESC")
    List<Object[]> getDailyActivityCounts(@Param("group") Group group, @Param("since") LocalDateTime since);

    /**
     * Delete all activities for a group (used when deleting a group)
     * @param group the group
     */
    void deleteByGroup(Group group);

    /**
     * Delete all activities by a user
     * @param user the user
     */
    void deleteByUser(User user);

    /**
     * Delete all activities related to a media item
     * @param media the media
     */
    void deleteByTargetMedia(Media media);
} 