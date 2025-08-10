package com.showsync.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.showsync.entity.recommendation.ContentLength;
import com.showsync.entity.recommendation.ViewingPersonality;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Entity representing a user's calculated preference profile for AI recommendations.
 * This stores processed preference data, not raw interactions.
 */
@Data
@Entity
@Table(name = "user_preference_profiles")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(exclude = {"user"})
@ToString(exclude = {"user"})
public class UserPreferenceProfile {
    
    private static final Logger logger = LoggerFactory.getLogger(UserPreferenceProfile.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    
    // JSON stored as TEXT for H2/PostgreSQL compatibility
    @Column(name = "genre_preferences", nullable = false, columnDefinition = "TEXT")
    private String genrePreferencesJson = "{}";
    
    @Column(name = "platform_preferences", nullable = false, columnDefinition = "TEXT")
    private String platformPreferencesJson = "{}";
    
    @Column(name = "era_preferences", nullable = false, columnDefinition = "TEXT")
    private String eraPreferencesJson = "{}";
    
    @Column(name = "preferred_content_length")
    @Enumerated(EnumType.STRING)
    private ContentLength preferredContentLength = ContentLength.MEDIUM;
    
    // Rating patterns
    @Column(name = "average_user_rating", precision = 3, scale = 2)
    private BigDecimal averageUserRating = BigDecimal.valueOf(7.0);
    
    @Column(name = "rating_variance", precision = 3, scale = 2)
    private BigDecimal ratingVariance = BigDecimal.valueOf(1.5);
    
    // Activity patterns
    @Column(name = "total_interactions")
    private Integer totalInteractions = 0;
    
    @Column(name = "total_completed")
    private Integer totalCompleted = 0;
    
    @Column(name = "completion_rate", precision = 3, scale = 2)
    private BigDecimal completionRate = BigDecimal.ZERO;
    
    // AI-calculated personality
    @Column(name = "viewing_personality")
    @Enumerated(EnumType.STRING)
    private ViewingPersonality viewingPersonality;
    
    // Algorithm confidence
    @Column(name = "confidence_score", precision = 3, scale = 2)
    private BigDecimal confidenceScore = BigDecimal.ZERO;
    
    // Timestamps
    @Column(name = "last_calculated_at", nullable = false)
    private LocalDateTime lastCalculatedAt = LocalDateTime.now();
    
    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Transient fields for easy access
    @Transient
    private Map<String, Double> genrePreferences;
    
    @Transient
    private Map<String, Double> platformPreferences;
    
    @Transient
    private Map<String, Double> eraPreferences;
    
    /**
     * Default constructor
     */
    public UserPreferenceProfile() {}
    
    /**
     * Constructor with user
     */
    public UserPreferenceProfile(User user) {
        this.user = user;
        this.genrePreferences = new HashMap<>();
        this.platformPreferences = new HashMap<>();
        this.eraPreferences = new HashMap<>();
    }
    
    /**
     * Get genre preferences as Map
     */
    public Map<String, Double> getGenrePreferences() {
        if (genrePreferences == null) {
            genrePreferences = parseJsonToMap(genrePreferencesJson);
        }
        return genrePreferences;
    }
    
    /**
     * Set genre preferences and update JSON
     */
    public void setGenrePreferences(Map<String, Double> preferences) {
        this.genrePreferences = preferences;
        this.genrePreferencesJson = mapToJson(preferences);
    }
    
    /**
     * Get platform preferences as Map
     */
    public Map<String, Double> getPlatformPreferences() {
        if (platformPreferences == null) {
            platformPreferences = parseJsonToMap(platformPreferencesJson);
        }
        return platformPreferences;
    }
    
    /**
     * Set platform preferences and update JSON
     */
    public void setPlatformPreferences(Map<String, Double> preferences) {
        this.platformPreferences = preferences;
        this.platformPreferencesJson = mapToJson(preferences);
    }
    
    /**
     * Get era preferences as Map
     */
    public Map<String, Double> getEraPreferences() {
        if (eraPreferences == null) {
            eraPreferences = parseJsonToMap(eraPreferencesJson);
        }
        return eraPreferences;
    }
    
    /**
     * Set era preferences and update JSON
     */
    public void setEraPreferences(Map<String, Double> preferences) {
        this.eraPreferences = preferences;
        this.eraPreferencesJson = mapToJson(preferences);
    }
    
    /**
     * Check if profile has sufficient data for recommendations
     */
    public boolean hasSufficientData() {
        return totalInteractions >= 5 && confidenceScore.doubleValue() >= 0.3;
    }
    
    /**
     * Get the user's top preferred genres (top 3)
     */
    public String getTopGenres() {
        return getGenrePreferences().entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .reduce((a, b) -> a + ", " + b)
                .orElse("No preferences yet");
    }
    
    /**
     * Calculate genre compatibility with another user (0.0 to 1.0)
     */
    public double calculateGenreCompatibility(UserPreferenceProfile other) {
        Map<String, Double> myGenres = getGenrePreferences();
        Map<String, Double> otherGenres = other.getGenrePreferences();
        
        if (myGenres.isEmpty() || otherGenres.isEmpty()) {
            return 0.0;
        }
        
        // Calculate cosine similarity
        double dotProduct = 0.0;
        double myMagnitude = 0.0;
        double otherMagnitude = 0.0;
        
        for (String genre : myGenres.keySet()) {
            double myScore = myGenres.getOrDefault(genre, 0.0);
            double otherScore = otherGenres.getOrDefault(genre, 0.0);
            
            dotProduct += myScore * otherScore;
            myMagnitude += myScore * myScore;
            otherMagnitude += otherScore * otherScore;
        }
        
        // Include genres only in other's preferences
        for (String genre : otherGenres.keySet()) {
            if (!myGenres.containsKey(genre)) {
                double otherScore = otherGenres.get(genre);
                otherMagnitude += otherScore * otherScore;
            }
        }
        
        if (myMagnitude == 0.0 || otherMagnitude == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (Math.sqrt(myMagnitude) * Math.sqrt(otherMagnitude));
    }
    
    /**
     * Update confidence score based on data quality
     */
    public void updateConfidenceScore() {
        double score = 0.0;
        
        // Base score from interaction count
        score += Math.min(totalInteractions / 20.0, 0.5); // Max 0.5 from interactions
        
        // Bonus for completion rate
        if (totalInteractions > 0) {
            score += completionRate.doubleValue() * 0.2; // Max 0.2 from completion
        }
        
        // Bonus for rating variance (shows thoughtful rating)
        double variance = ratingVariance.doubleValue();
        if (variance > 0.5 && variance < 3.0) {
            score += 0.2; // Good rating variance
        }
        
        // Bonus for genre diversity
        int genreCount = getGenrePreferences().size();
        score += Math.min(genreCount / 10.0, 0.1); // Max 0.1 from diversity
        
        this.confidenceScore = BigDecimal.valueOf(Math.min(score, 1.0));
    }
    
    /**
     * Mark profile as needing recalculation
     */
    public void markForRecalculation() {
        this.confidenceScore = BigDecimal.ZERO;
    }
    
    // Helper methods for JSON serialization
    private Map<String, Double> parseJsonToMap(String json) {
        try {
            if (json == null || json.trim().isEmpty() || "{}".equals(json)) {
                return new HashMap<>();
            }
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            logger.error("Error parsing JSON preferences: {}", json, e);
            return new HashMap<>();
        }
    }
    
    private String mapToJson(Map<String, Double> map) {
        try {
            return objectMapper.writeValueAsString(map != null ? map : new HashMap<>());
        } catch (JsonProcessingException e) {
            logger.error("Error converting map to JSON: {}", map, e);
            return "{}";
        }
    }
    
    /**
     * Update JSON fields before persisting
     */
    @PrePersist
    @PreUpdate
    private void updateJsonFields() {
        if (genrePreferences != null) {
            this.genrePreferencesJson = mapToJson(genrePreferences);
        }
        if (platformPreferences != null) {
            this.platformPreferencesJson = mapToJson(platformPreferences);
        }
        if (eraPreferences != null) {
            this.eraPreferencesJson = mapToJson(eraPreferences);
        }
        this.lastCalculatedAt = LocalDateTime.now();
    }
} 