package com.showsync.dto.groupmedia;

import com.showsync.entity.GroupMediaList;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request DTO for adding media to group lists.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@Data
@Schema(description = "Request to add media to a group list")
public class GroupMediaListRequest {

    @NotBlank(message = "External media ID is required")
    @Schema(description = "External media ID (TMDb ID or Open Library ID)", 
            example = "550", required = true)
    private String externalId;

    @NotBlank(message = "External source is required")
    @Schema(description = "External media source", 
            example = "tmdb", allowableValues = {"tmdb", "openlibrary"}, required = true)
    private String externalSource;

    @NotNull(message = "List type is required")
    @Schema(description = "Type of list to add media to", 
            example = "CURRENTLY_WATCHING", required = true)
    private GroupMediaList.ListType listType;

    @Schema(description = "Optional note about the addition", 
            example = "Recommended by Sarah")
    private String note;
} 