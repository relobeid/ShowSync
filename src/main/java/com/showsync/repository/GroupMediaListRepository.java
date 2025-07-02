package com.showsync.repository;

import com.showsync.entity.Group;
import com.showsync.entity.GroupMediaList;
import com.showsync.entity.Media;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for GroupMediaList entity operations.
 * Manages group-level media collections and lists.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Repository
public interface GroupMediaListRepository extends JpaRepository<GroupMediaList, Long> {

    /**
     * Find media list entry by group and media
     * @param group the group
     * @param media the media
     * @return optional group media list entry
     */
    Optional<GroupMediaList> findByGroupAndMedia(Group group, Media media);

    /**
     * Find all media in a group's list with optional type filtering
     * @param group the group
     * @param listType the list type (optional)
     * @param pageable pagination information
     * @return page of group media list entries
     */
    Page<GroupMediaList> findByGroupAndListTypeOrderByCreatedAtDesc(
            Group group, GroupMediaList.ListType listType, Pageable pageable);

    /**
     * Find all media in a group's lists
     * @param group the group
     * @param pageable pagination information
     * @return page of group media list entries
     */
    Page<GroupMediaList> findByGroupOrderByCreatedAtDesc(Group group, Pageable pageable);

    /**
     * Find currently watching media for a group
     * @param group the group
     * @return list of currently watching media
     */
    List<GroupMediaList> findByGroupAndListType(Group group, GroupMediaList.ListType listType);

    /**
     * Check if media exists in any group list
     * @param group the group
     * @param media the media
     * @return true if media exists in group lists
     */
    boolean existsByGroupAndMedia(Group group, Media media);

    /**
     * Count media in a specific list type for a group
     * @param group the group
     * @param listType the list type
     * @return count of media in the list
     */
    long countByGroupAndListType(Group group, GroupMediaList.ListType listType);

    /**
     * Find top rated media in a group
     * @param group the group
     * @param pageable pagination information
     * @return page of media ordered by group rating descending
     */
    @Query("SELECT gml FROM GroupMediaList gml WHERE gml.group = :group " +
           "AND gml.groupRating IS NOT NULL ORDER BY gml.groupRating DESC")
    Page<GroupMediaList> findTopRatedByGroup(@Param("group") Group group, Pageable pageable);

    /**
     * Find media added by a specific user in a group
     * @param group the group
     * @param userId the user ID
     * @param pageable pagination information
     * @return page of media added by the user
     */
    @Query("SELECT gml FROM GroupMediaList gml WHERE gml.group = :group " +
           "AND gml.addedBy.id = :userId ORDER BY gml.createdAt DESC")
    Page<GroupMediaList> findByGroupAndAddedByUserId(@Param("group") Group group, 
                                                    @Param("userId") Long userId, 
                                                    Pageable pageable);

    /**
     * Find media with the most votes in a group
     * @param group the group
     * @param pageable pagination information
     * @return page of media ordered by total votes descending
     */
    @Query("SELECT gml FROM GroupMediaList gml WHERE gml.group = :group " +
           "AND gml.totalVotes > 0 ORDER BY gml.totalVotes DESC")
    Page<GroupMediaList> findMostVotedByGroup(@Param("group") Group group, Pageable pageable);

    /**
     * Get statistics for a group's media lists
     * @param group the group
     * @return array with [total_media, currently_watching, completed, plan_to_watch, dropped, on_hold]
     */
    @Query("SELECT " +
           "COUNT(gml), " +
           "SUM(CASE WHEN gml.listType = 'CURRENTLY_WATCHING' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN gml.listType = 'COMPLETED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN gml.listType = 'PLAN_TO_WATCH' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN gml.listType = 'DROPPED' THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN gml.listType = 'ON_HOLD' THEN 1 ELSE 0 END) " +
           "FROM GroupMediaList gml WHERE gml.group = :group")
    Object[] getGroupMediaStatistics(@Param("group") Group group);

    /**
     * Find groups that have a specific media in their lists
     * @param media the media
     * @param listType optional list type filter
     * @param pageable pagination information
     * @return page of groups that have this media
     */
    @Query("SELECT gml.group FROM GroupMediaList gml WHERE gml.media = :media " +
           "AND (:listType IS NULL OR gml.listType = :listType) " +
           "ORDER BY gml.createdAt DESC")
    Page<Group> findGroupsByMedia(@Param("media") Media media, 
                                 @Param("listType") GroupMediaList.ListType listType, 
                                 Pageable pageable);

    /**
     * Delete all entries for a group (used when deleting a group)
     * @param group the group
     */
    void deleteByGroup(Group group);

    /**
     * Delete all entries for a media item
     * @param media the media
     */
    void deleteByMedia(Media media);
} 