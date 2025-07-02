package com.showsync.dto.groupmedia;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.showsync.entity.GroupMediaList;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for group media list entries.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Schema(description = "Group media list entry")
public class GroupMediaListResponse {

    @Schema(description = "Unique list entry ID", example = "1")
    private Long id;

    @Schema(description = "Group ID", example = "1")
    private Long groupId;

    @Schema(description = "Media information")
    private MediaInfo media;

    @Schema(description = "List type", example = "CURRENTLY_WATCHING")
    private GroupMediaList.ListType listType;

    @Schema(description = "User who added this media")
    private AddedByInfo addedBy;

    @Schema(description = "Aggregated group rating (1-10)", example = "8.2")
    private Double groupRating;

    @Schema(description = "Total number of ratings", example = "5")
    private Integer totalVotes;

    @Schema(description = "When added to the list")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Schema(description = "Last updated timestamp")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

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

        @Schema(description = "Original title", example = "Fight Club")
        private String originalTitle;

        @Schema(description = "Media description")
        private String description;

        @Schema(description = "Release date")
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime releaseDate;

        @Schema(description = "Poster image URL")
        private String posterUrl;

        @Schema(description = "Backdrop image URL")
        private String backdropUrl;

        @Schema(description = "External media ID", example = "550")
        private String externalId;

        @Schema(description = "External source", example = "tmdb")
        private String externalSource;

        @Schema(description = "Average rating", example = "8.8")
        private Double averageRating;

        @Schema(description = "Rating count", example = "1547")
        private Integer ratingCount;
    }

    /**
     * Nested DTO for user who added the media
     */
    @Data
    @Schema(description = "User who added the media")
    public static class AddedByInfo {
        @Schema(description = "User ID", example = "1")
        private Long id;

        @Schema(description = "Username", example = "moviebuff")
        private String username;

        @Schema(description = "Display name", example = "Movie Buff")
        private String displayName;

        @Schema(description = "Profile picture URL")
        private String profilePictureUrl;
    }
} 