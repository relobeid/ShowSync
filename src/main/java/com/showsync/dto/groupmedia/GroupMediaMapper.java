package com.showsync.dto.groupmedia;

import com.showsync.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between group media entities and DTOs.
 * Handles safe conversion with null checks and prevents exposure of sensitive data.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Component
public class GroupMediaMapper {

    /**
     * Converts a GroupMediaList entity to GroupMediaListResponse DTO.
     * 
     * @param groupMediaList The entity to convert
     * @return GroupMediaListResponse DTO
     */
    public GroupMediaListResponse toResponse(GroupMediaList groupMediaList) {
        if (groupMediaList == null) {
            return null;
        }

        GroupMediaListResponse response = new GroupMediaListResponse();
        response.setId(groupMediaList.getId());
        response.setGroupId(groupMediaList.getGroup().getId());
        response.setListType(groupMediaList.getListType());
        response.setGroupRating(groupMediaList.getGroupRating());
        response.setTotalVotes(groupMediaList.getTotalVotes());
        response.setCreatedAt(groupMediaList.getCreatedAt());
        response.setUpdatedAt(groupMediaList.getUpdatedAt());

        // Convert media information
        if (groupMediaList.getMedia() != null) {
            response.setMedia(toMediaInfo(groupMediaList.getMedia()));
        }

        // Convert added by information
        if (groupMediaList.getAddedBy() != null) {
            response.setAddedBy(toAddedByInfo(groupMediaList.getAddedBy()));
        }

        return response;
    }

    /**
     * Converts a GroupMediaVote entity to GroupMediaVoteResponse DTO.
     * 
     * @param vote The entity to convert
     * @return GroupMediaVoteResponse DTO
     */
    public GroupMediaVoteResponse toResponse(GroupMediaVote vote) {
        if (vote == null) {
            return null;
        }

        GroupMediaVoteResponse response = new GroupMediaVoteResponse();
        response.setId(vote.getId());
        response.setGroupId(vote.getGroup().getId());
        response.setMediaId(vote.getMedia().getId());
        response.setVoteType(vote.getVoteType());
        response.setSuggestedAt(vote.getSuggestedAt());
        response.setCreatedAt(vote.getCreatedAt());

        // Convert voter information
        if (vote.getUser() != null) {
            response.setVoter(toVoterInfo(vote.getUser()));
        }

        return response;
    }

    /**
     * Converts a GroupActivity entity to GroupActivityResponse DTO.
     * 
     * @param activity The entity to convert
     * @return GroupActivityResponse DTO
     */
    public GroupActivityResponse toResponse(GroupActivity activity) {
        if (activity == null) {
            return null;
        }

        GroupActivityResponse response = new GroupActivityResponse();
        response.setId(activity.getId());
        response.setGroupId(activity.getGroup().getId());
        response.setActivityType(activity.getActivityType());
        response.setActivityData(activity.getActivityData());
        response.setCreatedAt(activity.getCreatedAt());

        // Convert user information
        if (activity.getUser() != null) {
            response.setUser(toActivityUserInfo(activity.getUser()));
        }

        // Convert target media information
        if (activity.getTargetMedia() != null) {
            response.setTargetMedia(toActivityMediaInfo(activity.getTargetMedia()));
        }

        // Convert target user information
        if (activity.getTargetUser() != null) {
            response.setTargetUser(toActivityUserInfo(activity.getTargetUser()));
        }

        // Generate human-readable description
        response.setDescription(generateActivityDescription(activity));

        return response;
    }

    /**
     * Converts a list of GroupMediaList entities to GroupMediaListResponse DTOs.
     * 
     * @param groupMediaLists List of entities to convert
     * @return List of GroupMediaListResponse DTOs
     */
    public List<GroupMediaListResponse> toResponseList(List<GroupMediaList> groupMediaLists) {
        if (groupMediaLists == null) {
            return List.of();
        }

        return groupMediaLists.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of GroupMediaVote entities to GroupMediaVoteResponse DTOs.
     * 
     * @param votes List of entities to convert
     * @return List of GroupMediaVoteResponse DTOs
     */
    public List<GroupMediaVoteResponse> toVoteResponseList(List<GroupMediaVote> votes) {
        if (votes == null) {
            return List.of();
        }

        return votes.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Converts a list of GroupActivity entities to GroupActivityResponse DTOs.
     * 
     * @param activities List of entities to convert
     * @return List of GroupActivityResponse DTOs
     */
    public List<GroupActivityResponse> toActivityResponseList(List<GroupActivity> activities) {
        if (activities == null) {
            return List.of();
        }

        return activities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // Private helper methods

    private GroupMediaListResponse.MediaInfo toMediaInfo(Media media) {
        if (media == null) {
            return null;
        }

        GroupMediaListResponse.MediaInfo mediaInfo = new GroupMediaListResponse.MediaInfo();
        mediaInfo.setId(media.getId());
        mediaInfo.setType(media.getType() != null ? media.getType().name() : null);
        mediaInfo.setTitle(media.getTitle());
        mediaInfo.setOriginalTitle(media.getOriginalTitle());
        mediaInfo.setDescription(media.getDescription());
        mediaInfo.setReleaseDate(media.getReleaseDate());
        mediaInfo.setPosterUrl(media.getPosterUrl());
        mediaInfo.setBackdropUrl(media.getBackdropUrl());
        mediaInfo.setExternalId(media.getExternalId());
        mediaInfo.setExternalSource(media.getExternalSource());
        mediaInfo.setAverageRating(media.getAverageRating());
        mediaInfo.setRatingCount(media.getRatingCount());

        return mediaInfo;
    }

    private GroupMediaListResponse.AddedByInfo toAddedByInfo(User user) {
        if (user == null) {
            return null;
        }

        GroupMediaListResponse.AddedByInfo addedByInfo = new GroupMediaListResponse.AddedByInfo();
        addedByInfo.setId(user.getId());
        addedByInfo.setUsername(user.getUsername());
        addedByInfo.setDisplayName(user.getDisplayName());
        addedByInfo.setProfilePictureUrl(user.getProfilePictureUrl());

        return addedByInfo;
    }

    private GroupMediaVoteResponse.VoterInfo toVoterInfo(User user) {
        if (user == null) {
            return null;
        }

        GroupMediaVoteResponse.VoterInfo voterInfo = new GroupMediaVoteResponse.VoterInfo();
        voterInfo.setId(user.getId());
        voterInfo.setUsername(user.getUsername());
        voterInfo.setDisplayName(user.getDisplayName());
        voterInfo.setProfilePictureUrl(user.getProfilePictureUrl());

        return voterInfo;
    }

    private GroupActivityResponse.UserInfo toActivityUserInfo(User user) {
        if (user == null) {
            return null;
        }

        GroupActivityResponse.UserInfo userInfo = new GroupActivityResponse.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setDisplayName(user.getDisplayName());
        userInfo.setProfilePictureUrl(user.getProfilePictureUrl());

        return userInfo;
    }

    private GroupActivityResponse.MediaInfo toActivityMediaInfo(Media media) {
        if (media == null) {
            return null;
        }

        GroupActivityResponse.MediaInfo mediaInfo = new GroupActivityResponse.MediaInfo();
        mediaInfo.setId(media.getId());
        mediaInfo.setType(media.getType() != null ? media.getType().name() : null);
        mediaInfo.setTitle(media.getTitle());
        mediaInfo.setPosterUrl(media.getPosterUrl());
        mediaInfo.setExternalId(media.getExternalId());
        mediaInfo.setExternalSource(media.getExternalSource());

        return mediaInfo;
    }

    /**
     * Generates a human-readable description for a group activity.
     * 
     * @param activity The activity to describe
     * @return Human-readable description
     */
    private String generateActivityDescription(GroupActivity activity) {
        if (activity == null || activity.getUser() == null) {
            return "Unknown activity";
        }

        String userName = activity.getUser().getDisplayName() != null 
            ? activity.getUser().getDisplayName() 
            : activity.getUser().getUsername();

        return switch (activity.getActivityType()) {
            case MEDIA_ADDED_TO_LIST -> {
                String mediaTitle = activity.getTargetMedia() != null 
                    ? activity.getTargetMedia().getTitle() 
                    : "a media item";
                yield userName + " added " + mediaTitle + " to the group list";
            }
            case MEDIA_COMPLETED -> {
                String mediaTitle = activity.getTargetMedia() != null 
                    ? activity.getTargetMedia().getTitle() 
                    : "a media item";
                yield userName + " marked " + mediaTitle + " as completed";
            }
            case MEDIA_RATING_UPDATED -> {
                String mediaTitle = activity.getTargetMedia() != null 
                    ? activity.getTargetMedia().getTitle() 
                    : "a media item";
                yield userName + " rated " + mediaTitle;
            }
            case MEDIA_VOTE_CAST -> {
                String mediaTitle = activity.getTargetMedia() != null 
                    ? activity.getTargetMedia().getTitle() 
                    : "a media item";
                yield userName + " voted on " + mediaTitle;
            }
            case MEMBER_JOINED -> userName + " joined the group";
            case MEMBER_LEFT -> userName + " left the group";
            case MEMBER_PROMOTED -> {
                String targetUserName = activity.getTargetUser() != null 
                    ? (activity.getTargetUser().getDisplayName() != null 
                        ? activity.getTargetUser().getDisplayName() 
                        : activity.getTargetUser().getUsername())
                    : "a member";
                yield userName + " promoted " + targetUserName;
            }
            case MEMBER_DEMOTED -> {
                String targetUserName = activity.getTargetUser() != null 
                    ? (activity.getTargetUser().getDisplayName() != null 
                        ? activity.getTargetUser().getDisplayName() 
                        : activity.getTargetUser().getUsername())
                    : "a member";
                yield userName + " demoted " + targetUserName;
            }
            case GROUP_CREATED -> userName + " created the group";
            case GROUP_UPDATED -> userName + " updated group settings";
            default -> userName + " performed an activity";
        };
    }
} 