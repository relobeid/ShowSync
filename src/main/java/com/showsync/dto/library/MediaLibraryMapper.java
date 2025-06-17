package com.showsync.dto.library;

import com.showsync.entity.Media;
import com.showsync.entity.UserMediaInteraction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mapping between UserMediaInteraction entities and DTOs.
 * Handles safe conversion with null checks and prevents exposure of sensitive data.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@Component
public class MediaLibraryMapper {
    
    /**
     * Converts a UserMediaInteraction entity to MediaLibraryResponse DTO.
     * 
     * @param interaction The entity to convert
     * @return MediaLibraryResponse DTO
     */
    public MediaLibraryResponse toResponse(UserMediaInteraction interaction) {
        if (interaction == null) {
            return null;
        }
        
        MediaLibraryResponse response = new MediaLibraryResponse();
        response.setInteractionId(interaction.getId());
        response.setRating(interaction.getRating());
        response.setStatus(interaction.getStatus());
        response.setProgress(interaction.getProgress());
        response.setReview(interaction.getReview());
        response.setFavorite(interaction.isFavorite());
        response.setAddedAt(interaction.getCreatedAt());
        response.setUpdatedAt(interaction.getUpdatedAt());
        
        // Convert media information
        if (interaction.getMedia() != null) {
            response.setMedia(toMediaInfo(interaction.getMedia()));
        }
        
        return response;
    }
    
    /**
     * Converts a list of UserMediaInteraction entities to MediaLibraryResponse DTOs.
     * 
     * @param interactions List of entities to convert
     * @return List of MediaLibraryResponse DTOs
     */
    public List<MediaLibraryResponse> toResponseList(List<UserMediaInteraction> interactions) {
        if (interactions == null) {
            return List.of();
        }
        
        return interactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
    
    /**
     * Converts a Media entity to MediaInfo DTO.
     * Only exposes safe, public information about the media.
     * 
     * @param media The Media entity
     * @return MediaInfo DTO
     */
    private MediaLibraryResponse.MediaInfo toMediaInfo(Media media) {
        if (media == null) {
            return null;
        }
        
        MediaLibraryResponse.MediaInfo mediaInfo = new MediaLibraryResponse.MediaInfo();
        mediaInfo.setId(media.getId());
        mediaInfo.setType(media.getType() != null ? media.getType().name() : null);
        mediaInfo.setTitle(media.getTitle());
        mediaInfo.setOriginalTitle(media.getOriginalTitle());
        mediaInfo.setDescription(media.getDescription());
        mediaInfo.setReleaseDate(media.getReleaseDate());
        mediaInfo.setPosterUrl(media.getPosterUrl());
        mediaInfo.setBackdropUrl(media.getBackdropUrl());
        mediaInfo.setExternalId(media.getExternalId());
        mediaInfo.setExternalSource(media.getExternalSource());
        mediaInfo.setAverageRating(media.getAverageRating());
        mediaInfo.setRatingCount(media.getRatingCount());
        
        return mediaInfo;
    }
} 