package com.showsync.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Cache Configuration.
 * 
 * This test verifies that cache configuration is properly set up
 * and cache managers are correctly configured.
 */
@SpringBootTest
@ActiveProfiles("test")
class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldConfigureCacheManager() {
        // Verify cache manager is available
        assertThat(cacheManager).isNotNull();
        
        // Verify expected cache names are available
        assertThat(cacheManager.getCacheNames())
                .contains("external-api-responses")
                .contains("users")
                .contains("media") 
                .contains("groups")
                .contains("recommendations");
        
        System.out.println("✅ Cache Manager configured with caches: " + cacheManager.getCacheNames());
    }

    @Test
    void shouldCreateExternalApiResponsesCache() {
        // Test that external-api-responses cache can be created and used
        var cache = cacheManager.getCache("external-api-responses");
        assertThat(cache).isNotNull();
        
        // Test cache operations
        String testKey = "test-tmdb-movie-search-matrix-page-1";
        String testValue = "cached-response-data";
        
        cache.put(testKey, testValue);
        var cachedValue = cache.get(testKey, String.class);
        
        assertThat(cachedValue).isEqualTo(testValue);
        
        System.out.println("✅ External API cache working correctly");
    }
} 