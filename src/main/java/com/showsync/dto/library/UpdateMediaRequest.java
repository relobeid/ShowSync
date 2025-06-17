package com.showsync.dto.library;

import com.showsync.entity.UserMediaInteraction.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating media properties in user's library.
 * Supports updating rating, status, progress, and review.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@Data
@Schema(description = "Request to update media properties in user's library")
public class UpdateMediaRequest {
    
    @Min(value = 1, message = "Rating must be between 1 and 10")
    @Max(value = 10, message = "Rating must be between 1 and 10")
    @Schema(description = "User rating for the media (1-10)", 
            minimum = "1", maximum = "10", example = "8")
    private Integer rating;
    
    @Schema(description = "Viewing/reading status", 
            example = "WATCHING")
    private Status status;
    
    @Min(value = 0, message = "Progress cannot be negative")
    @Schema(description = "Progress (episodes watched, pages read, etc.)", 
            minimum = "0", example = "5")
    private Integer progress;
    
    @Size(max = 2000, message = "Review cannot exceed 2000 characters")
    @Schema(description = "User review/comment", 
            maxLength = 2000, 
            example = "Great movie with excellent cinematography!")
    private String review;
    
    @Schema(description = "Whether this media is marked as favorite", 
            example = "true")
    private Boolean favorite;
} 