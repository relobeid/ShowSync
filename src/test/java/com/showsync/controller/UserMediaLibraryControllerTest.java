package com.showsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.showsync.dto.library.AddMediaToLibraryRequest;
import com.showsync.dto.library.UpdateMediaRequest;
import com.showsync.entity.Media;
import com.showsync.entity.User;
import com.showsync.entity.UserMediaInteraction;
import com.showsync.entity.UserMediaInteraction.Status;
import com.showsync.repository.MediaRepository;
import com.showsync.repository.UserMediaInteractionRepository;
import com.showsync.repository.UserRepository;
import com.showsync.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for UserMediaLibraryController.
 * Tests complete HTTP request/response cycle with authentication and security.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-15
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Transactional
@DisplayName("UserMediaLibraryController Integration Tests")
class UserMediaLibraryControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private MediaRepository mediaRepository;
    
    @Autowired
    private UserMediaInteractionRepository userMediaInteractionRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    private MockMvc mockMvc;
    private User testUser;
    private Media testMedia;
    private String jwtToken;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();
        
        // Create test user
        testUser = createAndSaveTestUser();
        
        // Create test media
        testMedia = createAndSaveTestMedia();
        
        // Generate JWT token for authentication
        jwtToken = jwtUtil.generateToken(testUser.getUsername(), testUser.getRole().name(), testUser.getId());
    }
    
    @Nested
    @DisplayName("Add Media to Library Tests")
    class AddMediaToLibraryTests {
        
        @Test
        @DisplayName("Should successfully add media to library")
        void shouldAddMediaToLibrary() throws Exception {
            // Given
            AddMediaToLibraryRequest request = new AddMediaToLibraryRequest();
            request.setExternalId("550");
            request.setExternalSource("tmdb");
            request.setInitialStatus(Status.PLAN_TO_WATCH);
            
            // When & Then
            mockMvc.perform(post("/api/library")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.interactionId").isNumber())
                    .andExpect(jsonPath("$.status").value("PLAN_TO_WATCH"))
                    .andExpect(jsonPath("$.favorite").value(false))
                    .andExpect(jsonPath("$.media.externalId").value("550"))
                    .andExpect(jsonPath("$.media.externalSource").value("tmdb"));
        }
        
        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Given
            AddMediaToLibraryRequest request = new AddMediaToLibraryRequest();
            request.setExternalId("550");
            request.setExternalSource("tmdb");
            
            // When & Then
            mockMvc.perform(post("/api/library")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("Should return 400 when request is invalid")
        void shouldReturn400WhenRequestIsInvalid() throws Exception {
            // Given - Invalid request with missing required fields
            AddMediaToLibraryRequest request = new AddMediaToLibraryRequest();
            // Missing externalId and externalSource
            
            // When & Then
            mockMvc.perform(post("/api/library")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 409 when media already in library")
        void shouldReturn409WhenMediaAlreadyInLibrary() throws Exception {
            // Given - Add media to library first
            UserMediaInteraction existingInteraction = createAndSaveTestInteraction();
            
            AddMediaToLibraryRequest request = new AddMediaToLibraryRequest();
            request.setExternalId(testMedia.getExternalId());
            request.setExternalSource(testMedia.getExternalSource());
            
            // When & Then
            mockMvc.perform(post("/api/library")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Media is already in your library"));
        }
    }
    
    @Nested
    @DisplayName("Get User Library Tests")
    class GetUserLibraryTests {
        
        @Test
        @DisplayName("Should return user's complete library")
        void shouldReturnUserLibrary() throws Exception {
            // Given
            UserMediaInteraction interaction = createAndSaveTestInteraction();
            
            // When & Then
            mockMvc.perform(get("/api/library")
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].interactionId").value(interaction.getId()))
                    .andExpect(jsonPath("$[0].status").value("PLAN_TO_WATCH"))
                    .andExpect(jsonPath("$[0].media.title").value(testMedia.getTitle()));
        }
        
        @Test
        @DisplayName("Should return filtered library by status")
        void shouldReturnFilteredLibraryByStatus() throws Exception {
            // Given
            UserMediaInteraction interaction1 = createAndSaveTestInteraction();
            
            UserMediaInteraction interaction2 = new UserMediaInteraction();
            interaction2.setUser(testUser);
            interaction2.setMedia(testMedia);
            interaction2.setStatus(Status.WATCHING);
            interaction2.setFavorite(false);
            userMediaInteractionRepository.save(interaction2);
            
            // When & Then - Filter by WATCHING status
            mockMvc.perform(get("/api/library")
                    .param("status", "WATCHING")
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].status").value("WATCHING"));
        }
        
        @Test
        @DisplayName("Should return empty array when no media in library")
        void shouldReturnEmptyArrayWhenNoMediaInLibrary() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/library")
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }
    }
    
    @Nested
    @DisplayName("Update Media in Library Tests")
    class UpdateMediaInLibraryTests {
        
        @Test
        @DisplayName("Should successfully update media rating")
        void shouldUpdateMediaRating() throws Exception {
            // Given
            UserMediaInteraction interaction = createAndSaveTestInteraction();
            
            UpdateMediaRequest request = new UpdateMediaRequest();
            request.setRating(8);
            
            // When & Then
            mockMvc.perform(put("/api/library/" + testMedia.getId())
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(8));
        }
        
        @Test
        @DisplayName("Should successfully update media status")
        void shouldUpdateMediaStatus() throws Exception {
            // Given
            UserMediaInteraction interaction = createAndSaveTestInteraction();
            
            UpdateMediaRequest request = new UpdateMediaRequest();
            request.setStatus(Status.COMPLETED);
            
            // When & Then
            mockMvc.perform(put("/api/library/" + testMedia.getId())
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("COMPLETED"));
        }
        
        @Test
        @DisplayName("Should return 400 when rating is invalid")
        void shouldReturn400WhenRatingIsInvalid() throws Exception {
            // Given
            UserMediaInteraction interaction = createAndSaveTestInteraction();
            
            UpdateMediaRequest request = new UpdateMediaRequest();
            request.setRating(11); // Invalid rating > 10
            
            // When & Then
            mockMvc.perform(put("/api/library/" + testMedia.getId())
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
        
        @Test
        @DisplayName("Should return 400 when media not in library")
        void shouldReturn400WhenMediaNotInLibrary() throws Exception {
            // Given
            UpdateMediaRequest request = new UpdateMediaRequest();
            request.setRating(8);
            
            // When & Then
            mockMvc.perform(put("/api/library/999")
                    .header("Authorization", "Bearer " + jwtToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }
    }
    
    @Nested
    @DisplayName("Remove Media from Library Tests")
    class RemoveMediaFromLibraryTests {
        
        @Test
        @DisplayName("Should successfully remove media from library")
        void shouldRemoveMediaFromLibrary() throws Exception {
            // Given
            UserMediaInteraction interaction = createAndSaveTestInteraction();
            
            // When & Then
            mockMvc.perform(delete("/api/library/" + testMedia.getId())
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isNoContent());
            
            // Verify media is removed
            mockMvc.perform(get("/api/library/" + testMedia.getId())
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
        
        @Test
        @DisplayName("Should return 404 when media not in library")
        void shouldReturn404WhenMediaNotInLibrary() throws Exception {
            // When & Then
            mockMvc.perform(delete("/api/library/999")
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }
    
    @Nested
    @DisplayName("Get User Favorites Tests")
    class GetUserFavoritesTests {
        
        @Test
        @DisplayName("Should return user's favorite media")
        void shouldReturnUserFavorites() throws Exception {
            // Given
            UserMediaInteraction interaction = createAndSaveTestInteraction();
            interaction.setFavorite(true);
            userMediaInteractionRepository.save(interaction);
            
            // When & Then
            mockMvc.perform(get("/api/library/favorites")
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].favorite").value(true));
        }
    }
    
    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        
        @Test
        @DisplayName("Should prevent access to other user's library")
        void shouldPreventAccessToOtherUserLibrary() throws Exception {
            // Given - Create another user and their library
            User otherUser = new User();
            otherUser.setUsername("otheruser");
            otherUser.setEmail("other@example.com");
            otherUser.setPassword(passwordEncoder.encode("password"));
            otherUser.setDisplayName("Other User");
            otherUser.setRole(User.Role.USER);
            otherUser.setActive(true);
            otherUser.setEmailVerified(true);
            otherUser = userRepository.save(otherUser);
            
            UserMediaInteraction otherInteraction = new UserMediaInteraction();
            otherInteraction.setUser(otherUser);
            otherInteraction.setMedia(testMedia);
            otherInteraction.setStatus(Status.WATCHING);
            otherInteraction.setFavorite(false);
            otherInteraction = userMediaInteractionRepository.save(otherInteraction);
            
            // When & Then - Try to access other user's library item
            mockMvc.perform(get("/api/library/" + testMedia.getId())
                    .header("Authorization", "Bearer " + jwtToken))
                    .andDo(print())
                    .andExpect(status().isNotFound()); // Should not find because it belongs to other user
        }
        
        @Test
        @DisplayName("Should reject invalid JWT token")
        void shouldRejectInvalidJwtToken() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/library")
                    .header("Authorization", "Bearer invalid-token"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
    
    // Helper methods for creating test data
    private User createAndSaveTestUser() {
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setDisplayName("Test User");
        user.setRole(User.Role.USER);
        user.setActive(true);
        user.setEmailVerified(true);
        return userRepository.save(user);
    }
    
    private Media createAndSaveTestMedia() {
        Media media = new Media();
        media.setType(Media.MediaType.MOVIE);
        media.setTitle("Fight Club");
        media.setDescription("An insomniac office worker and a devil-may-care soap maker...");
        media.setExternalId("550");
        media.setExternalSource("tmdb");
        media.setReleaseDate(LocalDateTime.of(1999, 10, 15, 0, 0));
        return mediaRepository.save(media);
    }
    
    private UserMediaInteraction createAndSaveTestInteraction() {
        UserMediaInteraction interaction = new UserMediaInteraction();
        interaction.setUser(testUser);
        interaction.setMedia(testMedia);
        interaction.setStatus(Status.PLAN_TO_WATCH);
        interaction.setFavorite(false);
        return userMediaInteractionRepository.save(interaction);
    }
} 