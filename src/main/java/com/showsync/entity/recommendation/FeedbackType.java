package com.showsync.entity.recommendation;

/**
 * Types of feedback users can provide on recommendations
 */
public enum FeedbackType {
    /**
     * User liked the recommendation
     */
    POSITIVE("Positive", "User found this recommendation helpful"),
    
    /**
     * User didn't like the recommendation  
     */
    NEGATIVE("Negative", "User didn't find this recommendation useful"),
    
    /**
     * User was neutral about the recommendation
     */
    NEUTRAL("Neutral", "User was indifferent to this recommendation");
    
    private final String displayName;
    private final String description;
    
    FeedbackType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Convert numeric rating to feedback type
     */
    public static FeedbackType fromRating(int rating) {
        if (rating >= 4) {
            return POSITIVE;
        } else if (rating <= 2) {
            return NEGATIVE;
        } else {
            return NEUTRAL;
        }
    }
    
    /**
     * Get numeric weight for algorithm learning
     */
    public double getWeight() {
        return switch (this) {
            case POSITIVE -> 1.0;
            case NEUTRAL -> 0.0;
            case NEGATIVE -> -1.0;
        };
    }
}

/**
 * Specific reasons for feedback to help improve recommendations
 */
enum FeedbackReason {
    // Positive reasons
    GOOD_MATCH("Great match for my taste"),
    DISCOVERED_NEW("Helped me discover something new"),
    PERFECT_TIMING("Perfect for my current mood"),
    
    // Negative reasons  
    NOT_INTERESTED("Not interested in this type"),
    ALREADY_SEEN("Already watched this"),
    POOR_QUALITY("Poor quality content"),
    WRONG_GENRE("Not my preferred genre"),
    WRONG_TIMING("Not good timing for this"),
    
    // Neutral reasons
    MAYBE_LATER("Might watch later"),
    UNSURE("Unsure about this recommendation");
    
    private final String description;
    
    FeedbackReason(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
} 