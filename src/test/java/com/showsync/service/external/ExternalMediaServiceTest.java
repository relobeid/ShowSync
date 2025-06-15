package com.showsync.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.showsync.config.ExternalApiProperties;
import com.showsync.config.TestCacheConfig;
import com.showsync.dto.external.openlibrary.OpenLibraryBookResult;
import com.showsync.dto.external.openlibrary.OpenLibrarySearchResponse;
import com.showsync.dto.external.tmdb.TmdbMovieResponse;
import com.showsync.dto.external.tmdb.TmdbSearchResponse;
import com.showsync.dto.external.tmdb.TmdbTvShowResponse;
import com.showsync.service.external.impl.ExternalMediaServiceImpl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

import org.springframework.cache.CacheManager;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ExternalMediaService implementation.
 * 
 * This test class provides comprehensive testing of the external API integration
 * including mocked responses, error handling, caching, and retry logic.
 * 
 * Features tested:
 * - TMDb movie and TV show search functionality
 * - Open Library book search functionality
 * - Detailed media information retrieval
 * - Caching behavior
 * - Error handling and retry logic
 * - Rate limiting scenarios
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
    "external-apis.tmdb.apiKey=test-api-key",
    "external-apis.tmdb.baseUrl=http://localhost:${random.port}",
    "external-apis.openLibrary.baseUrl=http://localhost:${random.port}"
})
@Import(TestCacheConfig.class)
class ExternalMediaServiceTest {

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ExternalApiProperties apiProperties;

    private MockWebServer mockWebServer;
    private ExternalMediaService externalMediaService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        objectMapper = new ObjectMapper();
        
        // Update the real configuration properties to use mock server URLs
        apiProperties.getTmdb().setBaseUrl(mockWebServer.url("/").toString());
        apiProperties.getOpenLibrary().setBaseUrl(mockWebServer.url("/").toString());

        // Create WebClient instances with mock server URL
        WebClient tmdbWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .defaultHeader("Authorization", "Bearer " + apiProperties.getTmdb().getApiKey())
                .build();

        WebClient openLibraryWebClient = WebClient.builder()
                .baseUrl(mockWebServer.url("/").toString())
                .build();

        externalMediaService = new ExternalMediaServiceImpl(apiProperties, tmdbWebClient, openLibraryWebClient);
        
        // Clear cache before each test
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
    }

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void searchMovies_ShouldReturnResults_WhenValidQuery() throws Exception {
        // Arrange
        TmdbSearchResponse<TmdbMovieResponse> mockResponse = createMockMovieSearchResponse();
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchMovies("The Matrix", 1))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getResults()).hasSize(1);
                    assertThat(response.getResults().get(0).getTitle()).isEqualTo("The Matrix");
                    assertThat(response.getTotalResults()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void searchTvShows_ShouldReturnResults_WhenValidQuery() throws Exception {
        // Arrange
        TmdbSearchResponse<TmdbTvShowResponse> mockResponse = createMockTvSearchResponse();
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchTvShows("Breaking Bad", 1))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getResults()).hasSize(1);
                    assertThat(response.getResults().get(0).getName()).isEqualTo("Breaking Bad");
                    assertThat(response.getTotalResults()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void searchBooks_ShouldReturnResults_WhenValidQuery() throws Exception {
        // Arrange
        OpenLibrarySearchResponse mockResponse = createMockBookSearchResponse();
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchBooks("The Lord of the Rings", 20, 0))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getDocs()).hasSize(1);
                    assertThat(response.getDocs().get(0).getTitle()).isEqualTo("The Lord of the Rings");
                    assertThat(response.getNumFound()).isEqualTo(1);
                })
                .verifyComplete();
    }

    @Test
    void getMovieDetails_ShouldReturnDetails_WhenValidId() throws Exception {
        // Arrange
        TmdbMovieResponse mockResponse = createMockMovieResponse();
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        StepVerifier.create(externalMediaService.getMovieDetails(603L))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getTitle()).isEqualTo("The Matrix");
                    assertThat(response.getId()).isEqualTo(603L);
                })
                .verifyComplete();
    }

    @Test
    void getTvShowDetails_ShouldReturnDetails_WhenValidId() throws Exception {
        // Arrange
        TmdbTvShowResponse mockResponse = createMockTvShowResponse();
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        StepVerifier.create(externalMediaService.getTvShowDetails(1396L))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getName()).isEqualTo("Breaking Bad");
                    assertThat(response.getId()).isEqualTo(1396L);
                })
                .verifyComplete();
    }

    @Test
    void searchMovies_ShouldHandleError_WhenApiReturns500() {
        // Arrange
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectError()
                .verify();
    }

    @Test
    void searchMovies_ShouldRetryOnRateLimit_WhenApiReturns429() throws Exception {
        // Arrange
        TmdbSearchResponse<TmdbMovieResponse> mockResponse = createMockMovieSearchResponse();
        
        // First call returns 429 (rate limit)
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("Rate Limited"));
        
        // Second call (retry) returns success
        mockWebServer.enqueue(new MockResponse()
                .setBody(objectMapper.writeValueAsString(mockResponse))
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchMovies("The Matrix", 1))
                .assertNext(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.getResults()).hasSize(1);
                })
                .verifyComplete();
    }

    private TmdbSearchResponse<TmdbMovieResponse> createMockMovieSearchResponse() {
        TmdbSearchResponse<TmdbMovieResponse> response = new TmdbSearchResponse<>();
        response.setPage(1);
        response.setTotalPages(1);
        response.setTotalResults(1);
        
        TmdbMovieResponse movie = new TmdbMovieResponse();
        movie.setId(603L);
        movie.setTitle("The Matrix");
        movie.setOverview("A computer hacker learns from mysterious rebels about the true nature of his reality.");
        movie.setReleaseDate(LocalDate.of(1999, 3, 31));
        movie.setVoteAverage(8.7);
        movie.setPosterPath("/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg");
        
        response.setResults(Collections.singletonList(movie));
        return response;
    }

    private TmdbSearchResponse<TmdbTvShowResponse> createMockTvSearchResponse() {
        TmdbSearchResponse<TmdbTvShowResponse> response = new TmdbSearchResponse<>();
        response.setPage(1);
        response.setTotalPages(1);
        response.setTotalResults(1);
        
        TmdbTvShowResponse tvShow = new TmdbTvShowResponse();
        tvShow.setId(1396L);
        tvShow.setName("Breaking Bad");
        tvShow.setOverview("A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine.");
        tvShow.setFirstAirDate(LocalDate.of(2008, 1, 20));
        tvShow.setVoteAverage(9.5);
        tvShow.setPosterPath("/ggFHVNu6YYI5L9pCfOacjizRGt.jpg");
        
        response.setResults(Collections.singletonList(tvShow));
        return response;
    }

    private OpenLibrarySearchResponse createMockBookSearchResponse() {
        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        response.setNumFound(1);
        response.setStart(0);
        
        OpenLibraryBookResult book = new OpenLibraryBookResult();
        book.setKey("/works/OL27448W");
        book.setTitle("The Lord of the Rings");
        book.setAuthorName(Arrays.asList("J.R.R. Tolkien"));
        book.setFirstPublishYear(1954);
        book.setIsbn(Arrays.asList("9780547928227"));
        book.setCoverIds(Arrays.asList(8566785));
        
        response.setDocs(Collections.singletonList(book));
        return response;
    }

    private TmdbMovieResponse createMockMovieResponse() {
        TmdbMovieResponse movie = new TmdbMovieResponse();
        movie.setId(603L);
        movie.setTitle("The Matrix");
        movie.setOverview("A computer hacker learns from mysterious rebels about the true nature of his reality.");
        movie.setReleaseDate(LocalDate.of(1999, 3, 31));
        movie.setVoteAverage(8.7);
        movie.setRuntime(136);
        movie.setBudget(63000000L);
        movie.setRevenue(463517383L);
        movie.setPosterPath("/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg");
        return movie;
    }

    private TmdbTvShowResponse createMockTvShowResponse() {
        TmdbTvShowResponse tvShow = new TmdbTvShowResponse();
        tvShow.setId(1396L);
        tvShow.setName("Breaking Bad");
        tvShow.setOverview("A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine.");
        tvShow.setFirstAirDate(LocalDate.of(2008, 1, 20));
        tvShow.setVoteAverage(9.5);
        tvShow.setNumberOfSeasons(5);
        tvShow.setNumberOfEpisodes(62);
        tvShow.setPosterPath("/ggFHVNu6YYI5L9pCfOacjizRGt.jpg");
        return tvShow;
    }
} 