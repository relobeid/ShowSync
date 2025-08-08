package com.showsync.service.impl;

import com.showsync.config.RecommendationConfig;
import com.showsync.dto.recommendation.UserPreferenceResponse;
import com.showsync.entity.*;
import com.showsync.entity.recommendation.ViewingPersonality;
import com.showsync.repository.*;
import com.showsync.service.UserPreferenceService;
import com.showsync.service.util.AlgorithmUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of UserPreferenceService for managing user preference profiles.
 * Provides sophisticated algorithms for taste analysis and compatibility calculation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserPreferenceServiceImpl implements UserPreferenceService {

    private final UserPreferenceProfileRepository preferenceRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final UserMediaInteractionRepository interactionRepository;
    private final ReviewRepository reviewRepository;
    private final AlgorithmUtils algorithmUtils;
    private final RecommendationConfig config;

    // === PROFILE MANAGEMENT ===

    @Override
    @Transactional
    public UserPreferenceProfile getOrCreateUserProfile(Long userId) {
        log.debug("Getting or creating preference profile for user: {}", userId);
        
        return preferenceRepository.findByUserId(userId)
            .orElseGet(() -> createNewUserProfile(userId));
    }

    private UserPreferenceProfile createNewUserProfile(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        
        UserPreferenceProfile profile = new UserPreferenceProfile();
        profile.setUser(user);
        profile.setConfidenceScore(BigDecimal.valueOf(0.0));
        profile.setTotalInteractions(0);
        profile.setTotalCompleted(0);
        profile.setCompletionRate(BigDecimal.valueOf(0.0));
        profile.setAverageUserRating(BigDecimal.valueOf(0.0));
        profile.setRatingVariance(BigDecimal.valueOf(0.0));
        profile.setViewingPersonality(ViewingPersonality.CASUAL);
        
        // Initialize empty preference maps
        profile.setGenrePreferences(new HashMap<>());
        profile.setPlatformPreferences(new HashMap<>());
        profile.setEraPreferences(new HashMap<>());
        
        log.info("Created new preference profile for user: {}", userId);
        return preferenceRepository.save(profile);
    }

    @Override
    @Transactional
    public double updateUserPreferences(Long userId) {
        log.debug("Updating preferences for user: {}", userId);
        
        UserPreferenceProfile profile = getOrCreateUserProfile(userId);
        
        // Calculate all preference dimensions
        Map<String, Double> genrePrefs = calculateGenrePreferences(userId);
        Map<String, Double> platformPrefs = calculatePlatformPreferences(userId);
        Map<String, Double> eraPrefs = calculateEraPreferences(userId);
        
        // Update preference maps
        profile.setGenrePreferences(genrePrefs);
        profile.setPlatformPreferences(platformPrefs);
        profile.setEraPreferences(eraPrefs);
        
        // Calculate viewing personality
        profile.setViewingPersonality(determineViewingPersonality(userId));
        
        // Update interaction statistics
        updateInteractionStatistics(profile, userId);
        
        // Calculate and update confidence score
        double confidenceScore = calculateConfidenceScore(userId);
        profile.setConfidenceScore(BigDecimal.valueOf(confidenceScore));
        
        profile.setUpdatedAt(LocalDateTime.now());
        preferenceRepository.save(profile);
        
        log.info("Updated preference profile for user: {} with confidence: {}", userId, confidenceScore);
        return confidenceScore;
    }

    @Override
    @Transactional
    public double recalculateUserProfile(Long userId) {
        log.info("Force recalculating preference profile for user: {}", userId);
        
        // Delete existing profile to force fresh calculation
        preferenceRepository.findByUserId(userId)
            .ifPresent(preferenceRepository::delete);
        
        return updateUserPreferences(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public UserPreferenceResponse getUserPreferenceResponse(Long userId) {
        UserPreferenceProfile profile = getOrCreateUserProfile(userId);
        
        UserPreferenceResponse response = new UserPreferenceResponse();
        response.setUserId(userId);
        response.setGenrePreferences(profile.getGenrePreferences());
        response.setPlatformPreferences(profile.getPlatformPreferences());
        response.setEraPreferences(profile.getEraPreferences());
        response.setViewingPersonality(profile.getViewingPersonality());
        response.setConfidenceScore(profile.getConfidenceScore().doubleValue());
        response.setTotalInteractions(profile.getTotalInteractions());
        response.setCompletionRate(profile.getCompletionRate().doubleValue());
        response.setAverageUserRating(profile.getAverageUserRating());
        response.setDiversityScore(calculateDiversityScore(userId));
        response.setLastCalculatedAt(profile.getLastCalculatedAt());
        return response;
    }

    // === PREFERENCE CALCULATION ===

    @Override
    @Cacheable(value = "userGenrePreferences", key = "#userId")
    public Map<String, Double> calculateGenrePreferences(Long userId) {
        log.debug("Calculating genre preferences for user: {}", userId);
        
        // Genre data not available via interactions in current schema; return empty map
        return Collections.emptyMap();
    }

    @Override
    @Cacheable(value = "userPlatformPreferences", key = "#userId")
    public Map<String, Double> calculatePlatformPreferences(Long userId) {
        log.debug("Calculating platform preferences for user: {}", userId);
        
        // Platform preferences not tracked; return empty map
        return Collections.emptyMap();
    }

    @Override
    @Cacheable(value = "userEraPreferences", key = "#userId")
    public Map<String, Double> calculateEraPreferences(Long userId) {
        log.debug("Calculating era preferences for user: {}", userId);
        
        List<UserMediaInteraction> interactions = interactionRepository.findByUserId(userId);
        Map<String, Double> eraScores = new HashMap<>();
        for (UserMediaInteraction i : interactions) {
            if (i.getMedia() != null && i.getMedia().getReleaseDate() != null) {
                int year = i.getMedia().getReleaseDate().getYear();
                String era = determineEra(year);
                eraScores.merge(era, 1.0, Double::sum);
            }
        }
        return algorithmUtils.normalizeScores(eraScores);
    }

    @Override
    public ViewingPersonality determineViewingPersonality(Long userId) {
        log.debug("Determining viewing personality for user: {}", userId);
        
        // Get user interaction patterns
        List<UserMediaInteraction> interactions = interactionRepository.findByUserId(userId);
        
        if (interactions.size() < 5) {
            return ViewingPersonality.CASUAL;
        }
        
        // Calculate personality indicators
        long completed = interactions.stream()
            .filter(i -> i.getStatus() == UserMediaInteraction.Status.COMPLETED)
            .count();
        double avgCompletionRate = interactions.isEmpty() ? 0.0 : (double) completed / interactions.size();
        
        double avgSessionLength = 0.0; // not tracked currently
        
        // Check for binge patterns (multiple long sessions)
        long longSessions = 0; // not tracked currently
        
        // Calculate genre diversity
        Set<String> uniqueGenres = new HashSet<>();
        
        double diversityScore = (double) uniqueGenres.size() / Math.max(interactions.size(), 1);
        
        // Determine personality based on patterns
        if (avgCompletionRate > 0.8 && longSessions > interactions.size() * 0.3) {
            return ViewingPersonality.BINGE_WATCHER;
        } else if (diversityScore > 0.7) {
            return ViewingPersonality.EXPLORER;
        } else if (hasHighRatingVariance(userId)) {
            return ViewingPersonality.CRITIC;
        } else {
            return ViewingPersonality.CASUAL;
        }
    }

    // === COMPATIBILITY ANALYSIS ===

    @Override
    @Cacheable(value = "userCompatibility", key = "#userId1 + '_' + #userId2")
    public double calculateUserCompatibility(Long userId1, Long userId2) {
        log.debug("Calculating compatibility between users: {} and {}", userId1, userId2);
        
        UserPreferenceProfile profile1 = getOrCreateUserProfile(userId1);
        UserPreferenceProfile profile2 = getOrCreateUserProfile(userId2);
        
        // Calculate compatibility across different dimensions
        double genreCompatibility = algorithmUtils.cosineSimilarity(
            profile1.getGenrePreferences(), 
            profile2.getGenrePreferences()
        );
        
        double platformCompatibility = algorithmUtils.cosineSimilarity(
            profile1.getPlatformPreferences(), 
            profile2.getPlatformPreferences()
        );
        
        double eraCompatibility = algorithmUtils.cosineSimilarity(
            profile1.getEraPreferences(), 
            profile2.getEraPreferences()
        );
        
        // Check personality compatibility
        double personalityCompatibility = calculatePersonalityCompatibility(
            profile1.getViewingPersonality(), 
            profile2.getViewingPersonality()
        );
        
        // Weight the different compatibility factors
        double overallCompatibility = algorithmUtils.weightedAverage(
            Arrays.asList(genreCompatibility, platformCompatibility, eraCompatibility, personalityCompatibility),
            Arrays.asList(config.getGenreWeight(), config.getPlatformWeight(), config.getEraWeight(), 0.2)
        );
        
        log.debug("Calculated compatibility: {} between users {} and {}", overallCompatibility, userId1, userId2);
        return overallCompatibility;
    }

    @Override
    public List<UserCompatibility> findSimilarUsers(Long userId, int limit) {
        log.debug("Finding similar users for user: {} (limit: {})", userId, limit);
        
        List<UserPreferenceProfile> allProfiles = preferenceRepository.findCandidatesForCollaborativeFiltering(
            userId,
            BigDecimal.valueOf(config.getMinConfidenceThreshold()),
            config.getMinInteractionsForRecommendations()
        );
        
        UserPreferenceProfile userProfile = getOrCreateUserProfile(userId);
        
        return allProfiles.stream()
            .filter(profile -> !profile.getUser().getId().equals(userId))
            .map(profile -> {
                double compatibility = calculateUserCompatibility(userId, profile.getUser().getId());
                return new UserCompatibility(
                    profile.getUser().getId(),
                    profile.getUser().getUsername(),
                    compatibility
                );
            })
            .filter(compat -> compat.compatibilityScore() >= config.getMinSimilarityScore())
            .sorted((a, b) -> Double.compare(b.compatibilityScore(), a.compatibilityScore()))
            .limit(limit)
            .collect(Collectors.toList());
    }

    @Override
    public double calculateDiversityScore(Long userId) {
        Map<String, Double> genrePrefs = calculateGenrePreferences(userId);
        return algorithmUtils.calculateDiversity(genrePrefs);
    }

    // === CONFIDENCE & QUALITY ===

    @Override
    public double calculateConfidenceScore(Long userId) {
        log.debug("Calculating confidence score for user: {}", userId);
        
        List<UserMediaInteraction> interactions = interactionRepository.findByUserId(userId);
        
        if (interactions.isEmpty()) {
            return 0.0;
        }
        
        int interactionCount = interactions.size();
        
        // Calculate time span of interactions
        LocalDateTime earliest = interactions.stream()
            .map(UserMediaInteraction::getCreatedAt)
            .min(LocalDateTime::compareTo)
            .orElse(LocalDateTime.now());
        
        long timeSpanDays = ChronoUnit.DAYS.between(earliest, LocalDateTime.now());
        
        // Calculate diversity score
        double diversityScore = calculateDiversityScore(userId);
        
        return algorithmUtils.calculateConfidenceScore(interactionCount, timeSpanDays, diversityScore);
    }

    @Override
    public boolean hasSufficientData(Long userId) {
        int interactionCount = interactionRepository.findByUserId(userId).size();
        return interactionCount >= config.getMinInteractionsForRecommendations();
    }

    @Override
    public List<String> getProfileImprovementSuggestions(Long userId) {
        List<String> suggestions = new ArrayList<>();
        
        UserPreferenceProfile profile = getOrCreateUserProfile(userId);
        
        if (profile.getTotalInteractions() < config.getMinInteractionsForRecommendations()) {
            suggestions.add("Rate and interact with more content to improve recommendations");
        }
        
        if (calculateDiversityScore(userId) < 0.3) {
            suggestions.add("Try exploring different genres to discover new interests");
        }
        
        if (profile.getConfidenceScore().doubleValue() < config.getMinConfidenceThreshold()) {
            suggestions.add("Complete watching more content to build a stronger preference profile");
        }
        
        return suggestions;
    }

    // === BATCH OPERATIONS ===

    @Override
    @Transactional
    public int updateActiveUserProfiles(int daysBack) {
        log.info("Updating preference profiles for users active in last {} days", daysBack);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysBack);
        List<UserPreferenceProfile> activeProfiles = preferenceRepository.findRecentlyActiveProfiles(
            cutoffDate,
            BigDecimal.valueOf(config.getMinConfidenceThreshold())
        );
        List<Long> activeUserIds = activeProfiles.stream().map(p -> p.getUser().getId()).toList();
        
        int updated = 0;
        for (Long userId : activeUserIds) {
            try {
                updateUserPreferences(userId);
                updated++;
            } catch (Exception e) {
                log.error("Failed to update preferences for user: {}", userId, e);
            }
        }
        
        log.info("Updated {} user preference profiles", updated);
        return updated;
    }

    @Override
    @Transactional
    public int recalculateLowConfidenceProfiles(double minConfidenceThreshold) {
        log.info("Recalculating profiles with confidence below: {}", minConfidenceThreshold);
        
        List<UserPreferenceProfile> lowConfidenceProfiles = 
            preferenceRepository.findProfilesNeedingUpdate(
                BigDecimal.valueOf(minConfidenceThreshold),
                LocalDateTime.now().minusDays(30)
            );
        
        int recalculated = 0;
        for (UserPreferenceProfile profile : lowConfidenceProfiles) {
            try {
                recalculateUserProfile(profile.getUser().getId());
                recalculated++;
            } catch (Exception e) {
                log.error("Failed to recalculate profile for user: {}", profile.getUser().getId(), e);
            }
        }
        
        log.info("Recalculated {} low confidence profiles", recalculated);
        return recalculated;
    }

    @Override
    @Transactional
    public int cleanupInactiveProfiles(int daysInactive) {
        log.info("Cleaning up profiles inactive for {} days", daysInactive);
        
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        int deletedCount = 0;
        preferenceRepository.deleteInactiveProfiles(cutoffDate);
        
        log.info("Cleaned up {} inactive preference profiles", deletedCount);
        return deletedCount;
    }

    // === ANALYTICS ===

    @Override
    public Map<String, Object> getPlatformPreferenceAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        Object[] averages = preferenceRepository.getPlatformAverages();
        Map<String, Object> platformAverages = new HashMap<>();
        platformAverages.put("averageUserRating", averages[0]);
        platformAverages.put("completionRate", averages[1]);
        platformAverages.put("totalInteractions", averages[2]);
        platformAverages.put("confidenceScore", averages[3]);
        analytics.put("platformAverages", platformAverages);
        
        analytics.put("totalProfilesAnalyzed", preferenceRepository.count());
        analytics.put("generatedAt", LocalDateTime.now());
        
        return analytics;
    }

    @Override
    public List<PersonalityCount> getViewingPersonalityDistribution() {
        List<Object[]> distribution = preferenceRepository.countByPersonality();
        long totalProfiles = preferenceRepository.count();
        
        return distribution.stream()
            .map(row -> {
                ViewingPersonality personality = (ViewingPersonality) row[0];
                Long count = (Long) row[1];
                double percentage = totalProfiles > 0 ? (count * 100.0) / totalProfiles : 0.0;
                
                return new PersonalityCount(personality, count, percentage);
            })
            .collect(Collectors.toList());
    }

    // === PRIVATE HELPER METHODS ===

    private void updateInteractionStatistics(UserPreferenceProfile profile, Long userId) {
        List<UserMediaInteraction> interactions = interactionRepository.findByUserId(userId);
        
        profile.setTotalInteractions(interactions.size());
        
        long completedCount = interactions.stream()
            .filter(i -> i.getStatus() == UserMediaInteraction.Status.COMPLETED)
            .count();
        
        profile.setTotalCompleted((int) completedCount);
        double completionRate = interactions.isEmpty() ? 0.0 : (double) completedCount / interactions.size();
        profile.setCompletionRate(BigDecimal.valueOf(completionRate));
        
        // Calculate average rating and variance
        List<Double> ratings = interactions.stream()
            .map(UserMediaInteraction::getRating)
            .filter(Objects::nonNull)
            .map(r -> r.doubleValue())
            .collect(Collectors.toList());
        if (!ratings.isEmpty()) {
            double avgRating = ratings.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            double variance = algorithmUtils.standardDeviation(ratings);
            profile.setAverageUserRating(BigDecimal.valueOf(avgRating));
            profile.setRatingVariance(BigDecimal.valueOf(variance));
        }
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

    private boolean hasHighRatingVariance(Long userId) {
        List<Double> ratings = interactionRepository.findByUserId(userId).stream()
            .map(UserMediaInteraction::getRating)
            .filter(Objects::nonNull)
            .map(r -> r.doubleValue())
            .collect(Collectors.toList());
        if (ratings.size() < 5) return false;
        
        double variance = algorithmUtils.standardDeviation(ratings);
        return variance > 1.5; // High variance in ratings indicates critical viewing
    }

    private double calculatePersonalityCompatibility(ViewingPersonality p1, ViewingPersonality p2) {
        if (p1 == p2) return 1.0;
        
        // Define compatibility matrix
        return switch (p1) {
            case BINGE_WATCHER -> p2 == ViewingPersonality.BINGE_WATCHER ? 1.0 : 0.6;
            case EXPLORER -> p2 == ViewingPersonality.EXPLORER ? 1.0 : 0.7;
            case CRITIC -> p2 == ViewingPersonality.CRITIC ? 1.0 : 0.5;
            case CASUAL -> 0.8; // Casual viewers are generally compatible with everyone
            default -> 0.7;
        };
    }
} 