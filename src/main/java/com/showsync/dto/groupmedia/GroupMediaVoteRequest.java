package com.showsync.dto.groupmedia;

import com.showsync.entity.GroupMediaVote;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for voting on media within groups.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Schema(description = "Request to vote on media within a group")
public class GroupMediaVoteRequest {

    @NotNull(message = "Vote type is required")
    @Schema(description = "Type of vote to cast", 
            example = "WATCH_NEXT", required = true)
    private GroupMediaVote.VoteType voteType;

    @Schema(description = "Optional comment about the vote", 
            example = "This looks really interesting!")
    private String comment;
} 