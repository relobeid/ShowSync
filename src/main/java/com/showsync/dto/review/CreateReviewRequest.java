package com.showsync.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new review.
 * Includes validation for all required fields.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Data
@Schema(description = "Request to create a new review")
public class CreateReviewRequest {
    
    @Schema(description = "Media ID to review", example = "123", required = true)
    private Long mediaId;
    
    @Size(max = 255, message = "Review title cannot exceed 255 characters")
    @Schema(description = "Optional review title", 
            maxLength = 255, 
            example = "Great movie with excellent performances")
    private String title;
    
    @NotBlank(message = "Review content cannot be blank")
    @Size(min = 10, max = 5000, message = "Review content must be between 10 and 5000 characters")
    @Schema(description = "Review content", 
            minLength = 10, 
            maxLength = 5000,
            example = "This movie exceeded my expectations. The cinematography was stunning and the story was compelling throughout.",
            required = true)
    private String content;
    
    @Min(value = 1, message = "Rating must be between 1 and 10")
    @Max(value = 10, message = "Rating must be between 1 and 10")
    @Schema(description = "Rating (1-10)", 
            minimum = "1", 
            maximum = "10", 
            example = "8")
    private Integer rating;
    
    @Schema(description = "Whether this review contains spoilers", 
            example = "false")
    private boolean spoiler = false;
} 