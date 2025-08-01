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
    @Cacheable(value = "trendingRecommendations", key = "#userId + '_' + #limit")
    public List<ContentRecommendationResponse> getTrendingRecommendations(Long userId, int limit) {
        log.debug("Getting trending recommendations for user: {}", userId);
        
        List<ContentRecommendation> trending = contentRecommendationRepository
            .findTrendingRecommendations(userId, LocalDateTime.now().minusDays(7), limit);
        
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
            .findActiveRecommendationsByUserId(userId, LocalDateTime.now(), pageable);
        
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
            contentRecommendationRepository.markAsViewed(recommendationId, userId, LocalDateTime.now());
        } else if ("GROUP".equalsIgnoreCase(recommendationType)) {
            groupRecommendationRepository.markAsViewed(recommendationId, userId, LocalDateTime.now());
        }
    }

    @Override
    @Transactional
    public void dismissRecommendation(Long userId, String recommendationType, Long recommendationId, String reason) {
        log.debug("Dismissing recommendation: {} {} for user: {} with reason: {}", 
                 recommendationType, recommendationId, userId, reason);
        
        if ("CONTENT".equalsIgnoreCase(recommendationType)) {
            contentRecommendationRepository.markAsDismissed(recommendationId, userId, LocalDateTime.now());
        } else if ("GROUP".equalsIgnoreCase(recommendationType)) {
            groupRecommendationRepository.markAsDismissed(recommendationId, userId, LocalDateTime.now());
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
            contentRecommendationRepository.markAsAddedToLibrary(recommendationId, userId, LocalDateTime.now());
        } else if ("GROUP".equalsIgnoreCase(recommendationType) && "JOINED_GROUP".equals(actionTaken)) {
            groupRecommendationRepository.markAsJoined(recommendationId, userId, LocalDateTime.now());
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
        
        Duration processingTime = Duration.between(startTime, LocalDateTime.now());
        
        RecommendationGenerationSummary summary = new RecommendationGenerationSummary();
        summary.setTotalUsersProcessed(totalUsers);
        summary.setSuccessfulUsers(successfulUsers);
        summary.setFailedUsers(totalUsers - successfulUsers);
        summary.setTotalRecommendationsGenerated(totalRecommendations);
        summary.setProcessingTimeMinutes(processingTime.toMinutes());
        summary.setErrorBreakdown(errorCounts);
        summary.setGeneratedAt(LocalDateTime.now());
        
        log.info("Completed batch generation: {} users, {} recommendations in {} minutes", 
                successfulUsers, totalRecommendations, processingTime.toMinutes());
        
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
        
        RecommendationAnalytics analytics = new RecommendationAnalytics();
        analytics.setAnalysisPeriodDays(days);
        analytics.setGeneratedAt(LocalDateTime.now());
        
        // Overall metrics
        analytics.setTotalRecommendationsGenerated(
            contentRecommendationRepository.countByCreatedAtAfter(cutoffDate) +
            groupRecommendationRepository.countByCreatedAtAfter(cutoffDate)
        );
        
        analytics.setTotalRecommendationsViewed(
            contentRecommendationRepository.countViewedAfter(cutoffDate) +
            groupRecommendationRepository.countViewedAfter(cutoffDate)
        );
        
        // Calculate click-through rate
        double viewRate = analytics.getTotalRecommendationsGenerated() > 0 ? 
            (double) analytics.getTotalRecommendationsViewed() / analytics.getTotalRecommendationsGenerated() : 0.0;
        analytics.setOverallClickThroughRate(viewRate);
        
        // Content vs Group breakdown
        analytics.setContentRecommendationsGenerated(
            contentRecommendationRepository.countByCreatedAtAfter(cutoffDate)
        );
        analytics.setGroupRecommendationsGenerated(
            groupRecommendationRepository.countByCreatedAtAfter(cutoffDate)
        );
        
        return analytics;
    }

    @Override
    @Cacheable(value = "userRecommendationInsights", key = "#userId")
    public UserRecommendationInsights getUserRecommendationInsights(Long userId) {
        log.debug("Generating recommendation insights for user: {}", userId);
        
        UserRecommendationInsights insights = new UserRecommendationInsights();
        insights.setUserId(userId);
        insights.setGeneratedAt(LocalDateTime.now());
        
        // Get user's recommendation statistics
        int totalReceived = contentRecommendationRepository.countByUserId(userId);
        int totalViewed = contentRecommendationRepository.countViewedByUserId(userId);
        int totalActedOn = contentRecommendationRepository.countActionedByUserId(userId);
        
        insights.setTotalRecommendationsReceived(totalReceived);
        insights.setTotalRecommendationsViewed(totalViewed);
        insights.setTotalRecommendationsActedOn(totalActedOn);
        
        // Calculate engagement rate
        double engagementRate = totalReceived > 0 ? (double) totalViewed / totalReceived : 0.0;
        insights.setEngagementRate(engagementRate);
        
        // Get preference profile
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
        summary.setGeneratedAt(LocalDateTime.now());
        
        // Count unviewed recommendations
        int unviewedContent = contentRecommendationRepository.countUnviewedByUserId(userId);
        int unviewedGroups = groupRecommendationRepository.countUnviewedByUserId(userId);
        
        summary.setUnviewedContentRecommendations(unviewedContent);
        summary.setUnviewedGroupRecommendations(unviewedGroups);
        summary.setTotalUnviewed(unviewedContent + unviewedGroups);
        
        // Quality indicator
        double confidence = userPreferenceService.calculateConfidenceScore(userId);
        summary.setRecommendationQuality(confidence > 0.7 ? "High" : confidence > 0.4 ? "Medium" : "Building");
        
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
            .findByUserIdAndTypeAndExpiresAtAfter(userId, type, LocalDateTime.now())
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
        
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
            List<Review> highRatedReviews = reviewRepository
                .findByUserIdAndRatingGreaterThan(similarUser.userId(), 4);
            
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
        
        // Apply diversity filter to avoid too many similar recommendations
        Map<String, Integer> genreCount = new HashMap<>();
        List<ContentRecommendationResponse> diversified = new ArrayList<>();
        
        for (ContentRecommendationResponse rec : recommendations) {
            if (diversified.size() >= limit) break;
            
            String genre = rec.getMediaGenre();
            int currentCount = genreCount.getOrDefault(genre, 0);
            
            if (currentCount < config.getMaxSameTypeRecommendations()) {
                diversified.add(rec);
                genreCount.put(genre, currentCount + 1);
            }
        }
        
        // Rank by relevance score and user preference alignment
        return diversified.stream()
            .sorted((a, b) -> Double.compare(b.getRelevanceScore(), a.getRelevanceScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    private double calculateContentRelevanceScore(Media media, UserPreferenceProfile userProfile) {
        double score = 0.0;
        
        // Genre preference alignment
        if (media.getGenre() != null) {
            Map<String, Double> genrePrefs = userProfile.getGenrePreferences();
            score += genrePrefs.getOrDefault(media.getGenre(), 0.0) * config.getGenreWeight();
        }
        
        // Platform preference (if available)
        // score += platformAlignment * config.getPlatformWeight();
        
        // Era preference
        if (media.getReleaseYear() != null) {
            String era = determineEra(media.getReleaseYear());
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
            .filter(media -> Objects.equals(media.getGenre(), baseMedia.getGenre()) ||
                           Objects.equals(media.getType(), baseMedia.getType()))
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
        Map<String, Double> genrePrefs = profile.getGenrePreferences();
        
        for (Map.Entry<String, Double> genreEntry : genrePrefs.entrySet()) {
            if (recommendations.size() >= limit) break;
            
            String genre = genreEntry.getKey();
            double preference = genreEntry.getValue();
            
            if (preference > 0.5) { // Only include high-preference genres
                List<Media> genreMedia = mediaRepository.findByGenre(genre);
                
                for (Media media : genreMedia) {
                    if (recommendations.size() >= limit) break;
                    
                    double relevanceScore = calculateContentRelevanceScore(media, profile);
                    if (relevanceScore >= config.getMinRelevanceScore()) {
                        ContentRecommendationResponse response = createContentRecommendationResponse(
                            userId, media, RecommendationType.PERSONAL,
                            RecommendationReason.GENRE_MATCH, relevanceScore,
                            String.format("Based on your love for %s", genre)
                        );
                        recommendations.add(response);
                    }
                }
            }
        }
        
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
                recommendation.setGroup(group);
                recommendation.setCompatibilityScore(BigDecimal.valueOf(compatibilityScore));
                recommendation.setReason(RecommendationReason.SIMILAR_INTERESTS);
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
            .filter(group -> group.getIsPublic())
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
        List<User> groupMembers = membershipRepository.findByGroupId(groupId).stream()
            .map(membership -> membership.getUser())
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
        contentRecommendationRepository.deleteExpiredRecommendations(userId, now);
        groupRecommendationRepository.deleteExpiredRecommendations(userId, now);
    }

    private ContentRecommendationResponse createContentRecommendationResponse(
            Long userId, Media media, RecommendationType type, RecommendationReason reason,
            double relevanceScore, String explanation) {
        
        ContentRecommendationResponse response = new ContentRecommendationResponse();
        response.setUserId(userId);
        response.setMediaId(media.getId());
        response.setMediaTitle(media.getTitle());
        response.setMediaType(media.getType());
        response.setMediaGenre(media.getGenre());
        response.setRecommendationType(type);
        response.setReason(reason);
        response.setRelevanceScore(relevanceScore);
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
                entity.setMedia(media);
                entity.setRelevanceScore(BigDecimal.valueOf(response.getRelevanceScore()));
                entity.setType(response.getRecommendationType());
                entity.setReason(response.getReason());
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
        response.setUserId(recommendation.getUser().getId());
        response.setMediaId(recommendation.getMedia().getId());
        response.setMediaTitle(recommendation.getMedia().getTitle());
        response.setMediaType(recommendation.getMedia().getType());
        response.setMediaGenre(recommendation.getMedia().getGenre());
        response.setRecommendationType(recommendation.getType());
        response.setReason(recommendation.getReason());
        response.setRelevanceScore(recommendation.getRelevanceScore().doubleValue());
        response.setExplanation(recommendation.getExplanation());
        response.setIsViewed(recommendation.getIsViewed());
        response.setIsDismissed(recommendation.getIsDismissed());
        response.setIsAddedToLibrary(recommendation.getIsAddedToLibrary());
        response.setCreatedAt(recommendation.getCreatedAt());
        response.setExpiresAt(recommendation.getExpiresAt());
        
        return response;
    }

    private GroupRecommendationResponse mapToGroupResponse(GroupRecommendation recommendation) {
        GroupRecommendationResponse response = new GroupRecommendationResponse();
        response.setRecommendationId(recommendation.getId());
        response.setUserId(recommendation.getUser().getId());
        response.setGroupId(recommendation.getGroup().getId());
        response.setGroupName(recommendation.getGroup().getName());
        response.setGroupDescription(recommendation.getGroup().getDescription());
        response.setCompatibilityScore(recommendation.getCompatibilityScore().doubleValue());
        response.setReason(recommendation.getReason());
        response.setExplanation(recommendation.getExplanation());
        response.setIsViewed(recommendation.getIsViewed());
        response.setIsDismissed(recommendation.getIsDismissed());
        response.setIsJoined(recommendation.getIsJoined());
        response.setCreatedAt(recommendation.getCreatedAt());
        response.setExpiresAt(recommendation.getExpiresAt());
        
        return response;
    }
} 