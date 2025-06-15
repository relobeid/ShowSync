package com.showsync.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test-specific cache configuration.
 * 
 * This configuration provides a simple in-memory cache manager for testing
 * purposes. It ensures that cache-related functionality can be tested without
 * requiring Redis or other external cache systems.
 * 
 * Key Features:
 * - Uses ConcurrentMapCacheManager for fast in-memory caching during tests
 * - Automatically creates caches on demand
 * - Thread-safe concurrent access
 * - No external dependencies required
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@TestConfiguration
@EnableCaching
public class TestCacheConfig {

    /**
     * Provides a simple in-memory cache manager for testing.
     * 
     * This cache manager uses ConcurrentHashMap internally and creates
     * caches on-demand, making it perfect for unit and integration tests.
     * 
     * @return A ConcurrentMapCacheManager instance
     */
    @Bean
    @Primary
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "tmdb-movies", 
            "tmdb-tv-shows", 
            "tmdb-movie-details", 
            "tmdb-tv-details", 
            "open-library-books"
        );
    }
} 