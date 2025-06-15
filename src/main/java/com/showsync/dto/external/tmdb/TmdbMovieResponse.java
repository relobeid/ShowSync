package com.showsync.dto.external.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for TMDb Movie API responses.
 * 
 * This class represents the structure of movie data returned from
 * The Movie Database (TMDb) API. It includes all relevant fields
 * for movies with proper JSON property mapping.
 * 
 * Features:
 * - Complete movie metadata from TMDb
 * - Proper JSON property mapping with snake_case
 * - Support for image URLs and media details
 * - Null-safe handling of optional fields
 * 
 * Usage:
 * Used by TMDb service classes to deserialize API responses
 * and convert them to internal Media entities.
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Data
public class TmdbMovieResponse {

    /**
     * TMDb unique identifier for the movie.
     */
    private Long id;

    /**
     * Movie title.
     */
    private String title;

    /**
     * Original movie title (in original language).
     */
    @JsonProperty("original_title")
    private String originalTitle;

    /**
     * Movie overview/description.
     */
    private String overview;

    /**
     * Movie release date.
     */
    @JsonProperty("release_date")
    private LocalDate releaseDate;

    /**
     * Poster image path (relative to TMDb image base URL).
     */
    @JsonProperty("poster_path")
    private String posterPath;

    /**
     * Backdrop image path (relative to TMDb image base URL).
     */
    @JsonProperty("backdrop_path")
    private String backdropPath;

    /**
     * Movie popularity score.
     */
    private Double popularity;

    /**
     * Average user rating (0-10).
     */
    @JsonProperty("vote_average")
    private Double voteAverage;

    /**
     * Total number of votes.
     */
    @JsonProperty("vote_count")
    private Integer voteCount;

    /**
     * Adult content flag.
     */
    private Boolean adult;

    /**
     * Original language code (e.g., "en", "fr").
     */
    @JsonProperty("original_language")
    private String originalLanguage;

    /**
     * List of genre IDs.
     */
    @JsonProperty("genre_ids")
    private List<Integer> genreIds;

    /**
     * Runtime in minutes (only available in detailed responses).
     */
    private Integer runtime;

    /**
     * Movie status (e.g., "Released", "In Production").
     */
    private String status;

    /**
     * Movie tagline.
     */
    private String tagline;

    /**
     * Production budget in USD.
     */
    private Long budget;

    /**
     * Box office revenue in USD.
     */
    private Long revenue;

    /**
     * IMDb ID for cross-reference.
     */
    @JsonProperty("imdb_id")
    private String imdbId;

    /**
     * Homepage URL.
     */
    private String homepage;

    /**
     * List of production companies (in detailed responses).
     */
    @JsonProperty("production_companies")
    private List<ProductionCompany> productionCompanies;

    /**
     * List of production countries (in detailed responses).
     */
    @JsonProperty("production_countries")
    private List<ProductionCountry> productionCountries;

    /**
     * List of spoken languages (in detailed responses).
     */
    @JsonProperty("spoken_languages")
    private List<SpokenLanguage> spokenLanguages;

    /**
     * Production company information.
     */
    @Data
    public static class ProductionCompany {
        private Long id;
        private String name;
        @JsonProperty("logo_path")
        private String logoPath;
        @JsonProperty("origin_country")
        private String originCountry;
    }

    /**
     * Production country information.
     */
    @Data
    public static class ProductionCountry {
        @JsonProperty("iso_3166_1")
        private String iso31661;
        private String name;
    }

    /**
     * Spoken language information.
     */
    @Data
    public static class SpokenLanguage {
        @JsonProperty("english_name")
        private String englishName;
        @JsonProperty("iso_639_1")
        private String iso6391;
        private String name;
    }
} 