package com.showsync.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuration class for WebClient beans used for external API integration.
 * 
 * This configuration provides properly configured WebClient instances for:
 * - TMDb API integration
 * - Open Library API integration
 * - General HTTP client with common settings
 * 
 * Features:
 * - Connection and read timeout configuration
 * - Request/response logging
 * - Error handling and retry logic
 * - Connection pooling and resource management
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final ExternalApiProperties apiProperties;

    /**
     * General purpose WebClient with common configuration.
     * 
     * @return configured WebClient instance
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(createHttpClient(10000)))
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleErrors())
                .build();
    }

    /**
     * WebClient specifically configured for TMDb API calls.
     * 
     * @return TMDb-configured WebClient instance
     */
    @Bean("tmdbWebClient")
    public WebClient tmdbWebClient() {
        return WebClient.builder()
                .baseUrl(apiProperties.getTmdb().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(
                        createHttpClient(apiProperties.getTmdb().getTimeout())))
                .defaultHeader("Content-Type", "application/json")
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleErrors())
                .filter(addApiKeyParameter())
                .build();
    }

    /**
     * WebClient specifically configured for Open Library API calls.
     * 
     * @return Open Library-configured WebClient instance
     */
    @Bean("openLibraryWebClient")
    public WebClient openLibraryWebClient() {
        return WebClient.builder()
                .baseUrl(apiProperties.getOpenLibrary().getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(
                        createHttpClient(apiProperties.getOpenLibrary().getTimeout())))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("User-Agent", "ShowSync/0.1.0 (https://github.com/yourusername/ShowSync)")
                .filter(logRequest())
                .filter(logResponse())
                .filter(handleErrors())
                .build();
    }

    /**
     * Create HttpClient with timeout configuration.
     * 
     * @param timeoutMs timeout in milliseconds
     * @return configured HttpClient
     */
    private HttpClient createHttpClient(int timeoutMs) {
        return HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeoutMs)
                .responseTimeout(Duration.ofMillis(timeoutMs))
                .doOnConnected(conn ->
                        conn.addHandlerLast(new ReadTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS))
                                .addHandlerLast(new WriteTimeoutHandler(timeoutMs, TimeUnit.MILLISECONDS)));
    }

    /**
     * Filter function to log outgoing requests.
     * 
     * @return ExchangeFilterFunction for request logging
     */
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            if (log.isDebugEnabled()) {
                log.debug("External API Request: {} {}", 
                        clientRequest.method(), clientRequest.url());
                clientRequest.headers().forEach((name, values) -> 
                        log.debug("Request Header: {}: {}", name, values));
            }
            return Mono.just(clientRequest);
        });
    }

    /**
     * Filter function to log incoming responses.
     * 
     * @return ExchangeFilterFunction for response logging
     */
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (log.isDebugEnabled()) {
                log.debug("External API Response: {} {}", 
                        clientResponse.statusCode(), clientResponse.headers().asHttpHeaders());
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Filter function to handle common HTTP errors.
     * 
     * @return ExchangeFilterFunction for error handling
     */
    private ExchangeFilterFunction handleErrors() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            if (clientResponse.statusCode().isError()) {
                log.warn("External API Error Response: {} {}", 
                        clientResponse.statusCode(), clientResponse.headers().asHttpHeaders());
                
                return clientResponse.bodyToMono(String.class)
                        .defaultIfEmpty("Unknown error")
                        .flatMap(errorBody -> {
                            log.error("External API Error Body: {}", errorBody);
                            return Mono.just(clientResponse);
                        });
            }
            return Mono.just(clientResponse);
        });
    }

    /**
     * Filter function to add TMDb API key as query parameter.
     * 
     * @return ExchangeFilterFunction for adding API key
     */
    private ExchangeFilterFunction addApiKeyParameter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            String apiKey = apiProperties.getTmdb().getApiKey();
            if (apiKey != null && !apiKey.trim().isEmpty()) {
                String newUrl = clientRequest.url().toString();
                String separator = newUrl.contains("?") ? "&" : "?";
                String modifiedUrl = newUrl + separator + "api_key=" + apiKey;
                
                return Mono.just(ClientRequest.from(clientRequest)
                        .url(java.net.URI.create(modifiedUrl))
                        .build());
            }
            return Mono.just(clientRequest);
        });
    }
} 