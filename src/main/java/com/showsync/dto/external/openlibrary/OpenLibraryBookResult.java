package com.showsync.dto.external.openlibrary;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * Data Transfer Object for individual book results from Open Library Search API.
 * 
 * This class represents a single book result from the Open Library search API.
 * It includes comprehensive book metadata with proper JSON property mapping.
 * 
 * Features:
 * - Complete book metadata from Open Library
 * - Proper JSON property mapping with snake_case
 * - Support for multiple authors, ISBNs, and identifiers
 * - Null-safe handling of optional fields
 * 
 * Usage:
 * Used within OpenLibrarySearchResponse to represent individual book results
 * and convert them to internal Media entities.
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Data
public class OpenLibraryBookResult {

    /**
     * Open Library work key (unique identifier).
     */
    private String key;

    /**
     * Book title.
     */
    private String title;

    /**
     * Alternative titles for the book.
     */
    @JsonProperty("alternative_title")
    private List<String> alternativeTitle;

    /**
     * List of author names.
     */
    @JsonProperty("author_name")
    private List<String> authorName;

    /**
     * List of author keys (Open Library identifiers).
     */
    @JsonProperty("author_key")
    private List<String> authorKey;

    /**
     * First publication year.
     */
    @JsonProperty("first_publish_year")
    private Integer firstPublishYear;

    /**
     * List of publication years.
     */
    @JsonProperty("publish_year")
    private List<Integer> publishYear;

    /**
     * List of publishers.
     */
    private List<String> publisher;

    /**
     * List of languages.
     */
    private List<String> language;

    /**
     * List of ISBN-10 numbers.
     */
    private List<String> isbn;

    /**
     * List of ISBN-13 numbers.
     */
    @JsonProperty("isbn_13")
    private List<String> isbn13;

    /**
     * List of LCCN (Library of Congress Control Number).
     */
    private List<String> lccn;

    /**
     * List of OCLC numbers.
     */
    private List<String> oclc;

    /**
     * List of Open Library edition keys.
     */
    @JsonProperty("edition_key")
    private List<String> editionKey;

    /**
     * Number of editions.
     */
    @JsonProperty("edition_count")
    private Integer editionCount;

    /**
     * List of subjects/topics.
     */
    private List<String> subject;

    /**
     * List of people mentioned in the book.
     */
    private List<String> person;

    /**
     * List of places mentioned in the book.
     */
    private List<String> place;

    /**
     * List of time periods mentioned.
     */
    private List<String> time;

    /**
     * List of cover edition keys.
     */
    @JsonProperty("cover_edition_key")
    private String coverEditionKey;

    /**
     * List of cover IDs.
     */
    @JsonProperty("cover_i")
    private List<Integer> coverIds;

    /**
     * Has full text flag.
     */
    @JsonProperty("has_fulltext")
    private Boolean hasFulltext;

    /**
     * Public scan flag.
     */
    @JsonProperty("public_scan_b")
    private Boolean publicScan;

    /**
     * Number of pages (if available).
     */
    @JsonProperty("number_of_pages_median")
    private Integer numberOfPagesMedian;

    /**
     * Reading log count.
     */
    @JsonProperty("readinglog_count")
    private Integer readinglogCount;

    /**
     * Want to read count.
     */
    @JsonProperty("want_to_read_count")
    private Integer wantToReadCount;

    /**
     * Currently reading count.
     */
    @JsonProperty("currently_reading_count")
    private Integer currentlyReadingCount;

    /**
     * Already read count.
     */
    @JsonProperty("already_read_count")
    private Integer alreadyReadCount;

    /**
     * Get the first ISBN-13 if available, otherwise first ISBN-10.
     * 
     * @return the primary ISBN for the book
     */
    public String getPrimaryIsbn() {
        if (isbn13 != null && !isbn13.isEmpty()) {
            return isbn13.get(0);
        }
        if (isbn != null && !isbn.isEmpty()) {
            return isbn.get(0);
        }
        return null;
    }

    /**
     * Get the first author name if available.
     * 
     * @return the primary author name
     */
    public String getPrimaryAuthor() {
        if (authorName != null && !authorName.isEmpty()) {
            return authorName.get(0);
        }
        return null;
    }

    /**
     * Get the first cover ID if available.
     * 
     * @return the cover ID for image URLs
     */
    public Integer getPrimaryCoverId() {
        if (coverIds != null && !coverIds.isEmpty()) {
            return coverIds.get(0);
        }
        return null;
    }
} 