package com.showsync.dto.review;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for detailed media information including reviews and statistics.
 * Provides comprehensive media data for the media details endpoint.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-21
 */
@Data
@Schema(description = "Detailed media information with reviews and statistics")
public class MediaDetailsResponse {
    
    @Schema(description = "Media ID", example = "123")
    private Long id;
    
    @Schema(description = "Media type", example = "MOVIE")
    private String type;
    
    @Schema(description = "Title", example = "The Matrix")
    private String title;
    
    @Schema(description = "Original title", example = "The Matrix")
    private String originalTitle;
    
    @Schema(description = "Description/plot", 
            example = "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers.")
    private String description;
    
    @Schema(description = "Release date", example = "1999-03-31T00:00:00")
    private LocalDateTime releaseDate;
    
    @Schema(description = "Poster image URL")
    private String posterUrl;
    
    @Schema(description = "Backdrop image URL")
    private String backdropUrl;
    
    @Schema(description = "External media ID", example = "603")
    private String externalId;
    
    @Schema(description = "External source", example = "tmdb")
    private String externalSource;
    
    @Schema(description = "Media statistics")
    private MediaStatistics statistics;
    
    @Schema(description = "Recent reviews")
    private List<ReviewResponse> recentReviews;
    
    @Schema(description = "Most helpful reviews")
    private List<ReviewResponse> helpfulReviews;
    
    @Schema(description = "Current user's interaction with this media (if authenticated)")
    private UserInteraction userInteraction;
    
    /**
     * Nested DTO for media statistics.
     */
    @Data
    @Schema(description = "Aggregated media statistics")
    public static class MediaStatistics {
        
        @Schema(description = "Average rating from reviews", example = "8.2")
        private Double averageRating;
        
        @Schema(description = "Average rating from user libraries", example = "8.1")
        private Double libraryAverageRating;
        
        @Schema(description = "Total number of reviews", example = "247")
        private Long totalReviews;
        
        @Schema(description = "Total number of ratings in libraries", example = "1543")
        private Integer totalLibraryRatings;
        
        @Schema(description = "Number of users who marked as favorite", example = "89")
        private Long favoriteCount;
        
        @Schema(description = "Total users who added to library", example = "1856")
        private Long libraryCount;
        
        @Schema(description = "Rating distribution (1-10)")
        private List<RatingDistribution> ratingDistribution;
        
        @Schema(description = "Status distribution from user libraries")
        private StatusDistribution statusDistribution;
        
        @Schema(description = "Trending score (higher means more trending)", example = "95.7")
        private Double trendingScore;
    }
    
    /**
     * Rating distribution information.
     */
    @Data
    @Schema(description = "Rating distribution for a specific rating value")
    public static class RatingDistribution {
        
        @Schema(description = "Rating value (1-10)", example = "8")
        private Integer rating;
        
        @Schema(description = "Number of users who gave this rating", example = "156")
        private Long count;
        
        @Schema(description = "Percentage of total ratings", example = "12.5")
        private Double percentage;
    }
    
    /**
     * Status distribution information.
     */
    @Data
    @Schema(description = "Distribution of user library statuses")
    public static class StatusDistribution {
        
        @Schema(description = "Number planning to watch/read", example = "423")
        private Long planToWatch;
        
        @Schema(description = "Number currently watching/reading", example = "187")
        private Long watching;
        
        @Schema(description = "Number completed", example = "1204")
        private Long completed;
        
        @Schema(description = "Number dropped", example = "34")
        private Long dropped;
        
        @Schema(description = "Number on hold", example = "8")
        private Long onHold;
    }
    
    /**
     * Current user's interaction with this media.
     */
    @Data
    @Schema(description = "Current user's interaction with this media")
    public static class UserInteraction {
        
        @Schema(description = "Whether user has this in their library", example = "true")
        private boolean inLibrary;
        
        @Schema(description = "User's rating (1-10)", example = "9")
        private Integer rating;
        
        @Schema(description = "User's status", example = "COMPLETED")
        private String status;
        
        @Schema(description = "Whether user marked as favorite", example = "true")
        private boolean favorite;
        
        @Schema(description = "Whether user has reviewed this media", example = "false")
        private boolean hasReview;
        
        @Schema(description = "User's review ID if exists", example = "456")
        private Long reviewId;
    }
} 