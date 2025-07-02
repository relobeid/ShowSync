package com.showsync.service;

import com.showsync.dto.groupmedia.GroupMediaListRequest;
import com.showsync.dto.groupmedia.GroupMediaStatsResponse;
import com.showsync.dto.groupmedia.GroupMediaVoteRequest;
import com.showsync.entity.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing group media activities.
 * Handles group media lists, voting, activity tracking, and recommendations.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
public interface GroupMediaService {

    // Group Media List Operations
    
    /**
     * Add media to a group's list
     * @param groupId the group ID
     * @param request the media list request
     * @param currentUser the current user
     * @return the created group media list entry
     */
    GroupMediaList addMediaToGroupList(Long groupId, GroupMediaListRequest request, User currentUser);
    
    /**
     * Remove media from a group's list
     * @param groupId the group ID
     * @param mediaId the media ID
     * @param currentUser the current user
     */
    void removeMediaFromGroupList(Long groupId, Long mediaId, User currentUser);
    
    /**
     * Update media list type (e.g., move from watching to completed)
     * @param groupId the group ID
     * @param mediaId the media ID
     * @param newListType the new list type
     * @param currentUser the current user
     * @return the updated group media list entry
     */
    GroupMediaList updateMediaListType(Long groupId, Long mediaId, GroupMediaList.ListType newListType, User currentUser);
    
    /**
     * Get group's media list with optional filtering
     * @param groupId the group ID
     * @param listType optional list type filter
     * @param pageable pagination information
     * @param currentUser the current user
     * @return page of group media list entries
     */
    Page<GroupMediaList> getGroupMediaList(Long groupId, GroupMediaList.ListType listType, Pageable pageable, User currentUser);
    
    /**
     * Get specific media entry from group list
     * @param groupId the group ID
     * @param mediaId the media ID
     * @param currentUser the current user
     * @return optional group media list entry
     */
    Optional<GroupMediaList> getGroupMediaListEntry(Long groupId, Long mediaId, User currentUser);

    // Group Rating Aggregation
    
    /**
     * Update aggregated group rating for a media item
     * @param groupId the group ID
     * @param mediaId the media ID
     * @return the updated group media list entry
     */
    GroupMediaList updateGroupRating(Long groupId, Long mediaId);
    
    /**
     * Recalculate all group ratings for a group
     * @param groupId the group ID
     * @param currentUser the current user
     * @return number of ratings recalculated
     */
    int recalculateGroupRatings(Long groupId, User currentUser);

    // Group Media Voting
    
    /**
     * Cast a vote on media within a group
     * @param groupId the group ID
     * @param mediaId the media ID
     * @param request the vote request
     * @param currentUser the current user
     * @return the created or updated vote
     */
    GroupMediaVote castMediaVote(Long groupId, Long mediaId, GroupMediaVoteRequest request, User currentUser);
    
    /**
     * Remove user's vote on media
     * @param groupId the group ID
     * @param mediaId the media ID
     * @param currentUser the current user
     */
    void removeMediaVote(Long groupId, Long mediaId, User currentUser);
    
    /**
     * Get all votes for a media item in a group
     * @param groupId the group ID
     * @param mediaId the media ID
     * @param currentUser the current user
     * @return list of votes
     */
    List<GroupMediaVote> getMediaVotes(Long groupId, Long mediaId, User currentUser);
    
    /**
     * Get media recommendations based on voting patterns
     * @param groupId the group ID
     * @param currentUser the current user
     * @return list of recommended media
     */
    List<Media> getVotingBasedRecommendations(Long groupId, User currentUser);

    // Group Activity Feed
    
    /**
     * Get group's activity feed
     * @param groupId the group ID
     * @param pageable pagination information
     * @param currentUser the current user
     * @return page of group activities
     */
    Page<GroupActivity> getGroupActivityFeed(Long groupId, Pageable pageable, User currentUser);
    
    /**
     * Get activities related to specific media
     * @param groupId the group ID
     * @param mediaId the media ID
     * @param pageable pagination information
     * @param currentUser the current user
     * @return page of media-related activities
     */
    Page<GroupActivity> getMediaActivities(Long groupId, Long mediaId, Pageable pageable, User currentUser);
    
    /**
     * Record a group activity
     * @param activity the activity to record
     * @return the saved activity
     */
    GroupActivity recordActivity(GroupActivity activity);

    // Group Media Statistics
    
    /**
     * Get comprehensive group media statistics
     * @param groupId the group ID
     * @param currentUser the current user
     * @return group media statistics
     */
    GroupMediaStatsResponse getGroupMediaStatistics(Long groupId, User currentUser);

    // Group Media Recommendations
    
    /**
     * Get personalized media recommendations for the group
     * @param groupId the group ID
     * @param currentUser the current user
     * @return list of recommended media
     */
    List<Media> getGroupMediaRecommendations(Long groupId, User currentUser);
    
    /**
     * Get trending media within the group
     * @param groupId the group ID
     * @param currentUser the current user
     * @return list of trending media
     */
    List<Media> getGroupTrendingMedia(Long groupId, User currentUser);
} 