package com.showsync.service.impl;

import com.showsync.config.RecommendationConfig;
import com.showsync.dto.recommendation.*;
import com.showsync.entity.*;
import com.showsync.entity.recommendation.RecommendationType;
import com.showsync.entity.recommendation.RecommendationReason;
import com.showsync.entity.recommendation.ViewingPersonality;
import com.showsync.entity.recommendation.FeedbackType;
import com.showsync.repository.*;
import com.showsync.service.RecommendationService;
import com.showsync.service.UserPreferenceService;
import com.showsync.service.util.AlgorithmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of RecommendationService with advanced AI recommendation algorithms.
 * Provides collaborative filtering, content-based filtering, and real-time generation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RecommendationServiceImpl implements RecommendationService {

    private final ContentRecommendationRepository contentRecommendationRepository;
    private final GroupRecommendationRepository groupRecommendationRepository;
    private final RecommendationFeedbackRepository feedbackRepository;
    private final UserPreferenceProfileRepository preferenceRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final GroupRepository groupRepository;
    private final UserMediaInteractionRepository interactionRepository;
    private final ReviewRepository reviewRepository;
    private final GroupMembershipRepository membershipRepository;
    
    private final UserPreferenceService userPreferenceService;
    private final AlgorithmUtils algorithmUtils;
    private final RecommendationConfig config;

    // === PERSONAL CONTENT RECOMMENDATIONS ===

    @Override
    @Transactional(readOnly = true)
    public Page<ContentRecommendationResponse> getPersonalRecommendations(Long userId, Pageable pageable) {
        log.debug("Getting personal recommendations for user: {}", userId);
        
        Page<ContentRecommendation> recommendations = contentRecommendationRepository
            .findActivePersonalRecommendations(userId, LocalDateTime.now(), pageable);
        
        return recommendations.map(this::mapToContentResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ContentRecommendationResponse> getRealTimeRecommendations(Long userId, Long contextMediaId, int limit) {
        log.debug("Generating real-time recommendations for user: {} with context: {}", userId, contextMediaId);
        
        // Check if user has sufficient data for personalized recommendations
        if (!userPreferenceService.hasSufficientData(userId)) {
            return getTrendingRecommendations(userId, limit);
        }
        
        UserPreferenceProfile userProfile = userPreferenceService.getOrCreateUserProfile(userId);
        List<ContentRecommendationResponse> recommendations = new ArrayList<>();
        
        // Strategy 1: Content-based filtering (if context media provided)
        if (contextMediaId != null) {
            recommendations.addAll(generateContentBasedRecommendations(userId, contextMediaId, limit / 2));
        }
        
        // Strategy 2: Collaborative filtering
        recommendations.addAll(generateCollaborativeRecommendations(userId, limit - recommendations.size()));
        
        // Strategy 3: Fill with trending if needed
        if (recommendations.size() < limit) {
            recommendations.addAll(getTrendingRecommendations(userId, limit - recommendations.size()));
        }
        
        // Diversify and rank results
        return diversifyAndRankRecommendations(recommendations, userProfile, limit);
    }

    @Override
    @Cacheable(value = "trendingRecommendations", key = "#limit")
    public List<ContentRecommendationResponse> getTrendingRecommendations(Long userId, int limit) {
        log.debug("Getting trending recommendations for user: {}", userId);
        // Use repository signature: (now, minCount, pageable)
        List<ContentRecommendation> trending = contentRecommendationRepository
            .findTrendingRecommendations(
                LocalDateTime.now(),
                3L,
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, limit))
            );

        return trending.stream()
            .map(this::mapToContentResponse)
            .collect(Collectors.toList());
    }

    // === GROUP RECOMMENDATIONS ===

    @Override
    @Transactional(readOnly = true)
    public Page<GroupRecommendationResponse> getGroupRecommendations(Long userId, Pageable pageable) {
        log.debug("Getting group recommendations for user: {}", userId);
        
        Page<GroupRecommendation> recommendations = groupRecommendationRepository
            .findActiveRecommendationsForUser(userId, LocalDateTime.now(), pageable);
        
        return recommendations.map(this::mapToGroupResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ContentRecommendationResponse> getGroupContentRecommendations(Long userId, Long groupId, Pageable pageable) {
        log.debug("Getting group content recommendations for user: {} in group: {}", userId, groupId);
        
        Page<ContentRecommendation> recommendations = contentRecommendationRepository
            .findActiveGroupRecommendations(userId, groupId, LocalDateTime.now(), pageable);
        
        return recommendations.map(this::mapToContentResponse);
    }

    // === RECOMMENDATION MANAGEMENT ===

    @Override
    @Transactional
    public void markRecommendationAsViewed(Long userId, String recommendationType, Long recommendationId) {
        log.debug("Marking recommendation as viewed: {} {} for user: {}", recommendationType, recommendationId, userId);
        
        if ("CONTENT".equalsIgnoreCase(recommendationType)) {
            contentRecommendationRepository.markAsViewed(recommendationId);
        } else if ("GROUP".equalsIgnoreCase(recommendationType)) {
            groupRecommendationRepository.markAsViewed(recommendationId);
        }
    }

    @Override
    @Transactional
    public void dismissRecommendation(Long userId, String recommendationType, Long recommendationId, String reason) {
        log.debug("Dismissing recommendation: {} {} for user: {} with reason: {}", 
                 recommendationType, recommendationId, userId, reason);
        
        if ("CONTENT".equalsIgnoreCase(recommendationType)) {
            contentRecommendationRepository.markAsDismissed(recommendationId);
        } else if ("GROUP".equalsIgnoreCase(recommendationType)) {
            groupRecommendationRepository.markAsDismissed(recommendationId);
        }
        
        // Record negative feedback
        recordFeedback(userId, recommendationType, recommendationId, FeedbackType.NEGATIVE, reason);
    }

    @Override
    @Transactional
    public void recordPositiveFeedback(Long userId, String recommendationType, Long recommendationId, String actionTaken) {
        log.debug("Recording positive feedback for recommendation: {} {} user: {} action: {}", 
                 recommendationType, recommendationId, userId, actionTaken);
        
        // Record positive feedback
        recordFeedback(userId, recommendationType, recommendationId, FeedbackType.POSITIVE, actionTaken);
        
        // Update recommendation status based on action
        if ("CONTENT".equalsIgnoreCase(recommendationType) && "ADDED_TO_LIBRARY".equals(actionTaken)) {
            contentRecommendationRepository.markAsAddedToLibrary(recommendationId);
        } else if ("GROUP".equalsIgnoreCase(recommendationType) && "JOINED_GROUP".equals(actionTaken)) {
            groupRecommendationRepository.markAsJoined(recommendationId);
        }
    }

    @Override
    @Transactional
    public void submitFeedback(Long userId, String recommendationType, Long recommendationId, 
                              int rating, String feedbackText) {
        log.debug("Submitting explicit feedback for recommendation: {} {} user: {} rating: {}", 
                 recommendationType, recommendationId, userId, rating);
        
        FeedbackType feedbackType = FeedbackType.fromRating(rating);
        String combinedFeedback = feedbackText != null ? 
            String.format("Rating: %d, Comment: %s", rating, feedbackText) : 
            String.format("Rating: %d", rating);
        
        recordFeedback(userId, recommendationType, recommendationId, feedbackType, combinedFeedback);
    }

    // === BATCH RECOMMENDATION GENERATION ===

    @Override
    @Async
    @Transactional
    public int generateRecommendationsForUser(Long userId) {
        log.info("Generating recommendations for user: {}", userId);
        
        try {
            // Clean up old recommendations first
            cleanupExpiredRecommendations(userId);
            
            int generated = 0;
            
            // Generate content recommendations
            generated += generateContentRecommendationsForUser(userId);
            
            // Generate group recommendations
            generated += generateGroupRecommendationsForUser(userId);
            
            log.info("Generated {} recommendations for user: {}", generated, userId);
            return generated;
            
        } catch (Exception e) {
            log.error("Failed to generate recommendations for user: {}", userId, e);
            return 0;
        }
    }

    @Override
    @Transactional
    public RecommendationGenerationSummary generateRecommendationsForAllUsers() {
        log.info("Starting batch recommendation generation for all users");
        
        LocalDateTime startTime = LocalDateTime.now();
        int totalUsers = 0;
        int successfulUsers = 0;
        int totalRecommendations = 0;
        Map<String, Integer> errorCounts = new HashMap<>();
        
        // Get all active users (simplified - in real implementation would be paginated)
        List<User> activeUsers = userRepository.findAll().stream()
            .filter(user -> userPreferenceService.hasSufficientData(user.getId()))
            .collect(Collectors.toList());
        
        totalUsers = activeUsers.size();
        
        for (User user : activeUsers) {
            try {
                int generated = generateRecommendationsForUser(user.getId());
                totalRecommendations += generated;
                successfulUsers++;
                
                if (successfulUsers % 100 == 0) {
                    log.info("Processed {} of {} users", successfulUsers, totalUsers);
                }
                
            } catch (Exception e) {
                String errorType = e.getClass().getSimpleName();
                errorCounts.merge(errorType, 1, Integer::sum);
                log.error("Failed to generate recommendations for user: {}", user.getId(), e);
            }
        }
        
        RecommendationGenerationSummary summary = new RecommendationGenerationSummary();
        summary.setTotalUsersProcessed(totalUsers);
        summary.setSuccessfulUsers(successfulUsers);
        summary.setFailedUsers(totalUsers - successfulUsers);
        summary.setTotalRecommendationsGenerated(totalRecommendations);
        summary.setErrorCounts(errorCounts);
        summary.markCompleted();
        
        log.info("Completed batch generation: {} users, {} recommendations", 
                successfulUsers, totalRecommendations);
        
        return summary;
    }

    @Override
    @Transactional
    public int refreshRecommendationsForActiveUsers(int hoursBack) {
        log.info("Refreshing recommendations for users active in last {} hours", hoursBack);
        
        LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hoursBack);
        
        // Find users with recent activity (simplified query)
        List<Long> activeUserIds = interactionRepository.findAll().stream()
            .filter(interaction -> interaction.getUpdatedAt().isAfter(cutoffTime))
            .map(interaction -> interaction.getUser().getId())
            .distinct()
            .collect(Collectors.toList());
        
        int refreshed = 0;
        for (Long userId : activeUserIds) {
            try {
                generateRecommendationsForUser(userId);
                refreshed++;
            } catch (Exception e) {
                log.error("Failed to refresh recommendations for user: {}", userId, e);
            }
        }
        
        log.info("Refreshed recommendations for {} active users", refreshed);
        return refreshed;
    }

    // === ANALYTICS & INSIGHTS ===

    @Override
    @Cacheable(value = "recommendationAnalytics", key = "#days")
    public RecommendationAnalytics getRecommendationAnalytics(int days) {
        log.debug("Generating recommendation analytics for {} days", days);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(days);
        
        RecommendationAnalytics analytics = new RecommendationAnalytics(
            cutoffDate, LocalDateTime.now()
        );
        // Leave defaults; caller can compute rates via provided helpers
        return analytics;
    }

    @Override
    @Cacheable(value = "userRecommendationInsights", key = "#userId")
    public UserRecommendationInsights getUserRecommendationInsights(Long userId) {
        log.debug("Generating recommendation insights for user: {}", userId);
        
        UserRecommendationInsights insights = new UserRecommendationInsights();
        insights.setUserId(userId);
        // Basic zeros; detailed analytics require additional queries
        UserPreferenceProfile profile = userPreferenceService.getOrCreateUserProfile(userId);
        insights.setConfidenceScore(profile.getConfidenceScore().doubleValue());
        insights.setViewingPersonality(profile.getViewingPersonality());
        return insights;
    }

    @Override
    public RecommendationSummary getRecommendationSummary(Long userId) {
        log.debug("Getting recommendation summary for user: {}", userId);
        
        RecommendationSummary summary = new RecommendationSummary();
        summary.setUserId(userId);
        // Populate minimal viable fields; advanced stats require extra queries
        summary.setTotalActiveRecommendations(0);
        summary.setUnviewedRecommendations(0);
        return summary;
    }

    // === PREFERENCE MANAGEMENT ===

    @Override
    @Transactional
    public double updateUserPreferences(Long userId) {
        return userPreferenceService.updateUserPreferences(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getUserPreferences(Long userId) {
        return userPreferenceService.getUserPreferenceResponse(userId);
    }

    @Override
    @Cacheable(value = "userCompatibility", key = "#userId1 + '_' + #userId2")
    public double calculateUserCompatibility(Long userId1, Long userId2) {
        return userPreferenceService.calculateUserCompatibility(userId1, userId2);
    }

    // === RECOMMENDATION DISCOVERY ===

    @Override
    public List<ContentRecommendationResponse> findSimilarContent(Long userId, Long mediaId, int limit) {
        log.debug("Finding similar content to media: {} for user: {}", mediaId, userId);
        
        return generateContentBasedRecommendations(userId, mediaId, limit);
    }

    @Override
    public List<ContentRecommendationResponse> getRecommendationsByType(Long userId, RecommendationType type, int limit) {
        log.debug("Getting recommendations by type: {} for user: {}", type, userId);
        
        List<ContentRecommendation> recommendations = contentRecommendationRepository
            .findTopRecommendationsByType(
                userId,
                type,
                LocalDateTime.now(),
                BigDecimal.valueOf(config.getMinRelevanceScore()),
                org.springframework.data.domain.PageRequest.of(0, Math.max(1, limit))
            );
        
        return recommendations.stream()
            .map(this::mapToContentResponse)
            .collect(Collectors.toList());
    }

    // === PRIVATE ALGORITHM IMPLEMENTATIONS ===

    private List<ContentRecommendationResponse> generateContentBasedRecommendations(Long userId, Long contextMediaId, int limit) {
        log.debug("Generating content-based recommendations for user: {} based on media: {}", userId, contextMediaId);
        
        Optional<Media> contextMedia = mediaRepository.findById(contextMediaId);
        if (contextMedia.isEmpty()) {
            return new ArrayList<>();
        }
        
        Media baseMedia = contextMedia.get();
        List<ContentRecommendationResponse> recommendations = new ArrayList<>();
        
        // Find similar media based on content features
        List<Media> similarMedia = findSimilarMediaByContent(baseMedia, limit * 2);
        
        UserPreferenceProfile userProfile = userPreferenceService.getOrCreateUserProfile(userId);
        
        for (Media media : similarMedia) {
            if (recommendations.size() >= limit) break;
            
            // Calculate relevance score based on user preferences
            double relevanceScore = calculateContentRelevanceScore(media, userProfile);
            
            if (relevanceScore >= config.getMinRelevanceScore()) {
                ContentRecommendationResponse response = createContentRecommendationResponse(
                    userId, media, RecommendationType.CONTENT_BASED, 
                    RecommendationReason.SIMILAR_CONTENT, relevanceScore,
                    String.format("Similar to %s", baseMedia.getTitle())
                );
                recommendations.add(response);
            }
        }
        
        return recommendations;
    }

    private List<ContentRecommendationResponse> generateCollaborativeRecommendations(Long userId, int limit) {
        log.debug("Generating collaborative filtering recommendations for user: {}", userId);
        
        List<ContentRecommendationResponse> recommendations = new ArrayList<>();
        
        // Find similar users
        List<UserPreferenceService.UserCompatibility> similarUsers = 
            userPreferenceService.findSimilarUsers(userId, config.getCollaborativeFilteringUserCount());
        
        if (similarUsers.isEmpty()) {
            return recommendations;
        }
        
        // Get highly rated media from similar users
        Map<Long, Double> mediaScores = new HashMap<>();
        
        for (UserPreferenceService.UserCompatibility similarUser : similarUsers) {
            var page = org.springframework.data.domain.PageRequest.of(0, 50);
            List<Review> highRatedReviews = reviewRepository
                .findByUserIdOrderByCreatedAtDesc(similarUser.userId(), page)
                .getContent()
                .stream()
                .filter(r -> r.getRating() != null && r.getRating() >= 4)
                .collect(Collectors.toList());
            
            for (Review review : highRatedReviews) {
                double score = review.getRating() * similarUser.compatibilityScore();
                mediaScores.merge(review.getMedia().getId(), score, Double::sum);
            }
        }
        
        // Filter out media user has already seen and rank by score
        List<Long> userMediaIds = getUserMediaIds(userId);
        
        mediaScores.entrySet().stream()
            .filter(entry -> !userMediaIds.contains(entry.getKey()))
            .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
            .limit(limit)
            .forEach(entry -> {
                Media media = mediaRepository.findById(entry.getKey()).orElse(null);
                if (media != null) {
                    ContentRecommendationResponse response = createContentRecommendationResponse(
                        userId, media, RecommendationType.COLLABORATIVE,
                        RecommendationReason.SIMILAR_USERS, entry.getValue(),
                        "Users with similar taste enjoyed this"
                    );
                    recommendations.add(response);
                }
            });
        
        return recommendations;
    }

    private List<ContentRecommendationResponse> diversifyAndRankRecommendations(
            List<ContentRecommendationResponse> recommendations, 
            UserPreferenceProfile userProfile, 
            int limit) {
        
        // Simple rank by relevance score when genres are unavailable
        return recommendations.stream()
            .sorted((a, b) -> b.getRelevanceScore().compareTo(a.getRelevanceScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private double calculateContentRelevanceScore(Media media, UserPreferenceProfile userProfile) {
        double score = 0.0;
        
        // Genre data not available in Media entity; skip genre component
        
        // Platform preference (if available)
        // score += platformAlignment * config.getPlatformWeight();
        
        // Era preference using releaseDate if available
        if (media.getReleaseDate() != null) {
            int year = media.getReleaseDate().getYear();
            String era = determineEra(year);
            Map<String, Double> eraPrefs = userProfile.getEraPreferences();
            score += eraPrefs.getOrDefault(era, 0.0) * config.getEraWeight();
        }
        
        // Rating alignment
        if (media.getAverageRating() != null) {
            double ratingAlignment = Math.abs(media.getAverageRating() - userProfile.getAverageUserRating().doubleValue()) / 5.0;
            score += (1.0 - ratingAlignment) * config.getRatingWeight();
        }
        
        return Math.min(1.0, score);
    }

    private List<Media> findSimilarMediaByContent(Media baseMedia, int limit) {
        // Simplified content-based similarity
        return mediaRepository.findAll().stream()
            .filter(media -> !media.getId().equals(baseMedia.getId()))
            .filter(media -> Objects.equals(media.getType(), baseMedia.getType()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private String determineEra(int releaseYear) {
        if (releaseYear >= 2020) return "2020s";
        if (releaseYear >= 2010) return "2010s";
        if (releaseYear >= 2000) return "2000s";
        if (releaseYear >= 1990) return "1990s";
        if (releaseYear >= 1980) return "1980s";
        if (releaseYear >= 1970) return "1970s";
        return "Classic";
    }

    private List<Long> getUserMediaIds(Long userId) {
        return interactionRepository.findByUserId(userId).stream()
            .map(interaction -> interaction.getMedia().getId())
            .collect(Collectors.toList());
    }

    // === CONTENT RECOMMENDATION GENERATION ===

    private int generateContentRecommendationsForUser(Long userId) {
        log.debug("Generating content recommendations for user: {}", userId);
        
        int generated = 0;
        UserPreferenceProfile profile = userPreferenceService.getOrCreateUserProfile(userId);
        
        // Personal recommendations based on preferences
        List<ContentRecommendationResponse> personalRecs = generatePersonalRecommendations(userId, 10);
        generated += saveContentRecommendations(personalRecs, userId);
        
        // Collaborative filtering recommendations
        List<ContentRecommendationResponse> collaborativeRecs = generateCollaborativeRecommendations(userId, 5);
        generated += saveContentRecommendations(collaborativeRecs, userId);
        
        // Trending recommendations
        List<ContentRecommendationResponse> trendingRecs = getTrendingRecommendations(userId, 5);
        generated += saveContentRecommendations(trendingRecs, userId);
        
        return generated;
    }

    private List<ContentRecommendationResponse> generatePersonalRecommendations(Long userId, int limit) {
        UserPreferenceProfile profile = userPreferenceService.getOrCreateUserProfile(userId);
        List<ContentRecommendationResponse> recommendations = new ArrayList<>();
        
        // Get user's top genres
        // Without genre field, approximate by top-rated media of same type
        mediaRepository.findAll().stream()
            .sorted((a, b) -> Double.compare(
                b.getAverageRating() != null ? b.getAverageRating() : 0.0,
                a.getAverageRating() != null ? a.getAverageRating() : 0.0))
            .forEach(media -> {
                if (recommendations.size() >= limit) return;
                double relevanceScore = calculateContentRelevanceScore(media, profile);
                if (relevanceScore >= config.getMinRelevanceScore()) {
                    ContentRecommendationResponse response = createContentRecommendationResponse(
                        userId, media, RecommendationType.PERSONAL,
                        RecommendationReason.GENRE_MATCH, relevanceScore,
                        "Recommended based on your preferences"
                    );
                    recommendations.add(response);
                }
            });
        
        return recommendations;
    }

    private int generateGroupRecommendationsForUser(Long userId) {
        log.debug("Generating group recommendations for user: {}", userId);
        
        int generated = 0;
        UserPreferenceProfile userProfile = userPreferenceService.getOrCreateUserProfile(userId);
        
        // Find potential groups based on user's preferences and similar users
        List<Group> candidateGroups = findCandidateGroups(userId);
        
        for (Group group : candidateGroups) {
            if (generated >= 10) break; // Limit group recommendations
            
            double compatibilityScore = calculateGroupCompatibility(userId, group.getId());
            
            if (compatibilityScore >= config.getMinSimilarityScore()) {
                GroupRecommendation recommendation = new GroupRecommendation();
                recommendation.setUser(userRepository.findById(userId).orElse(null));
                recommendation.setRecommendedGroup(group);
                recommendation.setCompatibilityScore(BigDecimal.valueOf(compatibilityScore));
                recommendation.setReasonCode(RecommendationReason.SIMILAR_CONTENT);
                recommendation.setExplanation("Based on your viewing preferences and similar users");
                recommendation.setCreatedAt(LocalDateTime.now());
                recommendation.setExpiresAt(LocalDateTime.now().plus(config.getGroupRecommendationExpiry()));
                
                groupRecommendationRepository.save(recommendation);
                generated++;
            }
        }
        
        return generated;
    }

    private List<Group> findCandidateGroups(Long userId) {
        // Find public groups that user hasn't joined
        return groupRepository.findAll().stream()
            .filter(group -> group.getPrivacySetting() == Group.PrivacySetting.PUBLIC && group.isActive())
            .filter(group -> !isUserInGroup(userId, group.getId()))
            .collect(Collectors.toList());
    }

    private boolean isUserInGroup(Long userId, Long groupId) {
        User user = userRepository.findById(userId).orElse(null);
        Group group = groupRepository.findById(groupId).orElse(null);
        return user != null && group != null && 
               membershipRepository.findByUserAndGroup(user, group).isPresent();
    }

    private double calculateGroupCompatibility(Long userId, Long groupId) {
        // Simplified group compatibility based on member preferences
        Group group = groupRepository.findById(groupId).orElse(null);
        if (group == null) return 0.0;
        List<User> groupMembers = group.getMemberships().stream()
            .filter(m -> m.getStatus() == com.showsync.entity.GroupMembership.MembershipStatus.ACTIVE)
            .map(com.showsync.entity.GroupMembership::getUser)
            .collect(Collectors.toList());
        
        if (groupMembers.isEmpty()) return 0.0;
        
        double totalCompatibility = 0.0;
        int compatibleMembers = 0;
        
        for (User member : groupMembers) {
            double compatibility = userPreferenceService.calculateUserCompatibility(userId, member.getId());
            if (compatibility > config.getMinSimilarityScore()) {
                totalCompatibility += compatibility;
                compatibleMembers++;
            }
        }
        
        return compatibleMembers > 0 ? totalCompatibility / compatibleMembers : 0.0;
    }

    // === HELPER METHODS ===

    private void cleanupExpiredRecommendations(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        contentRecommendationRepository.deleteExpiredRecommendations(now);
        groupRecommendationRepository.deleteExpiredRecommendations(now);
    }

    private ContentRecommendationResponse createContentRecommendationResponse(
            Long userId, Media media, RecommendationType type, RecommendationReason reason,
            double relevanceScore, String explanation) {
        
        ContentRecommendationResponse response = new ContentRecommendationResponse();
        response.setMediaId(media.getId());
        response.setMediaTitle(media.getTitle());
        response.setMediaType(media.getType() != null ? media.getType().name() : null);
        response.setMediaPoster(media.getPosterUrl());
        response.setMediaOverview(media.getDescription());
        response.setMediaRating(media.getAverageRating());
        if (media.getReleaseDate() != null) {
            response.setMediaYear(media.getReleaseDate().getYear());
        }
        response.setRecommendationType(type);
        response.setReasonCode(reason);
        response.setRelevanceScore(BigDecimal.valueOf(relevanceScore));
        response.setExplanation(explanation);
        response.setCreatedAt(LocalDateTime.now());
        
        return response;
    }

    private int saveContentRecommendations(List<ContentRecommendationResponse> responses, Long userId) {
        int saved = 0;
        User user = userRepository.findById(userId).orElse(null);
        
        for (ContentRecommendationResponse response : responses) {
            Media media = mediaRepository.findById(response.getMediaId()).orElse(null);
            if (user != null && media != null) {
                ContentRecommendation entity = new ContentRecommendation();
                entity.setUser(user);
                entity.setRecommendedMedia(media);
                entity.setRelevanceScore(response.getRelevanceScore());
                entity.setRecommendationType(response.getRecommendationType());
                entity.setReasonCode(response.getReasonCode());
                entity.setExplanation(response.getExplanation());
                entity.setCreatedAt(LocalDateTime.now());
                entity.setExpiresAt(LocalDateTime.now().plus(config.getContentRecommendationExpiry()));
                
                contentRecommendationRepository.save(entity);
                saved++;
            }
        }
        
        return saved;
    }

    private void recordFeedback(Long userId, String recommendationType, Long recommendationId, 
                               FeedbackType feedbackType, String feedbackText) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            RecommendationFeedback feedback = new RecommendationFeedback();
            feedback.setUser(user);
            feedback.setRecommendationType(recommendationType);
            feedback.setRecommendationId(recommendationId);
            feedback.setFeedbackType(feedbackType);
            feedback.setFeedbackText(feedbackText);
            feedback.setCreatedAt(LocalDateTime.now());
            
            feedbackRepository.save(feedback);
            
            // Trigger preference update based on feedback
            if (feedbackType == FeedbackType.POSITIVE || feedbackType == FeedbackType.NEGATIVE) {
                userPreferenceService.updateUserPreferences(userId);
            }
        }
    }

    private ContentRecommendationResponse mapToContentResponse(ContentRecommendation recommendation) {
        ContentRecommendationResponse response = new ContentRecommendationResponse();
        response.setRecommendationId(recommendation.getId());
        if (recommendation.getRecommendedMedia() != null) {
            response.setMediaId(recommendation.getRecommendedMedia().getId());
            response.setMediaTitle(recommendation.getRecommendedMedia().getTitle());
            response.setMediaType(recommendation.getRecommendedMedia().getType() != null ? recommendation.getRecommendedMedia().getType().name() : null);
            response.setMediaPoster(recommendation.getRecommendedMedia().getPosterUrl());
            response.setMediaOverview(recommendation.getRecommendedMedia().getDescription());
            response.setMediaRating(recommendation.getRecommendedMedia().getAverageRating());
            if (recommendation.getRecommendedMedia().getReleaseDate() != null) {
                response.setMediaYear(recommendation.getRecommendedMedia().getReleaseDate().getYear());
            }
        }
        response.setRecommendationType(recommendation.getRecommendationType());
        response.setReasonCode(recommendation.getReasonCode());
        response.setRelevanceScore(recommendation.getRelevanceScore());
        response.setExplanation(recommendation.getExplanation());
        response.setViewed(recommendation.isViewed());
        response.setDismissed(recommendation.isDismissed());
        response.setAddedToLibrary(recommendation.isAddedToLibrary());
        response.setCreatedAt(recommendation.getCreatedAt());
        response.setExpiresAt(recommendation.getExpiresAt());
        
        return response;
    }

    private GroupRecommendationResponse mapToGroupResponse(GroupRecommendation recommendation) {
        GroupRecommendationResponse response = new GroupRecommendationResponse();
        response.setRecommendationId(recommendation.getId());
        if (recommendation.getRecommendedGroup() != null) {
            response.setGroupId(recommendation.getRecommendedGroup().getId());
            response.setGroupName(recommendation.getRecommendedGroup().getName());
            response.setGroupDescription(recommendation.getRecommendedGroup().getDescription());
            response.setPublic(recommendation.getRecommendedGroup().getPrivacySetting() == Group.PrivacySetting.PUBLIC);
        }
        response.setCompatibilityScore(recommendation.getCompatibilityScore());
        response.setReasonCode(recommendation.getReasonCode());
        response.setExplanation(recommendation.getExplanation());
        response.setViewed(recommendation.isViewed());
        response.setDismissed(recommendation.isDismissed());
        response.setJoined(recommendation.isJoined());
        response.setCreatedAt(recommendation.getCreatedAt());
        response.setExpiresAt(recommendation.getExpiresAt());
        
        return response;
    }
} 