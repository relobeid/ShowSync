package com.showsync.dto.library;

import com.showsync.entity.UserMediaInteraction.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Response DTO for media library items.
 * Provides clean API responses without exposing internal entity structure.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@Data
@Schema(description = "Media item in user's library")
public class MediaLibraryResponse {
    
    @Schema(description = "Unique interaction ID", example = "1")
    private Long interactionId;
    
    @Schema(description = "Media information")
    private MediaInfo media;
    
    @Schema(description = "User's rating (1-10)", example = "8")
    private Integer rating;
    
    @Schema(description = "Current status", example = "WATCHING")
    private Status status;
    
    @Schema(description = "Progress (episodes/pages)", example = "5")
    private Integer progress;
    
    @Schema(description = "User's review", example = "Great storyline!")
    private String review;
    
    @Schema(description = "Whether marked as favorite", example = "true")
    private boolean favorite;
    
    @Schema(description = "When added to library", example = "2024-12-15T10:30:00")
    private LocalDateTime addedAt;
    
    @Schema(description = "Last update time", example = "2024-12-15T15:45:00")
    private LocalDateTime updatedAt;
    
    /**
     * Nested DTO for media information to avoid exposing full Media entity.
     */
    @Data
    @Schema(description = "Basic media information")
    public static class MediaInfo {
        
        @Schema(description = "Media database ID", example = "123")
        private Long id;
        
        @Schema(description = "Media type", example = "MOVIE")
        private String type;
        
        @Schema(description = "Title", example = "Fight Club")
        private String title;
        
        @Schema(description = "Original title", example = "Fight Club")
        private String originalTitle;
        
        @Schema(description = "Description/plot", example = "An insomniac office worker...")
        private String description;
        
        @Schema(description = "Release date", example = "1999-10-15T00:00:00")
        private LocalDateTime releaseDate;
        
        @Schema(description = "Poster image URL", example = "https://image.tmdb.org/...")
        private String posterUrl;
        
        @Schema(description = "Backdrop image URL", example = "https://image.tmdb.org/...")
        private String backdropUrl;
        
        @Schema(description = "External media ID", example = "550")
        private String externalId;
        
        @Schema(description = "External source", example = "tmdb")
        private String externalSource;
        
        @Schema(description = "Average rating from all users", example = "8.5")
        private Double averageRating;
        
        @Schema(description = "Number of ratings", example = "1247")
        private Integer ratingCount;
    }
} 