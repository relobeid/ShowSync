package com.showsync.dto.group;

import com.showsync.entity.Group;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for updating group information
 */
@Data
public class UpdateGroupRequest {
    
    @Size(min = 3, max = 100, message = "Group name must be between 3 and 100 characters")
    private String name;

    @Size(max = 2000, message = "Description cannot exceed 2000 characters")
    private String description;

    private Group.PrivacySetting privacySetting;

    @PositiveOrZero(message = "Maximum members must be zero or positive")
    private Integer maxMembers;
} 