package com.showsync.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for review information.
 * Provides complete review data for API responses.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Data
@Schema(description = "Review information")
public class ReviewResponse {
    
    @Schema(description = "Review ID", example = "456")
    private Long id;
    
    @Schema(description = "Review author information")
    private UserInfo user;
    
    @Schema(description = "Media information")
    private MediaInfo media;
    
    @Schema(description = "Review title", example = "Great movie with excellent performances")
    private String title;
    
    @Schema(description = "Review content", 
            example = "This movie exceeded my expectations. The cinematography was stunning and the story was compelling throughout.")
    private String content;
    
    @Schema(description = "User's rating (1-10)", example = "8")
    private Integer rating;
    
    @Schema(description = "Number of helpful votes", example = "15")
    private Integer helpfulVotes;
    
    @Schema(description = "Total number of votes", example = "18")
    private Integer totalVotes;
    
    @Schema(description = "Helpfulness ratio (0.0-1.0)", example = "0.83")
    private double helpfulnessRatio;
    
    @Schema(description = "Whether this review contains spoilers", example = "false")
    private boolean spoiler;
    
    @Schema(description = "Whether this review has been moderated", example = "false")
    private boolean moderated;
    
    @Schema(description = "When the review was created", example = "2024-12-21T10:30:00")
    private LocalDateTime createdAt;
    
    @Schema(description = "When the review was last updated", example = "2024-12-21T15:45:00")
    private LocalDateTime updatedAt;
    
    @Schema(description = "Current user's vote on this review (if any)")
    private UserVote userVote;
    
    /**
     * Nested DTO for user information.
     */
    @Data
    @Schema(description = "Review author information")
    public static class UserInfo {
        
        @Schema(description = "User ID", example = "123")
        private Long id;
        
        @Schema(description = "Username", example = "moviebuff2024")
        private String username;
        
        @Schema(description = "Display name", example = "Movie Enthusiast")
        private String displayName;
        
        @Schema(description = "Profile picture URL")
        private String profilePictureUrl;
    }
    
    /**
     * Nested DTO for basic media information.
     */
    @Data
    @Schema(description = "Media information")
    public static class MediaInfo {
        
        @Schema(description = "Media ID", example = "789")
        private Long id;
        
        @Schema(description = "Media type", example = "MOVIE")
        private String type;
        
        @Schema(description = "Title", example = "The Matrix")
        private String title;
        
        @Schema(description = "Poster URL")
        private String posterUrl;
        
        @Schema(description = "Release year", example = "1999")
        private Integer releaseYear;
    }
    
    /**
     * Nested DTO for user's vote on this review.
     */
    @Data
    @Schema(description = "User's vote on this review")
    public static class UserVote {
        
        @Schema(description = "Whether the vote is helpful", example = "true")
        private boolean helpful;
        
        @Schema(description = "When the vote was cast", example = "2024-12-21T14:20:00")
        private LocalDateTime votedAt;
    }
} 