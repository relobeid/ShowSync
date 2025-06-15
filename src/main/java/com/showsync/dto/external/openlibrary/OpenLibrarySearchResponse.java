package com.showsync.dto.external.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for Open Library Search API responses.
 * 
 * This class represents the search results structure returned from
 * the Open Library API search endpoint. It includes pagination
 * information and a list of book results.
 * 
 * Features:
 * - Paginated search results with metadata
 * - List of book search results
 * - Total results and offset information
 * - Proper JSON property mapping
 * 
 * Usage:
 * Used by Open Library service classes to deserialize search API responses
 * and extract individual book items for further processing.
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Data
public class OpenLibrarySearchResponse {

    /**
     * Number of results found.
     */
    @JsonProperty("numFound")
    private Integer numFound;

    /**
     * Starting offset for results.
     */
    private Integer start;

    /**
     * Number of results returned in this response.
     */
    @JsonProperty("numFoundExact")
    private Boolean numFoundExact;

    /**
     * List of book search results.
     */
    @JsonProperty("docs")
    private List<OpenLibraryBookResult> docs;

    /**
     * Query execution time in milliseconds.
     */
    @JsonProperty("q")
    private String query;

    /**
     * Offset information for pagination.
     */
    private Integer offset;

    /**
     * Check if there are more results available.
     * 
     * @param limit the limit used in the query
     * @return true if there are more results beyond current batch
     */
    public boolean hasMoreResults(int limit) {
        if (numFound == null || start == null) {
            return false;
        }
        return (start + limit) < numFound;
    }

    /**
     * Get the number of results in this response.
     * 
     * @return number of results in current response
     */
    public int getResultCount() {
        return docs != null ? docs.size() : 0;
    }

    /**
     * Check if this is the first page of results.
     * 
     * @return true if start is 0 or null
     */
    public boolean isFirstPage() {
        return start == null || start == 0;
    }
} 