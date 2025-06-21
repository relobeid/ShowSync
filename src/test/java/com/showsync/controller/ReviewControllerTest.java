package com.showsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.showsync.config.TestSecurityConfig;
import com.showsync.dto.review.CreateReviewRequest;
import com.showsync.entity.*;
import com.showsync.repository.*;
import com.showsync.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@Transactional
@DisplayName("ReviewController Integration Tests")
class ReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MediaRepository mediaRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ReviewVoteRepository reviewVoteRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private User testUser;
    private User otherUser;
    private Media testMedia;
    private Review testReview;
    private String authToken;
    private String otherUserToken;

    @BeforeEach
    void setUp() {
        // Clean up
        reviewVoteRepository.deleteAll();
        reviewRepository.deleteAll();
        mediaRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setDisplayName("Test User");
        testUser.setRole(User.Role.USER);
        testUser.setActive(true);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Create other user
        otherUser = new User();
        otherUser.setUsername("otheruser");
        otherUser.setEmail("other@example.com");
        otherUser.setPassword("hashedpassword");
        otherUser.setDisplayName("Other User");
        otherUser.setRole(User.Role.USER);
        otherUser.setActive(true);
        otherUser.setEmailVerified(true);
        otherUser.setCreatedAt(LocalDateTime.now());
        otherUser.setUpdatedAt(LocalDateTime.now());
        otherUser = userRepository.save(otherUser);

        // Create test media
        testMedia = new Media();
        testMedia.setTitle("Test Movie");
        testMedia.setType(Media.MediaType.MOVIE);
        testMedia.setDescription("A test movie for testing");
        testMedia.setExternalId("12345");
        testMedia.setExternalSource("tmdb");
        testMedia.setCreatedAt(LocalDateTime.now());
        testMedia.setUpdatedAt(LocalDateTime.now());
        testMedia = mediaRepository.save(testMedia);

        // Create test review
        testReview = new Review();
        testReview.setUser(testUser);
        testReview.setMedia(testMedia);
        testReview.setTitle("Great movie!");
        testReview.setContent("This is a fantastic movie with great acting.");
        testReview.setRating(8);
        testReview.setHelpfulVotes(0);
        testReview.setTotalVotes(0);
        testReview.setSpoiler(false);
        testReview.setModerated(false);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());
        testReview = reviewRepository.save(testReview);

        // Generate auth tokens
        authToken = jwtUtil.generateToken(testUser.getUsername(), testUser.getRole().toString(), testUser.getId());
        otherUserToken = jwtUtil.generateToken(otherUser.getUsername(), otherUser.getRole().toString(), otherUser.getId());
    }

    @Nested
    @DisplayName("Create Review Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review successfully with valid data")
        void shouldCreateReviewSuccessfully() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setMediaId(testMedia.getId());
            request.setTitle("Amazing movie!");
            request.setContent("This movie exceeded all my expectations. The plot was engaging and the characters were well-developed.");
            request.setRating(9);
            request.setSpoiler(false);

            mockMvc.perform(post("/api/reviews")
                    .header("Authorization", "Bearer " + otherUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("Amazing movie!"))
                    .andExpect(jsonPath("$.content").value(request.getContent()))
                    .andExpect(jsonPath("$.rating").value(9))
                    .andExpect(jsonPath("$.spoiler").value(false))
                    .andExpect(jsonPath("$.user.username").value("otheruser"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setMediaId(testMedia.getId());
            request.setTitle("Test review");
            request.setContent("Test content");
            request.setRating(7);

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 with invalid rating")
        void shouldReturn400WithInvalidRating() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setMediaId(testMedia.getId());
            request.setTitle("Test review");
            request.setContent("Test content");
            request.setRating(11); // Invalid rating

            mockMvc.perform(post("/api/reviews")
                    .header("Authorization", "Bearer " + otherUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 with empty content")
        void shouldReturn400WithEmptyContent() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setMediaId(testMedia.getId());
            request.setTitle("Test review");
            request.setContent(""); // Empty content
            request.setRating(7);

            mockMvc.perform(post("/api/reviews")
                    .header("Authorization", "Bearer " + otherUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when user already reviewed media")
        void shouldReturn400WhenUserAlreadyReviewed() throws Exception {
            CreateReviewRequest request = new CreateReviewRequest();
            request.setMediaId(testMedia.getId());
            request.setTitle("Another review");
            request.setContent("Trying to review again");
            request.setRating(7);

            mockMvc.perform(post("/api/reviews")
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Update Review Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update own review successfully")
        void shouldUpdateOwnReviewSuccessfully() throws Exception {
            CreateReviewRequest updateRequest = new CreateReviewRequest();
            updateRequest.setTitle("Updated title");
            updateRequest.setContent("Updated content with more details about the movie.");
            updateRequest.setRating(9);
            updateRequest.setSpoiler(true);

            mockMvc.perform(put("/api/reviews/{reviewId}", testReview.getId())
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.title").value("Updated title"))
                    .andExpect(jsonPath("$.content").value(updateRequest.getContent()))
                    .andExpect(jsonPath("$.rating").value(9))
                    .andExpect(jsonPath("$.spoiler").value(true));
        }

        @Test
        @DisplayName("Should return 403 when updating other user's review")
        void shouldReturn403WhenUpdatingOtherUsersReview() throws Exception {
            CreateReviewRequest updateRequest = new CreateReviewRequest();
            updateRequest.setTitle("Hacked title");
            updateRequest.setContent("Hacked content");
            updateRequest.setRating(1);

            mockMvc.perform(put("/api/reviews/{reviewId}", testReview.getId())
                    .header("Authorization", "Bearer " + otherUserToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 for non-existent review")
        void shouldReturn404ForNonExistentReview() throws Exception {
            CreateReviewRequest updateRequest = new CreateReviewRequest();
            updateRequest.setTitle("Updated title");
            updateRequest.setContent("Updated content");
            updateRequest.setRating(8);

            mockMvc.perform(put("/api/reviews/{reviewId}", 99999L)
                    .header("Authorization", "Bearer " + authToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Delete Review Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete own review successfully")
        void shouldDeleteOwnReviewSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/reviews/{reviewId}", testReview.getId())
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNoContent());

            // Verify review is deleted
            mockMvc.perform(get("/api/reviews/{reviewId}", testReview.getId())
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 when deleting other user's review")
        void shouldReturn403WhenDeletingOtherUsersReview() throws Exception {
            mockMvc.perform(delete("/api/reviews/{reviewId}", testReview.getId())
                    .header("Authorization", "Bearer " + otherUserToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Vote on Review Tests")
    class VoteOnReviewTests {

        @Test
        @DisplayName("Should add helpful vote successfully")
        void shouldAddHelpfulVoteSuccessfully() throws Exception {
            mockMvc.perform(post("/api/reviews/{reviewId}/vote", testReview.getId())
                    .header("Authorization", "Bearer " + otherUserToken)
                    .param("helpful", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.helpfulVotes").value(1))
                    .andExpect(jsonPath("$.totalVotes").value(1));
        }

        @Test
        @DisplayName("Should add not helpful vote successfully")
        void shouldAddNotHelpfulVoteSuccessfully() throws Exception {
            mockMvc.perform(post("/api/reviews/{reviewId}/vote", testReview.getId())
                    .header("Authorization", "Bearer " + otherUserToken)
                    .param("helpful", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.helpfulVotes").value(0))
                    .andExpect(jsonPath("$.totalVotes").value(1));
        }

        @Test
        @DisplayName("Should return 400 when voting on own review")
        void shouldReturn400WhenVotingOnOwnReview() throws Exception {
            mockMvc.perform(post("/api/reviews/{reviewId}/vote", testReview.getId())
                    .header("Authorization", "Bearer " + authToken)
                    .param("helpful", "true"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should remove vote successfully")
        void shouldRemoveVoteSuccessfully() throws Exception {
            // First add a vote
            mockMvc.perform(post("/api/reviews/{reviewId}/vote", testReview.getId())
                    .header("Authorization", "Bearer " + otherUserToken)
                    .param("helpful", "true"))
                    .andExpect(status().isOk());

            // Then remove it
            mockMvc.perform(delete("/api/reviews/{reviewId}/vote", testReview.getId())
                    .header("Authorization", "Bearer " + otherUserToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.helpfulVotes").value(0))
                    .andExpect(jsonPath("$.totalVotes").value(0));
        }
    }

    @Nested
    @DisplayName("Get Media Details Tests")
    class GetMediaDetailsTests {

        @Test
        @DisplayName("Should get media details successfully")
        void shouldGetMediaDetailsSuccessfully() throws Exception {
            mockMvc.perform(get("/api/reviews/media/{mediaId}", testMedia.getId())
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testMedia.getId()))
                    .andExpect(jsonPath("$.title").value(testMedia.getTitle()))
                    .andExpect(jsonPath("$.type").value("MOVIE"))
                    .andExpect(jsonPath("$.statistics").exists())
                    .andExpect(jsonPath("$.statistics.totalReviews").value(1))
                    .andExpect(jsonPath("$.recentReviews").isArray())
                    .andExpect(jsonPath("$.recentReviews", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 404 for non-existent media")
        void shouldReturn404ForNonExistentMedia() throws Exception {
            mockMvc.perform(get("/api/reviews/media/{mediaId}", 99999L)
                    .header("Authorization", "Bearer " + authToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Get Reviews for Media Tests")
    class GetReviewsForMediaTests {

        @Test
        @DisplayName("Should get reviews for media with pagination")
        void shouldGetReviewsForMediaWithPagination() throws Exception {
            mockMvc.perform(get("/api/reviews/media/{mediaId}/reviews", testMedia.getId())
                    .header("Authorization", "Bearer " + authToken)
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].id").value(testReview.getId()))
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.totalPages").value(1));
        }
    }

    @Nested
    @DisplayName("Get Most Helpful Reviews Tests")
    class GetMostHelpfulReviewsTests {

        @Test
        @DisplayName("Should get most helpful reviews")
        void shouldGetMostHelpfulReviews() throws Exception {
            // Add some votes to make the review "helpful"
            ReviewVote vote = new ReviewVote();
            vote.setUser(otherUser);
            vote.setReview(testReview);
            vote.setHelpful(true);
            vote.setCreatedAt(LocalDateTime.now());
            reviewVoteRepository.save(vote);

            // Update review vote counts
            testReview.setHelpfulVotes(1);
            testReview.setTotalVotes(1);
            reviewRepository.save(testReview);

            mockMvc.perform(get("/api/reviews/media/{mediaId}/helpful", testMedia.getId())
                    .header("Authorization", "Bearer " + authToken)
                    .param("page", "0")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].helpfulVotes").value(1));
        }
    }

    @Nested
    @DisplayName("Authentication and Authorization Tests")
    class AuthTests {

        @Test
        @DisplayName("Should require authentication for all endpoints")
        void shouldRequireAuthenticationForAllEndpoints() throws Exception {
            // Test various endpoints without authentication
            mockMvc.perform(get("/api/reviews/media/{mediaId}", testMedia.getId()))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/reviews")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(put("/api/reviews/{reviewId}", testReview.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(delete("/api/reviews/{reviewId}", testReview.getId()))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/reviews/{reviewId}/vote", testReview.getId()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject invalid JWT tokens")
        void shouldRejectInvalidJwtTokens() throws Exception {
            mockMvc.perform(get("/api/reviews/media/{mediaId}", testMedia.getId())
                    .header("Authorization", "Bearer invalid-token"))
                    .andExpect(status().isUnauthorized());
        }
    }
} 