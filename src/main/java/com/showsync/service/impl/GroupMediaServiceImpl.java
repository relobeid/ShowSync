package com.showsync.service.impl;

import com.showsync.dto.groupmedia.GroupMediaListRequest;
import com.showsync.dto.groupmedia.GroupMediaStatsResponse;
import com.showsync.dto.groupmedia.GroupMediaVoteRequest;
import com.showsync.entity.*;
import com.showsync.repository.*;
import com.showsync.service.GroupMediaService;
import com.showsync.service.external.ExternalMediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of GroupMediaService for managing group media activities.
 * Handles group media lists, voting, activity tracking, and recommendations.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class GroupMediaServiceImpl implements GroupMediaService {

    private final GroupRepository groupRepository;
    private final GroupMembershipRepository membershipRepository;
    private final MediaRepository mediaRepository;
    private final UserMediaInteractionRepository userMediaInteractionRepository;
    private final GroupMediaListRepository groupMediaListRepository;
    private final GroupMediaVoteRepository groupMediaVoteRepository;
    private final GroupActivityRepository groupActivityRepository;
    private final ExternalMediaService externalMediaService;

    @Override
    public GroupMediaList addMediaToGroupList(Long groupId, GroupMediaListRequest request, User currentUser) {
        log.debug("Adding media {} from {} to group {} list as {}", 
                request.getExternalId(), request.getExternalSource(), groupId, request.getListType());

        // Validate group membership
        Group group = validateGroupMembership(groupId, currentUser);
        
        // Find or create media
        Media media = findOrCreateMedia(request.getExternalId(), request.getExternalSource());
        
        // Check if media already exists in group list
        if (groupMediaListRepository.existsByGroupAndMedia(group, media)) {
            throw new IllegalArgumentException("Media is already in the group's list");
        }
        
        // Create group media list entry
        GroupMediaList groupMediaList = new GroupMediaList();
        groupMediaList.setGroup(group);
        groupMediaList.setMedia(media);
        groupMediaList.setListType(request.getListType());
        groupMediaList.setAddedBy(currentUser);
        
        GroupMediaList saved = groupMediaListRepository.save(groupMediaList);
        
        // Record activity
        GroupActivity activity = GroupActivity.createMediaActivity(
                group, currentUser, GroupActivity.ActivityType.MEDIA_ADDED_TO_LIST, media);
        recordActivity(activity);
        
        log.info("User {} added media {} to group {} list as {}", 
                currentUser.getUsername(), media.getTitle(), group.getName(), request.getListType());
        
        return saved;
    }

    @Override
    public void removeMediaFromGroupList(Long groupId, Long mediaId, User currentUser) {
        log.debug("Removing media {} from group {} list by user {}", mediaId, groupId, currentUser.getUsername());

        Group group = validateGroupMembership(groupId, currentUser);
        Media media = findMediaById(mediaId);
        
        GroupMediaList groupMediaList = groupMediaListRepository.findByGroupAndMedia(group, media)
                .orElseThrow(() -> new IllegalArgumentException("Media not found in group list"));
        
        // Check permissions - users can only remove their own additions unless they're admin/owner
        GroupMembership membership = getMembership(group, currentUser);
        if (!groupMediaList.getAddedBy().getId().equals(currentUser.getId()) && 
            !isGroupAdminOrOwner(membership)) {
            throw new SecurityException("You can only remove media you added, unless you're a group admin");
        }
        
        groupMediaListRepository.delete(groupMediaList);
        
        // Record activity
        GroupActivity activity = GroupActivity.createMediaActivity(
                group, currentUser, GroupActivity.ActivityType.MEDIA_STATUS_CHANGED, media);
        recordActivity(activity);
        
        log.info("User {} removed media {} from group {} list", 
                currentUser.getUsername(), media.getTitle(), group.getName());
    }

    @Override
    public GroupMediaList updateMediaListType(Long groupId, Long mediaId, GroupMediaList.ListType newListType, User currentUser) {
        log.debug("Updating media {} in group {} to list type {}", mediaId, groupId, newListType);

        Group group = validateGroupMembership(groupId, currentUser);
        Media media = findMediaById(mediaId);
        
        GroupMediaList groupMediaList = groupMediaListRepository.findByGroupAndMedia(group, media)
                .orElseThrow(() -> new IllegalArgumentException("Media not found in group list"));
        
        GroupMediaList.ListType oldListType = groupMediaList.getListType();
        groupMediaList.setListType(newListType);
        
        GroupMediaList saved = groupMediaListRepository.save(groupMediaList);
        
        // Record specific activity based on the change
        GroupActivity.ActivityType activityType = determineActivityType(oldListType, newListType);
        GroupActivity activity = GroupActivity.createMediaActivity(group, currentUser, activityType, media);
        recordActivity(activity);
        
        log.info("User {} updated media {} in group {} from {} to {}", 
                currentUser.getUsername(), media.getTitle(), group.getName(), oldListType, newListType);
        
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupMediaList> getGroupMediaList(Long groupId, GroupMediaList.ListType listType, Pageable pageable, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        
        if (listType != null) {
            return groupMediaListRepository.findByGroupAndListTypeOrderByCreatedAtDesc(group, listType, pageable);
        } else {
            return groupMediaListRepository.findByGroupOrderByCreatedAtDesc(group, pageable);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupMediaList> getGroupMediaListEntry(Long groupId, Long mediaId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        Media media = findMediaById(mediaId);
        
        return groupMediaListRepository.findByGroupAndMedia(group, media);
    }

    @Override
    public GroupMediaList updateGroupRating(Long groupId, Long mediaId) {
        Group group = findGroupById(groupId);
        Media media = findMediaById(mediaId);
        
        GroupMediaList groupMediaList = groupMediaListRepository.findByGroupAndMedia(group, media)
                .orElseThrow(() -> new IllegalArgumentException("Media not found in group list"));
        
        // Calculate aggregated rating from group members' individual ratings
        List<User> groupMembers = getGroupMemberUsers(group);
        List<UserMediaInteraction> interactions = groupMembers.stream()
                .map(user -> userMediaInteractionRepository.findByUserIdAndMediaId(user.getId(), media.getId()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        if (!interactions.isEmpty()) {
            List<UserMediaInteraction> ratedInteractions = interactions.stream()
                    .filter(interaction -> interaction.getRating() != null)
                    .collect(Collectors.toList());
            
            if (!ratedInteractions.isEmpty()) {
                double averageRating = ratedInteractions.stream()
                        .mapToDouble(UserMediaInteraction::getRating)
                        .average()
                        .orElse(0.0);
                
                groupMediaList.updateGroupRating(averageRating, ratedInteractions.size());
                return groupMediaListRepository.save(groupMediaList);
            }
        }
        
        return groupMediaList;
    }

    @Override
    public int recalculateGroupRatings(Long groupId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        
        // Only group admins/owners can recalculate ratings
        GroupMembership membership = getMembership(group, currentUser);
        if (!isGroupAdminOrOwner(membership)) {
            throw new SecurityException("Only group admins can recalculate ratings");
        }
        
        List<GroupMediaList> allGroupMedia = groupMediaListRepository.findByGroupOrderByCreatedAtDesc(group, Pageable.unpaged()).getContent();
        int recalculated = 0;
        
        for (GroupMediaList groupMediaList : allGroupMedia) {
            GroupMediaList updated = updateGroupRating(groupId, groupMediaList.getMedia().getId());
            if (updated.hasGroupRating()) {
                recalculated++;
            }
        }
        
        log.info("Recalculated {} group ratings for group {}", recalculated, group.getName());
        return recalculated;
    }

    @Override
    public GroupMediaVote castMediaVote(Long groupId, Long mediaId, GroupMediaVoteRequest request, User currentUser) {
        log.debug("User {} casting vote {} on media {} in group {}", 
                currentUser.getUsername(), request.getVoteType(), mediaId, groupId);

        Group group = validateGroupMembership(groupId, currentUser);
        Media media = findMediaById(mediaId);
        
        // Check for existing vote
        Optional<GroupMediaVote> existingVote = groupMediaVoteRepository
                .findByGroupAndMediaAndUser(group, media, currentUser);
        
        GroupMediaVote vote;
        if (existingVote.isPresent()) {
            // Update existing vote
            vote = existingVote.get();
            vote.setVoteType(request.getVoteType());
            vote.setSuggestedAt(LocalDateTime.now());
        } else {
            // Create new vote
            vote = new GroupMediaVote();
            vote.setGroup(group);
            vote.setMedia(media);
            vote.setUser(currentUser);
            vote.setVoteType(request.getVoteType());
        }
        
        GroupMediaVote saved = groupMediaVoteRepository.save(vote);
        
        // Record activity
        GroupActivity activity = GroupActivity.createMediaActivity(
                group, currentUser, GroupActivity.ActivityType.MEDIA_VOTE_CAST, media);
        recordActivity(activity);
        
        log.info("User {} cast vote {} on media {} in group {}", 
                currentUser.getUsername(), request.getVoteType(), media.getTitle(), group.getName());
        
        return saved;
    }

    @Override
    public void removeMediaVote(Long groupId, Long mediaId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        Media media = findMediaById(mediaId);
        
        GroupMediaVote vote = groupMediaVoteRepository.findByGroupAndMediaAndUser(group, media, currentUser)
                .orElseThrow(() -> new IllegalArgumentException("No existing vote found to remove"));
        
        groupMediaVoteRepository.delete(vote);
        
        log.info("User {} removed vote on media {} in group {}", 
                currentUser.getUsername(), media.getTitle(), group.getName());
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupMediaVote> getMediaVotes(Long groupId, Long mediaId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        Media media = findMediaById(mediaId);
        
        return groupMediaVoteRepository.findByGroupAndMedia(group, media);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> getVotingBasedRecommendations(Long groupId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        
        // Get top voted media that are not yet in the group's lists
        List<Object[]> topVotedResults = groupMediaVoteRepository.findTopVotedMedia(group);
        
        return topVotedResults.stream()
                .map(result -> findMediaById((Long) result[0]))
                .filter(media -> !groupMediaListRepository.existsByGroupAndMedia(group, media))
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupActivity> getGroupActivityFeed(Long groupId, Pageable pageable, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        return groupActivityRepository.findByGroupOrderByCreatedAtDesc(group, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<GroupActivity> getMediaActivities(Long groupId, Long mediaId, Pageable pageable, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        Media media = findMediaById(mediaId);
        
        return groupActivityRepository.findByGroupAndTargetMediaOrderByCreatedAtDesc(group, media, pageable);
    }

    @Override
    public GroupActivity recordActivity(GroupActivity activity) {
        return groupActivityRepository.save(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public GroupMediaStatsResponse getGroupMediaStatistics(Long groupId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        
        // Get media list statistics
        Object[] mediaStatsRaw = groupMediaListRepository.getGroupMediaStatistics(group);
        Object[] mediaStats = (mediaStatsRaw != null && mediaStatsRaw.length > 0) ? (Object[]) mediaStatsRaw[0] : null;
        
        // Get voting statistics
        Object[] votingStatsRaw = groupMediaVoteRepository.getGroupVotingStatistics(group);
        Object[] votingStats = (votingStatsRaw != null && votingStatsRaw.length > 0) ? (Object[]) votingStatsRaw[0] : null;
        
        // Get activity statistics
        Object[] activityStatsRaw = groupActivityRepository.getGroupActivityStatistics(group);
        Object[] activityStats = (activityStatsRaw != null && activityStatsRaw.length > 0) ? (Object[]) activityStatsRaw[0] : null;
        
        GroupMediaStatsResponse response = new GroupMediaStatsResponse();
        
        // Media statistics
        if (mediaStats != null && mediaStats.length >= 6) {
            response.setTotalMedia(((Number) mediaStats[0]).longValue());
            response.setCurrentlyWatching(((Number) mediaStats[1]).longValue());
            response.setCompleted(((Number) mediaStats[2]).longValue());
            response.setPlanToWatch(((Number) mediaStats[3]).longValue());
            response.setDropped(((Number) mediaStats[4]).longValue());
            response.setOnHold(((Number) mediaStats[5]).longValue());
        } else {
            response.setTotalMedia(0L);
            response.setCurrentlyWatching(0L);
            response.setCompleted(0L);
            response.setPlanToWatch(0L);
            response.setDropped(0L);
            response.setOnHold(0L);
        }
        
        // Voting statistics
        if (votingStats != null && votingStats.length >= 3) {
            response.setTotalVotes(((Number) votingStats[0]).longValue());
            response.setUniqueMediaVotedOn(((Number) votingStats[1]).longValue());
            response.setActiveVoters(((Number) votingStats[2]).longValue());
        } else {
            response.setTotalVotes(0L);
            response.setUniqueMediaVotedOn(0L);
            response.setActiveVoters(0L);
        }
        
        // Activity statistics
        if (activityStats != null && activityStats.length >= 4) {
            response.setTotalActivities(((Number) activityStats[0]).longValue());
            response.setUniqueActiveUsers(((Number) activityStats[1]).longValue());
            response.setMediaActivities(((Number) activityStats[2]).longValue());
            response.setMemberActivities(((Number) activityStats[3]).longValue());
        } else {
            response.setTotalActivities(0L);
            response.setUniqueActiveUsers(0L);
            response.setMediaActivities(0L);
            response.setMemberActivities(0L);
        }
        
        // Calculate average group rating
        Page<GroupMediaList> ratedMedia = groupMediaListRepository.findTopRatedByGroup(group, PageRequest.of(0, 1000));
        if (!ratedMedia.isEmpty()) {
            double averageRating = ratedMedia.getContent().stream()
                    .filter(GroupMediaList::hasGroupRating)
                    .mapToDouble(GroupMediaList::getGroupRating)
                    .average()
                    .orElse(0.0);
            response.setAverageGroupRating(averageRating);
        }
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> getGroupMediaRecommendations(Long groupId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        
        // Get trending media based on recent activity
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        Page<GroupActivity> recentActivities = groupActivityRepository
                .findRecentActivities(group, oneWeekAgo, PageRequest.of(0, 20));
        
        return recentActivities.getContent().stream()
                .filter(activity -> activity.getTargetMedia() != null)
                .map(GroupActivity::getTargetMedia)
                .distinct()
                .limit(10)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Media> getGroupTrendingMedia(Long groupId, User currentUser) {
        Group group = validateGroupMembership(groupId, currentUser);
        
        // Get recently added media (trending within the group)
        Page<GroupMediaList> recentlyAdded = groupMediaListRepository
                .findByGroupOrderByCreatedAtDesc(group, PageRequest.of(0, 10));
        
        return recentlyAdded.getContent().stream()
                .map(GroupMediaList::getMedia)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private Group validateGroupMembership(Long groupId, User currentUser) {
        Group group = findGroupById(groupId);
        
        GroupMembership membership = membershipRepository
                .findByUserAndGroup(currentUser, group)
                .orElseThrow(() -> new SecurityException("You must be an active member of this group"));
        
        if (membership.getStatus() != GroupMembership.MembershipStatus.ACTIVE) {
            throw new SecurityException("Your group membership is not active");
        }
        
        return group;
    }

    private Group findGroupById(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found with ID: " + groupId));
    }

    private Media findMediaById(Long mediaId) {
        return mediaRepository.findById(mediaId)
                .orElseThrow(() -> new IllegalArgumentException("Media not found with ID: " + mediaId));
    }

    private GroupMembership getMembership(Group group, User user) {
        return membershipRepository.findByUserAndGroup(user, group)
                .orElseThrow(() -> new SecurityException("User is not a member of this group"));
    }

    private boolean isGroupAdminOrOwner(GroupMembership membership) {
        return membership.getRole() == GroupMembership.MembershipRole.OWNER ||
               membership.getRole() == GroupMembership.MembershipRole.ADMIN;
    }

    private List<User> getGroupMemberUsers(Group group) {
        return membershipRepository.findByGroupAndStatusOrderByJoinedAtDesc(
                group, GroupMembership.MembershipStatus.ACTIVE, Pageable.unpaged())
                .getContent().stream()
                .map(GroupMembership::getUser)
                .collect(Collectors.toList());
    }

    private Media findOrCreateMedia(String externalId, String externalSource) {
        // First try to find existing media
        Optional<Media> existingMedia = mediaRepository.findByExternalIdAndExternalSource(externalId, externalSource);
        if (existingMedia.isPresent()) {
            return existingMedia.get();
        }
        
        // Create new media from external API (placeholder for actual implementation)
        Media media = new Media();
        media.setExternalId(externalId);
        media.setExternalSource(externalSource);
        media.setTitle("Media Title"); // This would come from external API
        media.setType(Media.MediaType.MOVIE); // This would be determined from external API
        
        return mediaRepository.save(media);
    }

    private GroupActivity.ActivityType determineActivityType(GroupMediaList.ListType oldType, GroupMediaList.ListType newType) {
        if (newType == GroupMediaList.ListType.COMPLETED) {
            return GroupActivity.ActivityType.MEDIA_COMPLETED;
        }
        return GroupActivity.ActivityType.MEDIA_STATUS_CHANGED;
    }
} 