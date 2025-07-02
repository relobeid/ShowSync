package com.showsync.dto.groupmedia;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.showsync.entity.GroupMediaVote;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for group media votes.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Schema(description = "Group media vote information")
public class GroupMediaVoteResponse {

    @Schema(description = "Vote ID", example = "1")
    private Long id;

    @Schema(description = "Group ID", example = "1")
    private Long groupId;

    @Schema(description = "Media ID", example = "1")
    private Long mediaId;

    @Schema(description = "User who cast the vote")
    private VoterInfo voter;

    @Schema(description = "Type of vote", example = "WATCH_NEXT")
    private GroupMediaVote.VoteType voteType;

    @Schema(description = "When the vote was suggested")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime suggestedAt;

    @Schema(description = "When the vote was cast")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    /**
     * Nested DTO for voter information
     */
    @Data
    @Schema(description = "User who cast the vote")
    public static class VoterInfo {
        @Schema(description = "User ID", example = "1")
        private Long id;

        @Schema(description = "Username", example = "moviefan")
        private String username;

        @Schema(description = "Display name", example = "Movie Fan")
        private String displayName;

        @Schema(description = "Profile picture URL")
        private String profilePictureUrl;
    }
} 