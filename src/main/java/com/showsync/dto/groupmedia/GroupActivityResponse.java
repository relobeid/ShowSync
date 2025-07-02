package com.showsync.dto.groupmedia;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.showsync.entity.GroupActivity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for group activities in the activity feed.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Schema(description = "Group activity information")
public class GroupActivityResponse {

    @Schema(description = "Activity ID", example = "1")
    private Long id;

    @Schema(description = "Group ID", example = "1")
    private Long groupId;

    @Schema(description = "User who performed the activity")
    private UserInfo user;

    @Schema(description = "Type of activity", example = "MEDIA_ADDED_TO_LIST")
    private GroupActivity.ActivityType activityType;

    @Schema(description = "Media involved in the activity (if applicable)")
    private MediaInfo targetMedia;

    @Schema(description = "User involved in the activity (if applicable)")
    private UserInfo targetUser;

    @Schema(description = "Additional activity data")
    private String activityData;

    @Schema(description = "Activity description for display", 
            example = "John added Fight Club to Currently Watching")
    private String description;

    @Schema(description = "When the activity occurred")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Nested DTO for user information
     */
    @Data
    @Schema(description = "User information")
    public static class UserInfo {
        @Schema(description = "User ID", example = "1")
        private Long id;

        @Schema(description = "Username", example = "john_doe")
        private String username;

        @Schema(description = "Display name", example = "John Doe")
        private String displayName;

        @Schema(description = "Profile picture URL")
        private String profilePictureUrl;
    }

    /**
     * Nested DTO for media information
     */
    @Data
    @Schema(description = "Media information")
    public static class MediaInfo {
        @Schema(description = "Media ID", example = "1")
        private Long id;

        @Schema(description = "Media type", example = "MOVIE")
        private String type;

        @Schema(description = "Media title", example = "Fight Club")
        private String title;

        @Schema(description = "Poster image URL")
        private String posterUrl;

        @Schema(description = "External media ID", example = "550")
        private String externalId;

        @Schema(description = "External source", example = "tmdb")
        private String externalSource;
    }
} 