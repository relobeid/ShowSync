package com.showsync.service.impl;

import com.showsync.dto.review.CreateReviewRequest;
import com.showsync.dto.review.MediaDetailsResponse;
import com.showsync.dto.review.ReviewResponse;
import com.showsync.entity.*;
import com.showsync.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReviewServiceImpl Tests")
class ReviewServiceImplTest {

    @Mock
    private ReviewRepository reviewRepository;
    
    @Mock
    private ReviewVoteRepository reviewVoteRepository;
    
    @Mock
    private MediaRepository mediaRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private UserMediaInteractionRepository userMediaInteractionRepository;

    @InjectMocks
    private ReviewServiceImpl reviewService;

    private User testUser;
    private Media testMedia;
    private Review testReview;
    private CreateReviewRequest testRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setDisplayName("Test User");
        testUser.setEmail("test@example.com");

        testMedia = new Media();
        testMedia.setId(1L);
        testMedia.setTitle("Test Movie");
        testMedia.setType(Media.MediaType.MOVIE);

        testReview = new Review();
        testReview.setId(1L);
        testReview.setUser(testUser);
        testReview.setMedia(testMedia);
        testReview.setTitle("Great movie!");
        testReview.setContent("This is a fantastic movie with great acting.");
        testReview.setRating(8);
        testReview.setHelpfulVotes(5);
        testReview.setTotalVotes(7);
        testReview.setSpoiler(false);
        testReview.setModerated(false);
        testReview.setCreatedAt(LocalDateTime.now());
        testReview.setUpdatedAt(LocalDateTime.now());

        testRequest = new CreateReviewRequest();
        testRequest.setMediaId(1L);
        testRequest.setTitle("Great movie!");
        testRequest.setContent("This is a fantastic movie with great acting.");
        testRequest.setRating(8);
        testRequest.setSpoiler(false);
    }

    @Nested
    @DisplayName("Create Review Tests")
    class CreateReviewTests {

        @Test
        @DisplayName("Should create review successfully")
        void shouldCreateReviewSuccessfully() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));
            when(reviewRepository.existsByUserIdAndMediaId(1L, 1L)).thenReturn(false);
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
            // Mock for updateMediaStatistics
            when(mediaRepository.existsById(1L)).thenReturn(true);
            when(reviewRepository.countByMediaIdNotModerated(1L)).thenReturn(1L);
            when(reviewRepository.getAverageRatingByMediaId(1L)).thenReturn(8.0);
            when(reviewRepository.findByMediaIdNotModerated(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testReview)));

            // When
            Review result = reviewService.createReview(1L, testRequest);

            // Then
            assertNotNull(result);
            assertEquals(testReview.getId(), result.getId());
            assertEquals(testReview.getTitle(), result.getTitle());
            assertEquals(testReview.getContent(), result.getContent());
            assertEquals(testReview.getRating(), result.getRating());
            
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.createReview(1L, testRequest));
            
            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when media not found")
        void shouldThrowExceptionWhenMediaNotFound() {
            // Given
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(mediaRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.createReview(1L, testRequest));
            
            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception when user already reviewed media")
        void shouldThrowExceptionWhenUserAlreadyReviewed() {
            // Given
            when(reviewRepository.existsByUserIdAndMediaId(1L, 1L)).thenReturn(true);

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.createReview(1L, testRequest));
            
            verify(reviewRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw exception for invalid user ID")
        void shouldThrowExceptionForInvalidUserId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.createReview(null, testRequest));
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.createReview(0L, testRequest));
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.createReview(-1L, testRequest));
        }
    }

    @Nested
    @DisplayName("Update Review Tests")
    class UpdateReviewTests {

        @Test
        @DisplayName("Should update review successfully")
        void shouldUpdateReviewSuccessfully() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);
            // Mock for updateMediaStatistics
            when(mediaRepository.existsById(1L)).thenReturn(true);
            when(reviewRepository.countByMediaIdNotModerated(1L)).thenReturn(1L);
            when(reviewRepository.getAverageRatingByMediaId(1L)).thenReturn(8.0);
            when(reviewRepository.findByMediaIdNotModerated(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testReview)));

            CreateReviewRequest updateRequest = new CreateReviewRequest();
            updateRequest.setTitle("Updated title");
            updateRequest.setContent("Updated content");
            updateRequest.setRating(9);
            updateRequest.setSpoiler(true);

            // When
            Review result = reviewService.updateReview(1L, 1L, updateRequest);

            // Then
            assertNotNull(result);
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Should throw exception when review not found")
        void shouldThrowExceptionWhenReviewNotFound() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.updateReview(1L, 1L, testRequest));
        }

        @Test
        @DisplayName("Should throw exception when user doesn't own review")
        void shouldThrowExceptionWhenUserDoesntOwnReview() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

            // When & Then
            assertThrows(SecurityException.class, 
                () -> reviewService.updateReview(2L, 1L, testRequest));
        }
    }

    @Nested
    @DisplayName("Delete Review Tests")
    class DeleteReviewTests {

        @Test
        @DisplayName("Should delete review successfully")
        void shouldDeleteReviewSuccessfully() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            // Mock for updateMediaStatistics
            when(mediaRepository.existsById(1L)).thenReturn(true);
            when(reviewRepository.countByMediaIdNotModerated(1L)).thenReturn(0L);
            when(reviewRepository.getAverageRatingByMediaId(1L)).thenReturn(null);
            when(reviewRepository.findByMediaIdNotModerated(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

            // When
            assertDoesNotThrow(() -> reviewService.deleteReview(1L, 1L));

            // Then
            verify(reviewRepository).delete(testReview);
        }

        @Test
        @DisplayName("Should throw exception when review not found")
        void shouldThrowExceptionWhenReviewNotFound() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.deleteReview(1L, 1L));
        }

        @Test
        @DisplayName("Should throw exception when user doesn't own review")
        void shouldThrowExceptionWhenUserDoesntOwnReview() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

            // When & Then
            assertThrows(SecurityException.class, 
                () -> reviewService.deleteReview(2L, 1L));
        }
    }

    @Nested
    @DisplayName("Vote on Review Tests")
    class VoteOnReviewTests {

        @Test
        @DisplayName("Should add helpful vote successfully")
        void shouldAddHelpfulVoteSuccessfully() {
            // Given - Different user voting on testUser's review
            User otherUser = new User();
            otherUser.setId(2L);
            
            Review reviewByOtherUser = new Review();
            reviewByOtherUser.setId(1L);
            reviewByOtherUser.setUser(otherUser); // Review by different user
            
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(reviewByOtherUser));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser)); // Mock the user lookup
            when(reviewVoteRepository.findByUserIdAndReviewId(1L, 1L)).thenReturn(Optional.empty());
            when(reviewVoteRepository.save(any(ReviewVote.class))).thenReturn(new ReviewVote());
            when(reviewRepository.save(any(Review.class))).thenReturn(reviewByOtherUser);

            // When - testUser (ID=1) votes on otherUser's review
            Review result = reviewService.voteOnReview(1L, 1L, true);

            // Then
            assertNotNull(result);
            verify(reviewVoteRepository).save(any(ReviewVote.class));
            verify(reviewRepository).save(any(Review.class));
        }

        @Test
        @DisplayName("Should update existing vote")
        void shouldUpdateExistingVote() {
            // Given - Different user updating their vote
            User otherUser = new User();
            otherUser.setId(2L);
            
            ReviewVote existingVote = new ReviewVote();
            existingVote.setUser(otherUser);
            existingVote.setReview(testReview);
            existingVote.setHelpful(false);

            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));
            when(userRepository.findById(2L)).thenReturn(Optional.of(otherUser)); // Mock the user lookup
            when(reviewVoteRepository.findByUserIdAndReviewId(2L, 1L)).thenReturn(Optional.of(existingVote));
            when(reviewVoteRepository.save(any(ReviewVote.class))).thenReturn(existingVote);
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

            // When
            Review result = reviewService.voteOnReview(2L, 1L, true);

            // Then
            assertNotNull(result);
            verify(reviewVoteRepository).save(existingVote);
            verify(reviewRepository).save(testReview);
        }

        @Test
        @DisplayName("Should throw exception when review not found")
        void shouldThrowExceptionWhenReviewNotFound() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.voteOnReview(1L, 1L, true));
        }

        @Test
        @DisplayName("Should throw exception when user votes on own review")
        void shouldThrowExceptionWhenUserVotesOnOwnReview() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.voteOnReview(1L, 1L, true));
        }
    }

    @Nested
    @DisplayName("Remove Vote Tests")
    class RemoveVoteTests {

        @Test
        @DisplayName("Should remove vote successfully")
        void shouldRemoveVoteSuccessfully() {
            // Given
            ReviewVote existingVote = new ReviewVote();
            existingVote.setUser(testUser);
            existingVote.setReview(testReview);
            existingVote.setHelpful(true);

            when(reviewVoteRepository.findByUserIdAndReviewId(1L, 1L)).thenReturn(Optional.of(existingVote));
            when(reviewRepository.save(any(Review.class))).thenReturn(testReview);

            // When
            Review result = reviewService.removeVoteOnReview(1L, 1L);

            // Then
            assertNotNull(result);
            verify(reviewVoteRepository).delete(existingVote);
            verify(reviewRepository).save(testReview);
        }

        @Test
        @DisplayName("Should throw exception when vote not found")
        void shouldThrowExceptionWhenVoteNotFound() {
            // Given
            when(reviewVoteRepository.findByUserIdAndReviewId(1L, 1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.removeVoteOnReview(1L, 1L));
        }
    }

    @Nested
    @DisplayName("Get Media Details Tests")
    class GetMediaDetailsTests {

        @Test
        @DisplayName("Should get media details successfully")
        void shouldGetMediaDetailsSuccessfully() {
            // Given
            when(mediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));
            when(reviewRepository.countByMediaIdNotModerated(1L)).thenReturn(10L);
            when(reviewRepository.getAverageRatingByMediaId(1L)).thenReturn(8.5);
            when(reviewRepository.findByMediaIdNotModerated(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testReview)));

            // When
            MediaDetailsResponse result = reviewService.getMediaDetails(1L, 1L);

            // Then
            assertNotNull(result);
            assertEquals(testMedia.getId(), result.getId());
            assertEquals(testMedia.getTitle(), result.getTitle());
            assertNotNull(result.getStatistics());
            assertEquals(10L, result.getStatistics().getTotalReviews());
            assertEquals(8.5, result.getStatistics().getAverageRating());
            assertNotNull(result.getRecentReviews());
            assertEquals(1, result.getRecentReviews().size());
        }

        @Test
        @DisplayName("Should throw exception when media not found")
        void shouldThrowExceptionWhenMediaNotFound() {
            // Given
            when(mediaRepository.findById(1L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getMediaDetails(1L, 1L));
        }
    }

    @Nested
    @DisplayName("Get Reviews for Media Tests")
    class GetReviewsForMediaTests {

        @Test
        @DisplayName("Should get reviews for media successfully")
        void shouldGetReviewsForMediaSuccessfully() {
            // Given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview));
            
            when(reviewRepository.findByMediaIdNotModerated(1L, pageable)).thenReturn(reviewPage);

            // When
            Page<ReviewResponse> result = reviewService.getReviewsForMedia(1L, 1L, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(testReview.getId(), result.getContent().get(0).getId());
        }
    }

    @Nested
    @DisplayName("Get Most Helpful Reviews Tests")
    class GetMostHelpfulReviewsTests {

        @Test
        @DisplayName("Should get most helpful reviews successfully")
        void shouldGetMostHelpfulReviewsSuccessfully() {
            // Given
            Pageable pageable = PageRequest.of(0, 5);
            Page<Review> reviewPage = new PageImpl<>(List.of(testReview));
            
            when(reviewRepository.findMostHelpfulReviews(1L, 3, pageable)).thenReturn(reviewPage);

            // When
            Page<ReviewResponse> result = reviewService.getMostHelpfulReviews(1L, 1L, pageable);

            // Then
            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals(testReview.getId(), result.getContent().get(0).getId());
        }
    }

    @Nested
    @DisplayName("Get Reviews by User Tests")
    class GetReviewsByUserTests {

        @Test
        @DisplayName("Should get reviews by user successfully")
        void shouldGetReviewsByUserSuccessfully() {
            // Given
            when(userRepository.existsById(1L)).thenReturn(true);
            when(reviewRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testReview)));

            // When
            Page<ReviewResponse> result = reviewService.getReviewsByUser(1L, 2L, PageRequest.of(0, 10));

            // Then
            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals(testReview.getId(), result.getContent().get(0).getId());
            
            verify(reviewRepository).findByUserIdOrderByCreatedAtDesc(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw exception when user not found")
        void shouldThrowExceptionWhenUserNotFound() {
            // Given
            when(userRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getReviewsByUser(999L, 1L, PageRequest.of(0, 10)));
        }

        @Test
        @DisplayName("Should throw exception for invalid user ID")
        void shouldThrowExceptionForInvalidUserId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getReviewsByUser(null, 1L, PageRequest.of(0, 10)));
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getReviewsByUser(-1L, 1L, PageRequest.of(0, 10)));
        }
    }

    @Nested
    @DisplayName("Get Review by ID Tests")  
    class GetReviewByIdTests {

        @Test
        @DisplayName("Should get review by ID successfully")
        void shouldGetReviewByIdSuccessfully() {
            // Given
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(testReview));

            // When
            ReviewResponse result = reviewService.getReviewById(1L, 2L);

            // Then
            assertNotNull(result);
            assertEquals(testReview.getId(), result.getId());
            assertEquals(testReview.getTitle(), result.getTitle());
            assertEquals(testReview.getContent(), result.getContent());
            
            verify(reviewRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when review not found")
        void shouldThrowExceptionWhenReviewNotFound() {
            // Given
            when(reviewRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getReviewById(999L, 1L));
        }

        @Test
        @DisplayName("Should throw exception when review is moderated")
        void shouldThrowExceptionWhenReviewIsModerated() {
            // Given
            Review moderatedReview = new Review();
            moderatedReview.setId(1L);
            moderatedReview.setModerated(true);
            when(reviewRepository.findById(1L)).thenReturn(Optional.of(moderatedReview));

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getReviewById(1L, 1L));
        }

        @Test
        @DisplayName("Should throw exception for invalid review ID")
        void shouldThrowExceptionForInvalidReviewId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getReviewById(null, 1L));
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.getReviewById(-1L, 1L));
        }
    }

    @Nested
    @DisplayName("Get Trending Media Tests")
    class GetTrendingMediaTests {

        @Test
        @DisplayName("Should get trending media successfully")
        void shouldGetTrendingMediaSuccessfully() {
            // Given
            Object[] mediaStats = new Object[]{1L, 5L, 8.5, 25L}; // mediaId, reviewCount, avgRating, helpfulVotes
            List<Object[]> statsList = new java.util.ArrayList<>();
            statsList.add(mediaStats);
            when(reviewRepository.getMediaStatistics(any(LocalDateTime.class)))
                .thenReturn(statsList);
            when(mediaRepository.existsById(1L)).thenReturn(true);

            // When
            List<MediaDetailsResponse.MediaStatistics> result = 
                reviewService.getTrendingMedia(PageRequest.of(0, 10));

            // Then
            assertNotNull(result);
            assertEquals(1, result.size());
            
            MediaDetailsResponse.MediaStatistics stats = result.get(0);
            assertEquals(5L, stats.getTotalReviews());
            assertEquals(8.5, stats.getAverageRating());
            // Expected trending score: 5 + (8.5 * 2) + (25 * 0.5) = 5 + 17 + 12.5 = 34.5
            assertEquals(34.5, stats.getTrendingScore());
            
            verify(reviewRepository).getMediaStatistics(any(LocalDateTime.class));
        }

        @Test
        @DisplayName("Should filter out non-existent media")
        void shouldFilterOutNonExistentMedia() {
            // Given
            Object[] mediaStats = new Object[]{999L, 3L, 7.0, 10L}; // Non-existent media
            List<Object[]> statsList = new java.util.ArrayList<>();
            statsList.add(mediaStats);
            when(reviewRepository.getMediaStatistics(any(LocalDateTime.class)))
                .thenReturn(statsList);
            when(mediaRepository.existsById(999L)).thenReturn(false);

            // When
            List<MediaDetailsResponse.MediaStatistics> result = 
                reviewService.getTrendingMedia(PageRequest.of(0, 10));

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
        }

        @Test
        @DisplayName("Should handle empty results")
        void shouldHandleEmptyResults() {
            // Given
            when(reviewRepository.getMediaStatistics(any(LocalDateTime.class)))
                .thenReturn(List.of());

            // When
            List<MediaDetailsResponse.MediaStatistics> result = 
                reviewService.getTrendingMedia(PageRequest.of(0, 10));

            // Then
            assertNotNull(result);
            assertEquals(0, result.size());
        }
    }

    @Nested
    @DisplayName("Update Media Statistics Tests")
    class UpdateMediaStatisticsTests {

        @Test
        @DisplayName("Should update media statistics successfully")
        void shouldUpdateMediaStatisticsSuccessfully() {
            // Given
            when(mediaRepository.existsById(1L)).thenReturn(true);
            when(reviewRepository.countByMediaIdNotModerated(1L)).thenReturn(5L);
            when(reviewRepository.getAverageRatingByMediaId(1L)).thenReturn(8.2);
            when(reviewRepository.findByMediaIdNotModerated(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(testReview)));

            // When & Then
            assertDoesNotThrow(() -> reviewService.updateMediaStatistics(1L));
            
            verify(mediaRepository).existsById(1L);
            verify(reviewRepository).countByMediaIdNotModerated(1L);
            verify(reviewRepository).getAverageRatingByMediaId(1L);
            verify(reviewRepository).findByMediaIdNotModerated(eq(1L), any(Pageable.class));
        }

        @Test
        @DisplayName("Should throw exception when media not found")
        void shouldThrowExceptionWhenMediaNotFound() {
            // Given
            when(mediaRepository.existsById(999L)).thenReturn(false);

            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.updateMediaStatistics(999L));
        }

        @Test
        @DisplayName("Should throw exception for invalid media ID")
        void shouldThrowExceptionForInvalidMediaId() {
            // When & Then
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.updateMediaStatistics(null));
            assertThrows(IllegalArgumentException.class, 
                () -> reviewService.updateMediaStatistics(-1L));
        }
    }

    @Nested
    @DisplayName("Utility Method Tests")
    class UtilityMethodTests {

        @Test
        @DisplayName("Should check if user has reviewed media")
        void shouldCheckIfUserHasReviewedMedia() {
            // Given
            when(reviewRepository.existsByUserIdAndMediaId(1L, 1L)).thenReturn(true);

            // When
            boolean result = reviewService.hasUserReviewedMedia(1L, 1L);

            // Then
            assertTrue(result);
        }

        @Test
        @DisplayName("Should get user review for media")
        void shouldGetUserReviewForMedia() {
            // Given
            when(reviewRepository.findByUserIdAndMediaId(1L, 1L)).thenReturn(Optional.of(testReview));

            // When
            Optional<Review> result = reviewService.getUserReviewForMedia(1L, 1L);

            // Then
            assertTrue(result.isPresent());
            assertEquals(testReview.getId(), result.get().getId());
        }
    }
} 