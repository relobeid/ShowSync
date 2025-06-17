package com.showsync.service;

import com.showsync.entity.UserMediaInteraction;
import com.showsync.entity.UserMediaInteraction.Status;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing user's personal media library.
 * Handles all operations related to user-media interactions including
 * adding media to library, rating, status tracking, and removal.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
public interface UserMediaLibraryService {
    
    /**
     * Adds media to user's library with initial status.
     * If media doesn't exist in database, it will be created from external API data.
     * 
     * @param userId The authenticated user's ID
     * @param externalId External media ID (TMDb ID or Open Library ID)
     * @param externalSource Source of the media ("tmdb" or "openlibrary")
     * @param initialStatus Initial status for the media (default: PLAN_TO_WATCH)
     * @return UserMediaInteraction representing the added media
     * @throws IllegalArgumentException if media cannot be found or user is invalid
     * @throws RuntimeException if external API is unavailable
     */
    UserMediaInteraction addMediaToLibrary(Long userId, String externalId, String externalSource, Status initialStatus);
    
    /**
     * Retrieves user's complete media library with optional filtering.
     * 
     * @param userId The authenticated user's ID
     * @param status Optional status filter (null for all statuses)
     * @return List of UserMediaInteraction objects representing user's library
     */
    List<UserMediaInteraction> getUserLibrary(Long userId, Status status);
    
    /**
     * Updates the rating for a media item in user's library.
     * Rating must be between 1-10 inclusive.
     * 
     * @param userId The authenticated user's ID
     * @param mediaId The media ID in user's library
     * @param rating Rating value (1-10)
     * @return Updated UserMediaInteraction
     * @throws IllegalArgumentException if rating is invalid or media not in library
     * @throws SecurityException if user doesn't own this library entry
     */
    UserMediaInteraction updateMediaRating(Long userId, Long mediaId, Integer rating);
    
    /**
     * Updates the status of a media item in user's library.
     * 
     * @param userId The authenticated user's ID
     * @param mediaId The media ID in user's library
     * @param status New status for the media
     * @return Updated UserMediaInteraction
     * @throws IllegalArgumentException if media not in library
     * @throws SecurityException if user doesn't own this library entry
     */
    UserMediaInteraction updateMediaStatus(Long userId, Long mediaId, Status status);
    
    /**
     * Updates the progress for a media item (useful for TV shows, books).
     * 
     * @param userId The authenticated user's ID
     * @param mediaId The media ID in user's library
     * @param progress Progress value (episodes watched, pages read, etc.)
     * @return Updated UserMediaInteraction
     * @throws IllegalArgumentException if media not in library or progress is negative
     * @throws SecurityException if user doesn't own this library entry
     */
    UserMediaInteraction updateMediaProgress(Long userId, Long mediaId, Integer progress);
    
    /**
     * Adds or updates a review for a media item in user's library.
     * 
     * @param userId The authenticated user's ID
     * @param mediaId The media ID in user's library
     * @param review Review text (max 2000 characters)
     * @return Updated UserMediaInteraction
     * @throws IllegalArgumentException if media not in library or review too long
     * @throws SecurityException if user doesn't own this library entry
     */
    UserMediaInteraction updateMediaReview(Long userId, Long mediaId, String review);
    
    /**
     * Toggles favorite status for a media item in user's library.
     * 
     * @param userId The authenticated user's ID
     * @param mediaId The media ID in user's library
     * @return Updated UserMediaInteraction with toggled favorite status
     * @throws IllegalArgumentException if media not in library
     * @throws SecurityException if user doesn't own this library entry
     */
    UserMediaInteraction toggleMediaFavorite(Long userId, Long mediaId);
    
    /**
     * Removes media from user's library completely.
     * 
     * @param userId The authenticated user's ID
     * @param mediaId The media ID in user's library
     * @throws IllegalArgumentException if media not in library
     * @throws SecurityException if user doesn't own this library entry
     */
    void removeMediaFromLibrary(Long userId, Long mediaId);
    
    /**
     * Gets a specific media interaction from user's library.
     * 
     * @param userId The authenticated user's ID
     * @param mediaId The media ID
     * @return Optional UserMediaInteraction
     */
    Optional<UserMediaInteraction> getUserMediaInteraction(Long userId, Long mediaId);
    
    /**
     * Gets user's favorite media items.
     * 
     * @param userId The authenticated user's ID
     * @return List of favorite UserMediaInteraction objects
     */
    List<UserMediaInteraction> getUserFavorites(Long userId);
} 