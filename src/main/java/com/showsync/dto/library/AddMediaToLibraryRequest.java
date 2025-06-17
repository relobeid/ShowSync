package com.showsync.dto.library;

import com.showsync.entity.UserMediaInteraction.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for adding media to user's library.
 * Validates external media ID and source, with optional initial status.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@Data
@Schema(description = "Request to add media to user's library")
public class AddMediaToLibraryRequest {
    
    @NotBlank(message = "External media ID is required")
    @Schema(description = "External media ID (TMDb ID or Open Library ID)", 
            example = "550", required = true)
    private String externalId;
    
    @NotBlank(message = "External source is required")
    @Schema(description = "Source of the media", 
            allowableValues = {"tmdb", "openlibrary"}, 
            example = "tmdb", required = true)
    private String externalSource;
    
    @Schema(description = "Initial status for the media", 
            example = "PLAN_TO_WATCH",
            defaultValue = "PLAN_TO_WATCH")
    private Status initialStatus = Status.PLAN_TO_WATCH;
} 