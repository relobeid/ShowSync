package com.showsync.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ExternalApiProperties configuration loading.
 * 
 * This test verifies that external API properties are correctly loaded
 * from configuration files and environment variables.
 */
@SpringBootTest
@ActiveProfiles("dev")
class ExternalApiPropertiesTest {

    @Autowired
    private ExternalApiProperties externalApiProperties;

    @Test
    void shouldLoadTmdbConfiguration() {
        // Test that TMDb configuration is loaded
        assertThat(externalApiProperties.getTmdb()).isNotNull();
        assertThat(externalApiProperties.getTmdb().getBaseUrl()).isEqualTo("https://api.themoviedb.org/3");
        assertThat(externalApiProperties.getTmdb().getTimeout()).isEqualTo(10000);
        assertThat(externalApiProperties.getTmdb().getCacheTtl()).isEqualTo(3600);
        assertThat(externalApiProperties.getTmdb().getRateLimit()).isEqualTo(40);
    }

    @Test
    void shouldLoadTmdbApiKey() {
        // Test that API key is loaded
        String apiKey = externalApiProperties.getTmdb().getApiKey();
        assertThat(apiKey).isNotNull();
        assertThat(apiKey).isNotEmpty();
        
        // In test environment, it might be the placeholder or actual key
        if (!apiKey.equals("your-tmdb-api-key-here")) {
            // If it's a real API key, verify it's the expected format (32 character hex string)
            assertThat(apiKey).hasSize(32);
            assertThat(apiKey).matches("[a-f0-9]+");
            System.out.println("✅ TMDb API Key loaded successfully: " + apiKey.substring(0, 8) + "...");
        } else {
            System.out.println("✅ TMDb API Key placeholder detected in test environment");
        }
    }

    @Test
    void shouldLoadOpenLibraryConfiguration() {
        // Test that Open Library configuration is loaded
        assertThat(externalApiProperties.getOpenLibrary()).isNotNull();
        assertThat(externalApiProperties.getOpenLibrary().getBaseUrl()).isEqualTo("https://openlibrary.org");
        assertThat(externalApiProperties.getOpenLibrary().getSearchUrl()).isEqualTo("https://openlibrary.org/search.json");
        assertThat(externalApiProperties.getOpenLibrary().getTimeout()).isEqualTo(15000);
        assertThat(externalApiProperties.getOpenLibrary().getCacheTtl()).isEqualTo(7200);
        assertThat(externalApiProperties.getOpenLibrary().getRateLimit()).isEqualTo(100);
    }

    @Test
    void shouldGenerateImageUrls() {
        // Test TMDb image URL generation
        String posterUrl = externalApiProperties.getTmdb().getPosterUrl("/abc123.jpg");
        assertThat(posterUrl).isEqualTo("https://image.tmdb.org/t/p/w500/abc123.jpg");

        String backdropUrl = externalApiProperties.getTmdb().getBackdropUrl("/def456.jpg");
        assertThat(backdropUrl).isEqualTo("https://image.tmdb.org/t/p/w1280/def456.jpg");

        // Test Open Library cover URL generation
        String isbnUrl = externalApiProperties.getOpenLibrary().getCoverUrlByIsbn("9780747532699");
        assertThat(isbnUrl).isEqualTo("https://covers.openlibrary.org/b/isbn/9780747532699-M.jpg");

        String idUrl = externalApiProperties.getOpenLibrary().getCoverUrlById("123456");
        assertThat(idUrl).isEqualTo("https://covers.openlibrary.org/b/id/123456-M.jpg");
    }
} 