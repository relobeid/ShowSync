package com.showsync.service.impl;

import com.showsync.config.ExternalApiProperties;
import com.showsync.service.HealthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;

@Slf4j
@Service
public class HealthServiceImpl implements HealthService {

    @Autowired
    private DataSource dataSource;
    
    @Autowired
    private ExternalApiProperties apiProperties;
    
    @Autowired
    @Qualifier("tmdbWebClient")
    private WebClient tmdbWebClient;
    
    @Autowired
    @Qualifier("openLibraryWebClient")
    private WebClient openLibraryWebClient;

    @Override
    public boolean checkHealth() {
        return true; // Basic health check always returns true
    }

    @Override
    public boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            log.warn("Database health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean checkExternalApiHealth() {
        return checkTmdbApiHealth() && checkOpenLibraryApiHealth();
    }
    
    @Override
    public boolean checkTmdbApiHealth() {
        try {
            // Simple health check endpoint - get configuration which is lightweight
            Boolean isHealthy = tmdbWebClient
                    .get()
                    .uri("/configuration")
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode().is2xxSuccessful())
                    .timeout(Duration.ofSeconds(5))
                    .onErrorReturn(false)
                    .block();
            
            log.debug("TMDb API health check: {}", isHealthy ? "UP" : "DOWN");
            return Boolean.TRUE.equals(isHealthy);
            
        } catch (Exception e) {
            log.warn("TMDb API health check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean checkOpenLibraryApiHealth() {
        try {
            // Simple health check - search for a common term with minimal results
            Boolean isHealthy = openLibraryWebClient
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", "test")
                            .queryParam("limit", "1")
                            .build())
                    .retrieve()
                    .toBodilessEntity()
                    .map(response -> response.getStatusCode().is2xxSuccessful())
                    .timeout(Duration.ofSeconds(10)) // Open Library can be slower
                    .onErrorReturn(false)
                    .block();
            
            log.debug("Open Library API health check: {}", isHealthy ? "UP" : "DOWN");
            return Boolean.TRUE.equals(isHealthy);
            
        } catch (Exception e) {
            log.warn("Open Library API health check failed: {}", e.getMessage());
            return false;
        }
    }
} 