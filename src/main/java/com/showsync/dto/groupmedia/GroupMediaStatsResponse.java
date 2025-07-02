package com.showsync.dto.groupmedia;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * Response DTO for group media statistics and aggregations.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Schema(description = "Group media statistics")
public class GroupMediaStatsResponse {

    @Schema(description = "Total media in all lists", example = "25")
    private Long totalMedia;

    @Schema(description = "Media currently being watched", example = "3")
    private Long currentlyWatching;

    @Schema(description = "Completed media", example = "15")
    private Long completed;

    @Schema(description = "Media planned to watch", example = "5")
    private Long planToWatch;

    @Schema(description = "Dropped media", example = "1")
    private Long dropped;

    @Schema(description = "Media on hold", example = "1")
    private Long onHold;

    @Schema(description = "Total votes cast", example = "47")
    private Long totalVotes;

    @Schema(description = "Unique media voted on", example = "12")
    private Long uniqueMediaVotedOn;

    @Schema(description = "Active voters", example = "8")
    private Long activeVoters;

    @Schema(description = "Total activities", example = "156")
    private Long totalActivities;

    @Schema(description = "Unique active users", example = "10")
    private Long uniqueActiveUsers;

    @Schema(description = "Media-related activities", example = "89")
    private Long mediaActivities;

    @Schema(description = "Member-related activities", example = "23")
    private Long memberActivities;

    @Schema(description = "Average group rating", example = "7.8")
    private Double averageGroupRating;

    @Schema(description = "Most popular media (by votes)")
    private List<PopularMediaInfo> mostPopularMedia;

    @Schema(description = "Top rated media")
    private List<TopRatedMediaInfo> topRatedMedia;

    /**
     * Nested DTO for popular media information
     */
    @Data
    @Schema(description = "Popular media information")
    public static class PopularMediaInfo {
        @Schema(description = "Media ID", example = "1")
        private Long mediaId;

        @Schema(description = "Media title", example = "Fight Club")
        private String title;

        @Schema(description = "Media type", example = "MOVIE")
        private String type;

        @Schema(description = "Total votes received", example = "8")
        private Integer totalVotes;

        @Schema(description = "Vote score", example = "15")
        private Long voteScore;

        @Schema(description = "Poster image URL")
        private String posterUrl;
    }

    /**
     * Nested DTO for top rated media information
     */
    @Data
    @Schema(description = "Top rated media information")
    public static class TopRatedMediaInfo {
        @Schema(description = "Media ID", example = "1")
        private Long mediaId;

        @Schema(description = "Media title", example = "Fight Club")
        private String title;

        @Schema(description = "Media type", example = "MOVIE")
        private String type;

        @Schema(description = "Group rating", example = "9.2")
        private Double groupRating;

        @Schema(description = "Number of ratings", example = "5")
        private Integer ratingCount;

        @Schema(description = "Poster image URL")
        private String posterUrl;
    }
} 