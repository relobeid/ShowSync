package com.showsync.entity.recommendation;

/**
 * Types of recommendations that can be generated
 */
public enum RecommendationType {
    /**
     * Personal recommendations based on user's individual taste
     */
    PERSONAL,
    
    /**
     * Group-specific recommendations for what to watch together
     */
    GROUP,
    
    /**
     * Trending content across the platform
     */
    TRENDING,
    
    /**
     * Recommendations based on similar users' preferences
     */
    COLLABORATIVE,
    
    /**
     * Recommendations based on content similarity
     */
    CONTENT_BASED
} 