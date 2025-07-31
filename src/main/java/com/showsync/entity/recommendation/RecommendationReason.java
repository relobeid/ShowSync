package com.showsync.entity.recommendation;

/**
 * Reason codes explaining why a recommendation was made
 */
public enum RecommendationReason {
    // Content-based reasons
    GENRE_MATCH("Based on your genre preferences"),
    SIMILAR_CONTENT("Because you liked similar content"),
    SAME_DIRECTOR("From directors you enjoy"),
    SAME_ACTOR("Featuring actors you like"),
    
    // Social reasons
    GROUP_ACTIVITY("Popular in your groups"),
    SIMILAR_USERS("Users with similar taste enjoyed this"),
    FRIEND_ACTIVITY("Based on friend activity"),
    
    // Trending reasons
    TRENDING_GLOBAL("Trending globally"),
    TRENDING_GENRE("Trending in your favorite genres"),
    HIGHLY_RATED("Highly rated by the community"),
    
    // Group matching reasons
    ACTIVITY_LEVEL("Matches your activity level"),
    GENRE_COMPATIBILITY("Similar genre preferences"),
    SIZE_PREFERENCE("Matches your preferred group size"),
    
    // Platform reasons
    NEW_RELEASE("Recently added"),
    ENDING_SOON("Leaving platform soon"),
    AWARD_WINNER("Award-winning content"),
    
    // Behavioral reasons
    COMPLETION_PATTERN("Matches your viewing patterns"),
    TIME_BASED("Good for your typical viewing time"),
    BINGE_WORTHY("Perfect for binge watching"),
    
    // Fallback
    GENERAL("General recommendation");
    
    private final String description;
    
    RecommendationReason(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get user-friendly explanation with context
     */
    public String getExplanation(String contextItem) {
        return switch (this) {
            case GENRE_MATCH -> "Based on your love for " + contextItem;
            case SIMILAR_CONTENT -> "Because you enjoyed " + contextItem;
            case GROUP_ACTIVITY -> "Popular in " + contextItem;
            case SIMILAR_USERS -> "Users who liked " + contextItem + " also enjoyed this";
            default -> description;
        };
    }
} 