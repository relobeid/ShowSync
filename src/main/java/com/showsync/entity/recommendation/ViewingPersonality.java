package com.showsync.entity.recommendation;

/**
 * Algorithmic personality classification for users based on viewing patterns
 */
public enum ViewingPersonality {
    /**
     * Watches occasionally, casual viewing habits
     */
    CASUAL("Casual Viewer", "Enjoys light entertainment occasionally"),
    
    /**
     * Critical viewer who rates carefully and prefers quality content
     */
    CRITIC("Critical Viewer", "Values quality and thoughtful content"),
    
    /**
     * Binge watches series and movies in long sessions
     */
    BINGE_WATCHER("Binge Watcher", "Prefers to consume content in long sessions"),
    
    /**
     * Always looking for new and diverse content
     */
    EXPLORER("Content Explorer", "Always seeking new and unique experiences"),
    
    /**
     * Prefers comfort content, rewatches favorites
     */
    COMFORT_SEEKER("Comfort Seeker", "Enjoys familiar and comforting content"),
    
    /**
     * Highly social, influenced by group activities
     */
    SOCIAL("Social Viewer", "Viewing habits influenced by friends and groups"),
    
    /**
     * Follows trends and popular content
     */
    TRENDY("Trend Follower", "Enjoys what's popular and trending"),
    
    /**
     * Prefers niche or specialized content
     */
    NICHE("Niche Enthusiast", "Has specific interests and specialized tastes"),
    
    /**
     * Completionist who finishes what they start
     */
    COMPLETIONIST("Completionist", "Always finishes what they start watching"),
    
    /**
     * Samples a lot but doesn't always finish
     */
    SAMPLER("Content Sampler", "Tries many things but selective about finishing");
    
    private final String displayName;
    private final String description;
    
    ViewingPersonality(String displayName, String description) {
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
     * Get recommendation style for this personality
     */
    public String getRecommendationStyle() {
        return switch (this) {
            case CASUAL -> "Easy-going recommendations with broad appeal";
            case CRITIC -> "High-quality, critically acclaimed content";
            case BINGE_WATCHER -> "Series and long-form content perfect for marathons";
            case EXPLORER -> "Diverse, unique, and international content";
            case COMFORT_SEEKER -> "Familiar genres and feel-good content";
            case SOCIAL -> "Popular content that's great for discussion";
            case TRENDY -> "Latest releases and trending content";
            case NICHE -> "Specialized content matching specific interests";
            case COMPLETIONIST -> "Well-structured series and complete stories";
            case SAMPLER -> "Varied recommendations to explore different tastes";
        };
    }
} 