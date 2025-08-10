package com.showsync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Configuration properties for the AI recommendation system.
 * Allows fine-tuning of algorithm parameters and system behavior.
 */
@Configuration
@ConfigurationProperties(prefix = "showsync.recommendations")
@Data
public class RecommendationConfig {
    
    // === GENERAL SETTINGS ===
    
    /**
     * Whether the recommendation system is enabled
     */
    private boolean enabled = true;
    
    /**
     * Maximum number of active recommendations per user
     */
    private int maxRecommendationsPerUser = 20;
    
    /**
     * Default batch size for processing users
     */
    private int defaultBatchSize = 100;
    
    // === CONFIDENCE THRESHOLDS ===
    
    /**
     * Minimum confidence score to consider profile reliable
     */
    private double minConfidenceThreshold = 0.5;
    
    /**
     * Minimum interactions needed for generating recommendations
     */
    private int minInteractionsForRecommendations = 5;
    
    /**
     * Minimum interactions for high-confidence recommendations
     */
    private int minInteractionsForHighConfidence = 20;
    
    // === SCORING WEIGHTS ===
    
    /**
     * Weight for genre compatibility in recommendations
     */
    private double genreWeight = 0.4;
    
    /**
     * Weight for rating similarity in recommendations
     */
    private double ratingWeight = 0.3;
    
    /**
     * Weight for platform preferences
     */
    private double platformWeight = 0.2;
    
    /**
     * Weight for era/decade preferences
     */
    private double eraWeight = 0.1;
    
    // === EXPIRATION SETTINGS ===
    
    /**
     * Default expiration time for content recommendations
     */
    private Duration contentRecommendationExpiry = Duration.ofDays(14);
    
    /**
     * Default expiration time for group recommendations
     */
    private Duration groupRecommendationExpiry = Duration.ofDays(7);
    
    /**
     * How often to refresh user preferences
     */
    private Duration preferenceRefreshInterval = Duration.ofDays(7);
    
    // === ALGORITHM PARAMETERS ===
    
    /**
     * Number of similar users to consider for collaborative filtering
     */
    private int collaborativeFilteringUserCount = 50;
    
    /**
     * Minimum similarity score for collaborative filtering
     */
    private double minSimilarityScore = 0.3;
    
    /**
     * Decay factor for older interactions (0.0 to 1.0)
     */
    private double timeDecayFactor = 0.95;
    
    /**
     * Boost factor for recently viewed content
     */
    private double recentViewBoost = 1.2;
    
    // === PERSONALIZATION SETTINGS ===
    
    /**
     * Balance between personalized and trending recommendations (0.0 = all trending, 1.0 = all personalized)
     */
    private double personalizationBalance = 0.7;
    
    /**
     * Diversity factor for recommendations (higher = more diverse)
     */
    private double diversityFactor = 0.3;
    
    /**
     * Exploration factor for new content (higher = more exploration)
     */
    private double explorationFactor = 0.2;
    
    // === FEEDBACK SETTINGS ===
    
    /**
     * Weight for positive feedback in learning
     */
    private double positiveFeedbackWeight = 1.0;
    
    /**
     * Weight for negative feedback in learning
     */
    private double negativeFeedbackWeight = -0.8;
    
    /**
     * Learning rate for updating preferences based on feedback
     */
    private double feedbackLearningRate = 0.1;
    
    // === PERFORMANCE SETTINGS ===
    
    /**
     * Whether to cache recommendation calculations
     */
    private boolean enableCaching = true;
    
    /**
     * Cache TTL for recommendation results
     */
    private Duration cacheExpiry = Duration.ofHours(6);
    
    /**
     * Whether to run recommendation generation asynchronously
     */
    private boolean asyncGeneration = true;
    
    /**
     * Thread pool size for async recommendation generation
     */
    private int asyncThreadPoolSize = 5;
    
    // === SCHEDULING SETTINGS ===
    
    /**
     * Enable scheduled batch generation tasks
     */
    private boolean enableSchedulers = true;
    
    /**
     * Cron expression for daily all-users generation
     * Default: 3:15 AM daily
     */
    private String dailyGenerationCron = "0 15 3 * * *";
    
    /**
     * Cron expression for periodic active-users refresh
     * Default: every hour at minute 10
     */
    private String activeUsersRefreshCron = "0 10 * * * *";
    
    /**
     * Hours window to consider a user as recently active
     */
    private int activeUsersHoursBack = 24;
    
    // === QUALITY CONTROL ===
    
    /**
     * Minimum relevance score to include recommendation
     */
    private double minRelevanceScore = 0.3;
    
    /**
     * Maximum number of recommendations of the same type in a batch
     */
    private int maxSameTypeRecommendations = 5;
    
    /**
     * Whether to filter out content user has already seen
     */
    private boolean filterSeenContent = true;
    
    // === FEATURE FLAGS ===
    
    /**
     * Feature flags for different recommendation types
     */
    private FeatureFlags features = new FeatureFlags();
    
    @Data
    public static class FeatureFlags {
        private boolean personalRecommendations = true;
        private boolean groupRecommendations = true;
        private boolean trendingRecommendations = true;
        private boolean collaborativeFiltering = true;
        private boolean contentBasedFiltering = true;
        private boolean crossGenreRecommendations = true;
        private boolean seasonalRecommendations = false;
        private boolean experimentalAlgorithms = false;
    }
    
    // === GENRE MAPPINGS ===
    
    /**
     * Genre similarity mappings for cross-genre recommendations
     */
    private Map<String, List<String>> genreSimilarities = Map.of(
        "Action", List.of("Adventure", "Thriller", "Crime"),
        "Drama", List.of("Romance", "Biography", "History"),
        "Comedy", List.of("Family", "Animation", "Musical"),
        "Horror", List.of("Thriller", "Mystery", "Supernatural"),
        "Sci-Fi", List.of("Fantasy", "Adventure", "Thriller")
    );
    
    // === PLATFORM PRIORITIES ===
    
    /**
     * Platform priority scores for recommendations
     */
    private Map<String, Double> platformPriorities = Map.of(
        "Netflix", 1.0,
        "Disney+", 0.9,
        "HBO Max", 0.9,
        "Amazon Prime", 0.8,
        "Hulu", 0.7,
        "Apple TV+", 0.6
    );
    
    // === VALIDATION ===
    
    /**
     * Validate configuration on startup
     */
    public void validate() {
        if (genreWeight + ratingWeight + platformWeight + eraWeight != 1.0) {
            throw new IllegalStateException("Recommendation weights must sum to 1.0");
        }
        
        if (minConfidenceThreshold < 0.0 || minConfidenceThreshold > 1.0) {
            throw new IllegalStateException("Confidence threshold must be between 0.0 and 1.0");
        }
        
        if (personalizationBalance < 0.0 || personalizationBalance > 1.0) {
            throw new IllegalStateException("Personalization balance must be between 0.0 and 1.0");
        }
    }
    
    // === UTILITY METHODS ===
    
    /**
     * Get expiry duration for recommendation type
     */
    public Duration getExpiryForType(String recommendationType) {
        return switch (recommendationType.toUpperCase()) {
            case "GROUP" -> groupRecommendationExpiry;
            case "CONTENT" -> contentRecommendationExpiry;
            default -> contentRecommendationExpiry;
        };
    }
    
    /**
     * Check if feature is enabled
     */
    public boolean isFeatureEnabled(String featureName) {
        return switch (featureName.toLowerCase()) {
            case "personal" -> features.personalRecommendations;
            case "group" -> features.groupRecommendations;
            case "trending" -> features.trendingRecommendations;
            case "collaborative" -> features.collaborativeFiltering;
            case "content-based" -> features.contentBasedFiltering;
            case "cross-genre" -> features.crossGenreRecommendations;
            case "seasonal" -> features.seasonalRecommendations;
            case "experimental" -> features.experimentalAlgorithms;
            default -> false;
        };
    }
    
    /**
     * Get similar genres for a given genre
     */
    public List<String> getSimilarGenres(String genre) {
        return genreSimilarities.getOrDefault(genre, List.of());
    }
    
    /**
     * Get platform priority score
     */
    public double getPlatformPriority(String platform) {
        return platformPriorities.getOrDefault(platform, 0.5);
    }
} 