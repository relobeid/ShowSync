package com.showsync.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Cache Configuration for ShowSync.
 * 
 * This configuration sets up Redis-based caching with optimized TTL policies
 * for different types of cached data, particularly external API responses.
 * 
 * Features:
 * - Different TTL policies for different cache types
 * - JSON serialization for complex objects
 * - String serialization for cache keys
 * - Optimized configuration for external API caching
 * 
 * Cache TTL Strategy:
 * - External API responses: Based on ExternalApiProperties (1-2 hours)
 * - User data: Medium-term caching (30 minutes)
 * - Media metadata: Long-term caching (12 hours)
 * - Group data: Short-term caching (15 minutes)
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@EnableCaching
@RequiredArgsConstructor
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class CacheConfig {

    private final ExternalApiProperties externalApiProperties;

    /**
     * Primary Redis Cache Manager with TTL policies.
     * Only created when Redis connection is available.
     */
    @Bean
    @Primary
    @ConditionalOnBean(RedisConnectionFactory.class)
    public CacheManager redisCacheManager(RedisConnectionFactory redisConnectionFactory) {
        log.info("Configuring Redis Cache Manager with TTL policies");
        
        // Default cache configuration
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // Default TTL: 30 minutes
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues(); // Don't cache null values
        
        // Cache-specific configurations with different TTL values
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // External API responses - use TTL from properties
        cacheConfigurations.put("external-api-responses", defaultConfig
                .entryTtl(Duration.ofSeconds(externalApiProperties.getTmdb().getCacheTtl())));
        
        // User data cache - medium-term
        cacheConfigurations.put("users", defaultConfig
                .entryTtl(Duration.ofMinutes(30)));
        
        // Media metadata - long-term (media info doesn't change often)
        cacheConfigurations.put("media", defaultConfig
                .entryTtl(Duration.ofHours(12)));
        
        // Group data - short-term (group membership can change)
        cacheConfigurations.put("groups", defaultConfig
                .entryTtl(Duration.ofMinutes(15)));
        
        // Recommendations - medium-term (balance between freshness and performance)
        cacheConfigurations.put("recommendations", defaultConfig
                .entryTtl(Duration.ofHours(6)));
        
        log.info("Cache TTL configured - External API: {}s, Users: 30min, Media: 12h, Groups: 15min, Recommendations: 6h",
                externalApiProperties.getTmdb().getCacheTtl());
        
        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    /**
     * Fallback in-memory cache manager when Redis is not available.
     * Provides caching functionality without persistence.
     */
    @Bean
    @ConditionalOnMissingBean(CacheManager.class)
    public CacheManager fallbackCacheManager() {
        log.warn("Redis not available - using in-memory cache manager as fallback");
        return new ConcurrentMapCacheManager(
                "external-api-responses",
                "users", 
                "media", 
                "groups", 
                "recommendations"
        );
    }
} 