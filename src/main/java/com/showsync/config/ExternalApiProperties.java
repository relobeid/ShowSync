package com.showsync.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for external API integrations.
 * 
 * This class binds external API configuration from application.yml
 * and provides type-safe access to API settings for:
 * - The Movie Database (TMDb) API
 * - Open Library API
 * 
 * Features:
 * - Environment-specific configuration support
 * - Rate limiting configuration
 * - Timeout and caching settings
 * - Image URL configuration for media assets
 * 
 * Usage:
 * Inject this component into services that need external API access.
 * All configuration is externalized and can be overridden per environment.
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Data
@ConfigurationProperties(prefix = "external-apis")
public class ExternalApiProperties {

    /**
     * TMDb (The Movie Database) API configuration.
     */
    private TmdbConfig tmdb = new TmdbConfig();

    /**
     * Open Library API configuration.
     */
    private OpenLibraryConfig openLibrary = new OpenLibraryConfig();

    /**
     * TMDb API specific configuration.
     */
    @Data
    public static class TmdbConfig {
        /**
         * Base URL for TMDb API v3.
         */
        private String baseUrl = "https://api.themoviedb.org/3";

        /**
         * API key for TMDb authentication.
         * Should be provided via environment variable TMDB_API_KEY.
         */
        private String apiKey;

        /**
         * Request timeout in milliseconds.
         */
        private int timeout = 10000;

        /**
         * Cache TTL for API responses in seconds.
         */
        private int cacheTtl = 3600;

        /**
         * Rate limit in requests per minute.
         */
        private int rateLimit = 40;

        /**
         * Base URL for TMDb images.
         */
        private String imageBaseUrl = "https://image.tmdb.org/t/p/";

        /**
         * Default poster image size.
         */
        private String posterSize = "w500";

        /**
         * Default backdrop image size.
         */
        private String backdropSize = "w1280";

        /**
         * Get complete poster URL for given path.
         */
        public String getPosterUrl(String path) {
            if (path == null || path.isEmpty()) {
                return null;
            }
            return imageBaseUrl + posterSize + path;
        }

        /**
         * Get complete backdrop URL for given path.
         */
        public String getBackdropUrl(String path) {
            if (path == null || path.isEmpty()) {
                return null;
            }
            return imageBaseUrl + backdropSize + path;
        }
    }

    /**
     * Open Library API specific configuration.
     */
    @Data
    public static class OpenLibraryConfig {
        /**
         * Base URL for Open Library API.
         */
        private String baseUrl = "https://openlibrary.org";

        /**
         * Search API endpoint URL.
         */
        private String searchUrl = "https://openlibrary.org/search.json";

        /**
         * Book details API endpoint URL.
         */
        private String detailsUrl = "https://openlibrary.org/api/books";

        /**
         * Cover images base URL.
         */
        private String coversUrl = "https://covers.openlibrary.org/b";

        /**
         * Request timeout in milliseconds.
         */
        private int timeout = 15000;

        /**
         * Cache TTL for API responses in seconds.
         */
        private int cacheTtl = 7200;

        /**
         * Rate limit in requests per minute.
         */
        private int rateLimit = 100;

        /**
         * Default cover image size (S, M, L).
         */
        private String coverSize = "M";

        /**
         * Get complete cover URL for given ISBN.
         */
        public String getCoverUrlByIsbn(String isbn) {
            if (isbn == null || isbn.isEmpty()) {
                return null;
            }
            return coversUrl + "/isbn/" + isbn + "-" + coverSize + ".jpg";
        }

        /**
         * Get complete cover URL for given Open Library ID.
         */
        public String getCoverUrlById(String id) {
            if (id == null || id.isEmpty()) {
                return null;
            }
            return coversUrl + "/id/" + id + "-" + coverSize + ".jpg";
        }
    }
} 