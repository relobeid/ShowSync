package com.showsync.entity.recommendation;

/**
 * Content length preferences for users
 */
public enum ContentLength {
    /**
     * Short content - under 30 minutes
     */
    SHORT("Short", "Under 30 minutes", 0, 30),
    
    /**
     * Medium content - 30 minutes to 2 hours
     */
    MEDIUM("Medium", "30 minutes to 2 hours", 30, 120),
    
    /**
     * Long content - over 2 hours
     */
    LONG("Long", "Over 2 hours", 120, Integer.MAX_VALUE);
    
    private final String displayName;
    private final String description;
    private final int minMinutes;
    private final int maxMinutes;
    
    ContentLength(String displayName, String description, int minMinutes, int maxMinutes) {
        this.displayName = displayName;
        this.description = description;
        this.minMinutes = minMinutes;
        this.maxMinutes = maxMinutes;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getMinMinutes() {
        return minMinutes;
    }
    
    public int getMaxMinutes() {
        return maxMinutes;
    }
    
    /**
     * Determine content length category from runtime minutes
     */
    public static ContentLength fromMinutes(int minutes) {
        if (minutes < 30) {
            return SHORT;
        } else if (minutes <= 120) {
            return MEDIUM;
        } else {
            return LONG;
        }
    }
    
    /**
     * Check if given runtime fits this length category
     */
    public boolean contains(int minutes) {
        return minutes >= minMinutes && minutes < maxMinutes;
    }
} 