package com.showsync.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.showsync.config.TestCacheConfig;
import com.showsync.dto.external.openlibrary.OpenLibraryBookResult;
import com.showsync.dto.external.openlibrary.OpenLibrarySearchResponse;
import com.showsync.dto.external.tmdb.TmdbMovieResponse;
import com.showsync.dto.external.tmdb.TmdbSearchResponse;
import com.showsync.dto.external.tmdb.TmdbTvShowResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for ExternalMediaService.
 * 
 * This test class uses WireMock to mock external API responses and tests
 * the complete integration with WebClient, caching, and response mapping.
 * 
 * Key Features:
 * - Uses WireMock 3.x for Jakarta EE compatibility
 * - Dynamic port configuration with @DynamicPropertySource
 * - Proper WebClient bean replacement with bean overriding
 * - Comprehensive test coverage for all external API methods
 * - Cache integration testing
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = {
    "external-apis.tmdb.apiKey=test-api-key",
    "spring.main.allow-bean-definition-overriding=true"
})
@Import({TestCacheConfig.class, ExternalMediaServiceTest.TestWebClientConfig.class})
class ExternalMediaServiceTest {

    private static WireMockServer tmdbWireMock;
    private static WireMockServer openLibraryWireMock;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private ExternalMediaService externalMediaService;

    private ObjectMapper objectMapper;

    @BeforeAll
    static void setUpClass() {
        // Start WireMock servers before Spring context initialization
        tmdbWireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        openLibraryWireMock = new WireMockServer(WireMockConfiguration.options().dynamicPort());
        
        tmdbWireMock.start();
        openLibraryWireMock.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // Configure the base URLs for the WebClients to use the WireMock ports
        registry.add("test.tmdb.baseUrl", () -> "http://localhost:" + tmdbWireMock.port());
        registry.add("test.openlibrary.baseUrl", () -> "http://localhost:" + openLibraryWireMock.port());
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // Handle LocalDate serialization
        
        // Clear cache before each test
        if (cacheManager != null) {
            cacheManager.getCacheNames().forEach(cacheName -> {
                var cache = cacheManager.getCache(cacheName);
                if (cache != null) {
                    cache.clear();
                }
            });
        }
        
        // Reset WireMock stubs
        tmdbWireMock.resetAll();
        openLibraryWireMock.resetAll();
    }

    @AfterEach
    void tearDown() {
        // Reset WireMock after each test
        if (tmdbWireMock != null) {
            tmdbWireMock.resetAll();
        }
        if (openLibraryWireMock != null) {
            openLibraryWireMock.resetAll();
        }
    }

    @Test
    void searchMovies_ShouldReturnResults_WhenValidQuery() throws Exception {
        // Arrange
        TmdbSearchResponse<TmdbMovieResponse> mockResponse = createMockMovieSearchResponse();
        
        tmdbWireMock.stubFor(get(urlPathEqualTo("/search/movie"))
                .withQueryParam("query", equalTo("test"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("include_adult", equalTo("false"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
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
        
        tmdbWireMock.stubFor(get(urlPathEqualTo("/search/tv"))
                .withQueryParam("query", equalTo("test"))
                .withQueryParam("page", equalTo("1"))
                .withQueryParam("include_adult", equalTo("false"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchTvShows("test", 1))
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
        
        openLibraryWireMock.stubFor(get(urlPathEqualTo("/search.json"))
                .withQueryParam("q", equalTo("test"))
                .withQueryParam("limit", equalTo("20"))
                .withQueryParam("offset", equalTo("0"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchBooks("test", 20, 0))
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
        
        tmdbWireMock.stubFor(get(urlPathEqualTo("/movie/603"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

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
        
        tmdbWireMock.stubFor(get(urlPathEqualTo("/tv/1396"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .withBody(objectMapper.writeValueAsString(mockResponse))));

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
        tmdbWireMock.stubFor(get(urlPathEqualTo("/search/movie"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody("Internal Server Error")));

        // Act & Assert
        StepVerifier.create(externalMediaService.searchMovies("test", 1))
                .expectError()
                .verify();
    }

    private TmdbSearchResponse<TmdbMovieResponse> createMockMovieSearchResponse() {
        TmdbSearchResponse<TmdbMovieResponse> response = new TmdbSearchResponse<>();
        response.setPage(1);
        response.setTotalPages(1);
        response.setTotalResults(1);
        response.setResults(Arrays.asList(createMockMovieResponse()));
        return response;
    }

    private TmdbSearchResponse<TmdbTvShowResponse> createMockTvSearchResponse() {
        TmdbSearchResponse<TmdbTvShowResponse> response = new TmdbSearchResponse<>();
        response.setPage(1);
        response.setTotalPages(1);
        response.setTotalResults(1);
        response.setResults(Arrays.asList(createMockTvShowResponse()));
        return response;
    }

    private OpenLibrarySearchResponse createMockBookSearchResponse() {
        OpenLibrarySearchResponse response = new OpenLibrarySearchResponse();
        response.setNumFound(1);
        response.setStart(0);
        response.setDocs(Arrays.asList(createMockBookResult()));
        return response;
    }

    private TmdbMovieResponse createMockMovieResponse() {
        TmdbMovieResponse movie = new TmdbMovieResponse();
        movie.setId(603L);
        movie.setTitle("The Matrix");
        movie.setOverview("A computer hacker learns from mysterious rebels about the true nature of his reality.");
        movie.setReleaseDate(LocalDate.of(1999, 3, 31));
        movie.setVoteAverage(8.7);
        movie.setVoteCount(18040);
        movie.setPosterPath("/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg");
        movie.setBackdropPath("/fNG7i7RqMErkcqhohV2a6cV1Ehy.jpg");
        movie.setPopularity(42.818);
        movie.setAdult(false);
        movie.setOriginalLanguage("en");
        movie.setOriginalTitle("The Matrix");
        movie.setGenreIds(Arrays.asList(28, 878));
        return movie;
    }

    private TmdbTvShowResponse createMockTvShowResponse() {
        TmdbTvShowResponse tvShow = new TmdbTvShowResponse();
        tvShow.setId(1396L);
        tvShow.setName("Breaking Bad");
        tvShow.setOverview("A high school chemistry teacher diagnosed with inoperable lung cancer turns to manufacturing and selling methamphetamine.");
        tvShow.setFirstAirDate(LocalDate.of(2008, 1, 20));
        tvShow.setLastAirDate(LocalDate.of(2013, 9, 29));
        tvShow.setVoteAverage(9.5);
        tvShow.setVoteCount(8503);
        tvShow.setPosterPath("/ggFHVNu6YYI5L9pCfOacjizRGt.jpg");
        tvShow.setBackdropPath("/suopoADq0k8YZr4dQXcU6pToj6s.jpg");
        tvShow.setPopularity(261.694);
        tvShow.setOriginalLanguage("en");
        tvShow.setOriginalName("Breaking Bad");
        tvShow.setGenreIds(Arrays.asList(18, 80));
        tvShow.setOriginCountry(Arrays.asList("US"));
        return tvShow;
    }

    private OpenLibraryBookResult createMockBookResult() {
        OpenLibraryBookResult book = new OpenLibraryBookResult();
        book.setKey("/works/OL27448W");
        book.setTitle("The Lord of the Rings");
        book.setAuthorName(Arrays.asList("J.R.R. Tolkien"));
        book.setFirstPublishYear(1954);
        book.setIsbn(Arrays.asList("9780544003415"));
        book.setCoverIds(Arrays.asList(8566616));
        return book;
    }

    /**
     * Test configuration that provides WebClient beans pointing to WireMock servers.
     */
    @TestConfiguration
    static class TestWebClientConfig {

        @Bean("tmdbWebClient")
        public WebClient tmdbWebClient() {
            return WebClient.builder()
                    .baseUrl("http://localhost:" + tmdbWireMock.port())
                    .defaultHeader("Authorization", "Bearer test-api-key")
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }
        
        @Bean("openLibraryWebClient")
        public WebClient openLibraryWebClient() {
            return WebClient.builder()
                    .baseUrl("http://localhost:" + openLibraryWireMock.port())
                    .defaultHeader("Content-Type", "application/json")
                    .build();
        }
    }
}