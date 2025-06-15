package com.showsync.dto.external.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * Data Transfer Object for TMDb TV Show API responses.
 * 
 * This class represents the structure of TV show data returned from
 * The Movie Database (TMDb) API. It includes all relevant fields
 * for TV shows with proper JSON property mapping.
 * 
 * Features:
 * - Complete TV show metadata from TMDb
 * - Proper JSON property mapping with snake_case
 * - Support for image URLs and series details
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
public class TmdbTvShowResponse {

    /**
     * TMDb unique identifier for the TV show.
     */
    private Long id;

    /**
     * TV show name.
     */
    private String name;

    /**
     * Original TV show name (in original language).
     */
    @JsonProperty("original_name")
    private String originalName;

    /**
     * TV show overview/description.
     */
    private String overview;

    /**
     * First air date.
     */
    @JsonProperty("first_air_date")
    private LocalDate firstAirDate;

    /**
     * Last air date.
     */
    @JsonProperty("last_air_date")
    private LocalDate lastAirDate;

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
     * TV show popularity score.
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
     * List of origin countries.
     */
    @JsonProperty("origin_country")
    private List<String> originCountry;

    /**
     * Current status of the TV show.
     */
    private String status;

    /**
     * Type of TV show (e.g., "Scripted", "Reality").
     */
    private String type;

    /**
     * TV show tagline.
     */
    private String tagline;

    /**
     * Homepage URL.
     */
    private String homepage;

    /**
     * Whether the show is currently in production.
     */
    @JsonProperty("in_production")
    private Boolean inProduction;

    /**
     * Number of seasons.
     */
    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;

    /**
     * Number of episodes.
     */
    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;

    /**
     * List of episode runtimes in minutes.
     */
    @JsonProperty("episode_run_time")
    private List<Integer> episodeRunTime;

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
     * List of networks that air the show.
     */
    private List<Network> networks;

    /**
     * List of creators of the show.
     */
    @JsonProperty("created_by")
    private List<Creator> createdBy;

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

    /**
     * Network information.
     */
    @Data
    public static class Network {
        private Long id;
        private String name;
        @JsonProperty("logo_path")
        private String logoPath;
        @JsonProperty("origin_country")
        private String originCountry;
    }

    /**
     * Creator information.
     */
    @Data
    public static class Creator {
        private Long id;
        private String name;
        @JsonProperty("profile_path")
        private String profilePath;
        @JsonProperty("credit_id")
        private String creditId;
        private Integer gender;
    }
} 