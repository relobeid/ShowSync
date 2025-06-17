package com.showsync.service.impl;

import com.showsync.entity.Media;
import com.showsync.entity.User;
import com.showsync.entity.UserMediaInteraction;
import com.showsync.entity.UserMediaInteraction.Status;
import com.showsync.repository.MediaRepository;
import com.showsync.repository.UserMediaInteractionRepository;
import com.showsync.repository.UserRepository;
import com.showsync.service.UserMediaLibraryService;
import com.showsync.service.external.ExternalMediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of UserMediaLibraryService.
 * Provides secure, transactional operations for user's personal media library.
 * 
 * Security Considerations:
 * - All operations validate user ownership of library entries
 * - Input validation prevents malicious data injection
 * - Proper exception handling prevents information leakage
 * - Comprehensive audit logging for user actions
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@Slf4j
@Service
@Transactional
public class UserMediaLibraryServiceImpl implements UserMediaLibraryService {
    
    private final UserMediaInteractionRepository userMediaInteractionRepository;
    private final MediaRepository mediaRepository;
    private final UserRepository userRepository;
    private final ExternalMediaService externalMediaService;
    
    @Autowired
    public UserMediaLibraryServiceImpl(
            UserMediaInteractionRepository userMediaInteractionRepository,
            MediaRepository mediaRepository,
            UserRepository userRepository,
            ExternalMediaService externalMediaService) {
        this.userMediaInteractionRepository = userMediaInteractionRepository;
        this.mediaRepository = mediaRepository;
        this.userRepository = userRepository;
        this.externalMediaService = externalMediaService;
    }
    
    @Override
    public UserMediaInteraction addMediaToLibrary(Long userId, String externalId, String externalSource, Status initialStatus) {
        log.info("Adding media to library - userId: {}, externalId: {}, source: {}, status: {}", 
                userId, externalId, externalSource, initialStatus);
        
        // Validate inputs
        validateUserId(userId);
        validateExternalMediaId(externalId, externalSource);
        
        if (initialStatus == null) {
            initialStatus = Status.PLAN_TO_WATCH;
        }
        
        // Check if media already exists in user's library
        Media media = findOrCreateMedia(externalId, externalSource);
        Optional<UserMediaInteraction> existingInteraction = 
                userMediaInteractionRepository.findByUserIdAndMediaId(userId, media.getId());
        
        if (existingInteraction.isPresent()) {
            log.warn("Media already in library - userId: {}, mediaId: {}", userId, media.getId());
            throw new IllegalArgumentException("Media is already in your library");
        }
        
        // Create new interaction
        UserMediaInteraction interaction = new UserMediaInteraction();
        interaction.setUser(getUserById(userId));
        interaction.setMedia(media);
        interaction.setStatus(initialStatus);
        interaction.setFavorite(false);
        
        UserMediaInteraction saved = userMediaInteractionRepository.save(interaction);
        log.info("Media added to library successfully - interactionId: {}", saved.getId());
        
        return saved;
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserMediaInteraction> getUserLibrary(Long userId, Status status) {
        log.debug("Retrieving user library - userId: {}, status filter: {}", userId, status);
        
        validateUserId(userId);
        
        List<UserMediaInteraction> interactions;
        if (status != null) {
            interactions = userMediaInteractionRepository.findByUserIdAndStatus(userId, status);
        } else {
            interactions = userMediaInteractionRepository.findByUserId(userId);
        }
        
        log.debug("Retrieved {} interactions for user {}", interactions.size(), userId);
        return interactions;
    }
    
    @Override
    public UserMediaInteraction updateMediaRating(Long userId, Long mediaId, Integer rating) {
        log.info("Updating media rating - userId: {}, mediaId: {}, rating: {}", userId, mediaId, rating);
        
        validateRating(rating);
        
        UserMediaInteraction interaction = validateAndGetUserMediaInteraction(userId, mediaId);
        interaction.setRating(rating);
        
        UserMediaInteraction updated = userMediaInteractionRepository.save(interaction);
        log.info("Media rating updated successfully - interactionId: {}", updated.getId());
        
        return updated;
    }
    
    @Override
    public UserMediaInteraction updateMediaStatus(Long userId, Long mediaId, Status status) {
        log.info("Updating media status - userId: {}, mediaId: {}, status: {}", userId, mediaId, status);
        
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }
        
        UserMediaInteraction interaction = validateAndGetUserMediaInteraction(userId, mediaId);
        interaction.setStatus(status);
        
        UserMediaInteraction updated = userMediaInteractionRepository.save(interaction);
        log.info("Media status updated successfully - interactionId: {}", updated.getId());
        
        return updated;
    }
    
    @Override
    public UserMediaInteraction updateMediaProgress(Long userId, Long mediaId, Integer progress) {
        log.info("Updating media progress - userId: {}, mediaId: {}, progress: {}", userId, mediaId, progress);
        
        if (progress != null && progress < 0) {
            throw new IllegalArgumentException("Progress cannot be negative");
        }
        
        UserMediaInteraction interaction = validateAndGetUserMediaInteraction(userId, mediaId);
        interaction.setProgress(progress);
        
        UserMediaInteraction updated = userMediaInteractionRepository.save(interaction);
        log.info("Media progress updated successfully - interactionId: {}", updated.getId());
        
        return updated;
    }
    
    @Override
    public UserMediaInteraction updateMediaReview(Long userId, Long mediaId, String review) {
        log.info("Updating media review - userId: {}, mediaId: {}, reviewLength: {}", 
                userId, mediaId, review != null ? review.length() : 0);
        
        if (review != null && review.length() > 2000) {
            throw new IllegalArgumentException("Review cannot exceed 2000 characters");
        }
        
        UserMediaInteraction interaction = validateAndGetUserMediaInteraction(userId, mediaId);
        interaction.setReview(review);
        
        UserMediaInteraction updated = userMediaInteractionRepository.save(interaction);
        log.info("Media review updated successfully - interactionId: {}", updated.getId());
        
        return updated;
    }
    
    @Override
    public UserMediaInteraction toggleMediaFavorite(Long userId, Long mediaId) {
        log.info("Toggling media favorite - userId: {}, mediaId: {}", userId, mediaId);
        
        UserMediaInteraction interaction = validateAndGetUserMediaInteraction(userId, mediaId);
        interaction.setFavorite(!interaction.isFavorite());
        
        UserMediaInteraction updated = userMediaInteractionRepository.save(interaction);
        log.info("Media favorite toggled successfully - interactionId: {}, isFavorite: {}", 
                updated.getId(), updated.isFavorite());
        
        return updated;
    }
    
    @Override
    public void removeMediaFromLibrary(Long userId, Long mediaId) {
        log.info("Removing media from library - userId: {}, mediaId: {}", userId, mediaId);
        
        UserMediaInteraction interaction = validateAndGetUserMediaInteraction(userId, mediaId);
        userMediaInteractionRepository.delete(interaction);
        
        log.info("Media removed from library successfully - interactionId: {}", interaction.getId());
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<UserMediaInteraction> getUserMediaInteraction(Long userId, Long mediaId) {
        log.debug("Getting user media interaction - userId: {}, mediaId: {}", userId, mediaId);
        
        validateUserId(userId);
        
        return userMediaInteractionRepository.findByUserIdAndMediaId(userId, mediaId);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<UserMediaInteraction> getUserFavorites(Long userId) {
        log.debug("Getting user favorites - userId: {}", userId);
        
        validateUserId(userId);
        
        return userMediaInteractionRepository.findByUserIdAndIsFavoriteTrue(userId);
    }
    
    // Private validation and helper methods
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }
    
    private void validateExternalMediaId(String externalId, String externalSource) {
        if (externalId == null || externalId.trim().isEmpty()) {
            throw new IllegalArgumentException("External media ID cannot be null or empty");
        }
        
        if (externalSource == null || (!externalSource.equals("tmdb") && !externalSource.equals("openlibrary"))) {
            throw new IllegalArgumentException("External source must be 'tmdb' or 'openlibrary'");
        }
    }
    
    private void validateRating(Integer rating) {
        if (rating != null && (rating < 1 || rating > 10)) {
            throw new IllegalArgumentException("Rating must be between 1 and 10");
        }
    }
    
    private UserMediaInteraction validateAndGetUserMediaInteraction(Long userId, Long mediaId) {
        validateUserId(userId);
        
        if (mediaId == null || mediaId <= 0) {
            throw new IllegalArgumentException("Invalid media ID");
        }
        
        Optional<UserMediaInteraction> interaction = 
                userMediaInteractionRepository.findByUserIdAndMediaId(userId, mediaId);
        
        if (interaction.isEmpty()) {
            throw new IllegalArgumentException("Media not found in user's library");
        }
        
        // Security check: Ensure the interaction belongs to the authenticated user
        if (!interaction.get().getUser().getId().equals(userId)) {
            log.warn("Security violation: User {} attempted to access interaction belonging to user {}", 
                    userId, interaction.get().getUser().getId());
            throw new SecurityException("Access denied: You can only modify your own library entries");
        }
        
        return interaction.get();
    }
    
    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }
    
    private Media findOrCreateMedia(String externalId, String externalSource) {
        // First, check if media already exists in our database
        Optional<Media> existingMedia = mediaRepository.findByExternalIdAndExternalSource(externalId, externalSource);
        
        if (existingMedia.isPresent()) {
            log.debug("Media found in database - mediaId: {}", existingMedia.get().getId());
            return existingMedia.get();
        }
        
        // Media doesn't exist, fetch from external API and create it
        log.info("Creating new media from external API - externalId: {}, source: {}", externalId, externalSource);
        
        try {
            Media newMedia = createMediaFromExternalApi(externalId, externalSource);
            Media saved = mediaRepository.save(newMedia);
            log.info("New media created successfully - mediaId: {}, title: {}", saved.getId(), saved.getTitle());
            return saved;
        } catch (Exception e) {
            log.error("Failed to create media from external API - externalId: {}, source: {}", 
                    externalId, externalSource, e);
            throw new RuntimeException("Unable to fetch media information from external source", e);
        }
    }
    
    private Media createMediaFromExternalApi(String externalId, String externalSource) {
        // This is a placeholder implementation - we'll integrate with ExternalMediaService
        // For now, create a basic media object
        Media media = new Media();
        media.setExternalId(externalId);
        media.setExternalSource(externalSource);
        media.setTitle("Unknown Title"); // This will be fetched from external API
        media.setType(determineMediaType(externalSource));
        media.setCreatedAt(LocalDateTime.now());
        media.setUpdatedAt(LocalDateTime.now());
        
        // TODO: Implement actual external API integration
        log.warn("Using placeholder media creation - external API integration pending");
        
        return media;
    }
    
    private Media.MediaType determineMediaType(String externalSource) {
        return switch (externalSource) {
            case "tmdb" -> Media.MediaType.MOVIE; // Default for TMDb, could be TV_SHOW
            case "openlibrary" -> Media.MediaType.BOOK;
            default -> throw new IllegalArgumentException("Unknown external source: " + externalSource);
        };
    }
} 