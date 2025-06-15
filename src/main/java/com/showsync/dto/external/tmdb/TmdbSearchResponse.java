package com.showsync.dto.external.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for TMDb Search API responses.
 * 
 * This class represents the paginated search results structure
 * returned from The Movie Database (TMDb) API search endpoints.
 * It can handle both movie and TV show search results.
 * 
 * Features:
 * - Paginated search results with metadata
 * - Generic result items (movies or TV shows)
 * - Total results and page information
 * - Proper JSON property mapping
 * 
 * Usage:
 * Used by TMDb service classes to deserialize search API responses
 * and extract individual media items for further processing.
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Data
public class TmdbSearchResponse<T> {

    /**
     * Current page number.
     */
    private Integer page;

    /**
     * List of search result items.
     * Generic type T can be TmdbMovieResponse or TmdbTvShowResponse.
     */
    private List<T> results;

    /**
     * Total number of pages available.
     */
    @JsonProperty("total_pages")
    private Integer totalPages;

    /**
     * Total number of results across all pages.
     */
    @JsonProperty("total_results")
    private Integer totalResults;

    /**
     * Check if there are more pages available.
     * 
     * @return true if current page is less than total pages
     */
    public boolean hasMorePages() {
        return page != null && totalPages != null && page < totalPages;
    }

    /**
     * Check if this is the first page.
     * 
     * @return true if current page is 1
     */
    public boolean isFirstPage() {
        return page != null && page == 1;
    }

    /**
     * Check if this is the last page.
     * 
     * @return true if current page equals total pages
     */
    public boolean isLastPage() {
        return page != null && totalPages != null && page.equals(totalPages);
    }

    /**
     * Get the number of results in this page.
     * 
     * @return number of results in current page
     */
    public int getResultCount() {
        return results != null ? results.size() : 0;
    }
} 