package com.showsync.service.impl;

import com.showsync.dto.external.tmdb.TmdbMovieResponse;
import com.showsync.entity.Media;
import com.showsync.entity.User;
import com.showsync.entity.UserMediaInteraction;
import com.showsync.entity.UserMediaInteraction.Status;
import com.showsync.repository.MediaRepository;
import com.showsync.repository.UserMediaInteractionRepository;
import com.showsync.repository.UserRepository;
import com.showsync.service.external.ExternalMediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * Comprehensive unit tests for UserMediaLibraryServiceImpl.
 * Tests business logic, security validation, and error handling.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserMediaLibraryService Tests")
class UserMediaLibraryServiceImplTest {

    @Mock
    private UserMediaInteractionRepository userMediaInteractionRepository;
    
    @Mock
    private MediaRepository mediaRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private ExternalMediaService externalMediaService;
    
    @InjectMocks
    private UserMediaLibraryServiceImpl userMediaLibraryService;
    
    private User testUser;
    private Media testMedia;
    private UserMediaInteraction testInteraction;
    
    @BeforeEach
    void setUp() {
        testUser = createTestUser();
        testMedia = createTestMedia();
        testInteraction = createTestInteraction();
    }
    
    @Nested
    @DisplayName("Add Media to Library Tests")
    class AddMediaToLibraryTests {
        
        @Test
        @DisplayName("Should successfully add new media to library")
        void shouldAddNewMediaToLibrary() {
            // Given
            Long userId = 1L;
            String externalId = "550";
            String externalSource = "tmdb";
            Status initialStatus = Status.PLAN_TO_WATCH;
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(mediaRepository.findByExternalIdAndExternalSource(externalId, externalSource))
                    .thenReturn(Optional.of(testMedia));
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, testMedia.getId()))
                    .thenReturn(Optional.empty());
            when(userMediaInteractionRepository.save(any(UserMediaInteraction.class)))
                    .thenReturn(testInteraction);
            
            // When
            UserMediaInteraction result = userMediaLibraryService.addMediaToLibrary(
                    userId, externalId, externalSource, initialStatus);
            
            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(Status.PLAN_TO_WATCH);
            assertThat(result.isFavorite()).isFalse();
            
            verify(userMediaInteractionRepository).save(argThat(interaction -> 
                interaction.getUser().equals(testUser) && 
                interaction.getMedia().equals(testMedia) &&
                interaction.getStatus().equals(initialStatus)
            ));
        }
        
        @Test
        @DisplayName("Should create new media when not found in database")
        void shouldCreateNewMediaWhenNotFound() {
            // Given
            Long userId = 1L;
            String externalId = "999";
            String externalSource = "tmdb";
            
            // Mock external API response
            TmdbMovieResponse mockMovieResponse = new TmdbMovieResponse();
            mockMovieResponse.setId(Long.valueOf(externalId));
            mockMovieResponse.setTitle("Test Movie");
            mockMovieResponse.setOverview("Test movie description");
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(mediaRepository.findByExternalIdAndExternalSource(externalId, externalSource))
                    .thenReturn(Optional.empty());
            when(externalMediaService.getMovieDetails(Long.valueOf(externalId)))
                    .thenReturn(reactor.core.publisher.Mono.just(mockMovieResponse));
            when(mediaRepository.save(any(Media.class))).thenReturn(testMedia);
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, testMedia.getId()))
                    .thenReturn(Optional.empty());
            when(userMediaInteractionRepository.save(any(UserMediaInteraction.class)))
                    .thenReturn(testInteraction);
            
            // When
            UserMediaInteraction result = userMediaLibraryService.addMediaToLibrary(
                    userId, externalId, externalSource, Status.PLAN_TO_WATCH);
            
            // Then
            assertThat(result).isNotNull();
            verify(mediaRepository).save(argThat(media -> 
                media.getExternalId().equals(externalId) &&
                media.getExternalSource().equals(externalSource)
            ));
        }
        
        @Test
        @DisplayName("Should throw exception when media already in library")
        void shouldThrowExceptionWhenMediaAlreadyInLibrary() {
            // Given
            Long userId = 1L;
            String externalId = "550";
            String externalSource = "tmdb";
            
            lenient().when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(mediaRepository.findByExternalIdAndExternalSource(externalId, externalSource))
                    .thenReturn(Optional.of(testMedia));
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, testMedia.getId()))
                    .thenReturn(Optional.of(testInteraction));
            
            // When & Then
            assertThatThrownBy(() -> userMediaLibraryService.addMediaToLibrary(
                    userId, externalId, externalSource, Status.PLAN_TO_WATCH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Media is already in your library");
        }
        
        @Test
        @DisplayName("Should validate user ID")
        void shouldValidateUserId() {
            // When & Then
            assertThatThrownBy(() -> userMediaLibraryService.addMediaToLibrary(
                    null, "550", "tmdb", Status.PLAN_TO_WATCH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");
            
            assertThatThrownBy(() -> userMediaLibraryService.addMediaToLibrary(
                    0L, "550", "tmdb", Status.PLAN_TO_WATCH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid user ID");
        }
        
        @Test
        @DisplayName("Should validate external media parameters")
        void shouldValidateExternalMediaParameters() {
            // When & Then
            assertThatThrownBy(() -> userMediaLibraryService.addMediaToLibrary(
                    1L, null, "tmdb", Status.PLAN_TO_WATCH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("External media ID cannot be null or empty");
            
            assertThatThrownBy(() -> userMediaLibraryService.addMediaToLibrary(
                    1L, "", "tmdb", Status.PLAN_TO_WATCH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("External media ID cannot be null or empty");
            
            assertThatThrownBy(() -> userMediaLibraryService.addMediaToLibrary(
                    1L, "550", "invalid", Status.PLAN_TO_WATCH))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("External source must be 'tmdb' or 'openlibrary'");
        }
        
        @Test
        @DisplayName("Should default to PLAN_TO_WATCH when status is null")
        void shouldDefaultToPlanToWatchWhenStatusIsNull() {
            // Given
            Long userId = 1L;
            String externalId = "550";
            String externalSource = "tmdb";
            
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(mediaRepository.findByExternalIdAndExternalSource(externalId, externalSource))
                    .thenReturn(Optional.of(testMedia));
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, testMedia.getId()))
                    .thenReturn(Optional.empty());
            when(userMediaInteractionRepository.save(any(UserMediaInteraction.class)))
                    .thenReturn(testInteraction);
            
            // When
            userMediaLibraryService.addMediaToLibrary(userId, externalId, externalSource, null);
            
            // Then
            verify(userMediaInteractionRepository).save(argThat(interaction -> 
                interaction.getStatus().equals(Status.PLAN_TO_WATCH)
            ));
        }
    }
    
    @Nested
    @DisplayName("Update Media Rating Tests")
    class UpdateMediaRatingTests {
        
        @Test
        @DisplayName("Should successfully update rating")
        void shouldUpdateRating() {
            // Given
            Long userId = 1L;
            Long mediaId = 1L;
            Integer rating = 8;
            
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, mediaId))
                    .thenReturn(Optional.of(testInteraction));
            when(userMediaInteractionRepository.save(testInteraction))
                    .thenReturn(testInteraction);
            
            // When
            UserMediaInteraction result = userMediaLibraryService.updateMediaRating(userId, mediaId, rating);
            
            // Then
            assertThat(result.getRating()).isEqualTo(rating);
            verify(userMediaInteractionRepository).save(testInteraction);
        }
        
        @Test
        @DisplayName("Should validate rating range")
        void shouldValidateRatingRange() {
            // When & Then
            assertThatThrownBy(() -> userMediaLibraryService.updateMediaRating(1L, 1L, 0))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rating must be between 1 and 10");
            
            assertThatThrownBy(() -> userMediaLibraryService.updateMediaRating(1L, 1L, 11))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Rating must be between 1 and 10");
        }
        
        @Test
        @DisplayName("Should allow null rating (removing rating)")
        void shouldAllowNullRating() {
            // Given
            Long userId = 1L;
            Long mediaId = 1L;
            
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, mediaId))
                    .thenReturn(Optional.of(testInteraction));
            when(userMediaInteractionRepository.save(testInteraction))
                    .thenReturn(testInteraction);
            
            // When
            UserMediaInteraction result = userMediaLibraryService.updateMediaRating(userId, mediaId, null);
            
            // Then
            assertThat(result.getRating()).isNull();
        }
        
        @Test
        @DisplayName("Should throw SecurityException when user doesn't own interaction")
        void shouldThrowSecurityExceptionWhenUserDoesNotOwnInteraction() {
            // Given
            Long userId = 1L;
            Long mediaId = 1L;
            Integer rating = 8;
            
            User differentUser = new User();
            differentUser.setId(2L);
            testInteraction.setUser(differentUser);
            
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, mediaId))
                    .thenReturn(Optional.of(testInteraction));
            
            // When & Then
            assertThatThrownBy(() -> userMediaLibraryService.updateMediaRating(userId, mediaId, rating))
                    .isInstanceOf(SecurityException.class)
                    .hasMessage("Access denied: You can only modify your own library entries");
        }
    }
    
    @Nested
    @DisplayName("Get User Library Tests")
    class GetUserLibraryTests {
        
        @Test
        @DisplayName("Should return all user interactions when no status filter")
        void shouldReturnAllUserInteractions() {
            // Given
            Long userId = 1L;
            List<UserMediaInteraction> interactions = Arrays.asList(testInteraction);
            
            when(userMediaInteractionRepository.findByUserId(userId))
                    .thenReturn(interactions);
            
            // When
            List<UserMediaInteraction> result = userMediaLibraryService.getUserLibrary(userId, null);
            
            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testInteraction);
        }
        
        @Test
        @DisplayName("Should return filtered interactions when status provided")
        void shouldReturnFilteredInteractions() {
            // Given
            Long userId = 1L;
            Status status = Status.WATCHING;
            List<UserMediaInteraction> interactions = Arrays.asList(testInteraction);
            
            when(userMediaInteractionRepository.findByUserIdAndStatus(userId, status))
                    .thenReturn(interactions);
            
            // When
            List<UserMediaInteraction> result = userMediaLibraryService.getUserLibrary(userId, status);
            
            // Then
            assertThat(result).hasSize(1);
            verify(userMediaInteractionRepository).findByUserIdAndStatus(userId, status);
        }
    }
    
    @Nested
    @DisplayName("Remove Media From Library Tests")
    class RemoveMediaFromLibraryTests {
        
        @Test
        @DisplayName("Should successfully remove media from library")
        void shouldRemoveMediaFromLibrary() {
            // Given
            Long userId = 1L;
            Long mediaId = 1L;
            
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, mediaId))
                    .thenReturn(Optional.of(testInteraction));
            
            // When
            userMediaLibraryService.removeMediaFromLibrary(userId, mediaId);
            
            // Then
            verify(userMediaInteractionRepository).delete(testInteraction);
        }
        
        @Test
        @DisplayName("Should throw exception when media not found in library")
        void shouldThrowExceptionWhenMediaNotFound() {
            // Given
            Long userId = 1L;
            Long mediaId = 999L;
            
            when(userMediaInteractionRepository.findByUserIdAndMediaId(userId, mediaId))
                    .thenReturn(Optional.empty());
            
            // When & Then
            assertThatThrownBy(() -> userMediaLibraryService.removeMediaFromLibrary(userId, mediaId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Media not found in user's library");
        }
    }
    
    @Nested
    @DisplayName("Update Media Review Tests")  
    class UpdateMediaReviewTests {
        
        @Test
        @DisplayName("Should validate review length")
        void shouldValidateReviewLength() {
            // Given
            String longReview = "a".repeat(2001);
            
            // When & Then
            assertThatThrownBy(() -> userMediaLibraryService.updateMediaReview(1L, 1L, longReview))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Review cannot exceed 2000 characters");
        }
    }
    
    // Helper methods for creating test data
    private User createTestUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setDisplayName("Test User");
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        return user;
    }
    
    private Media createTestMedia() {
        Media media = new Media();
        media.setId(1L);
        media.setType(Media.MediaType.MOVIE);
        media.setTitle("Fight Club");
        media.setDescription("An insomniac office worker...");
        media.setExternalId("550");
        media.setExternalSource("tmdb");
        media.setCreatedAt(LocalDateTime.now());
        media.setUpdatedAt(LocalDateTime.now());
        return media;
    }
    
    private UserMediaInteraction createTestInteraction() {
        UserMediaInteraction interaction = new UserMediaInteraction();
        interaction.setId(1L);
        interaction.setUser(testUser);
        interaction.setMedia(testMedia);
        interaction.setStatus(Status.PLAN_TO_WATCH);
        interaction.setRating(null);
        interaction.setFavorite(false);
        interaction.setCreatedAt(LocalDateTime.now());
        interaction.setUpdatedAt(LocalDateTime.now());
        return interaction;
    }
} 