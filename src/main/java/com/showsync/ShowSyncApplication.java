package com.showsync;

import com.showsync.config.ExternalApiProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * ShowSync Application - Main entry point for the ShowSync backend service.
 * 
 * ShowSync is an AI-powered social media discovery platform for TV shows, movies, and books.
 * This application provides REST APIs for:
 * - User management and authentication
 * - Media library management (movies, TV shows, books)
 * - Social group management and activities
 * - AI-powered group matching and content recommendations
 * - Real-time communication and notifications
 * 
 * Key Features Enabled:
 * - @SpringBootApplication: Auto-configuration, component scanning, and configuration
 * - @EnableJpaAuditing: Automatic auditing of entity creation and modification timestamps
 * - @EnableCaching: Application-level caching support for performance optimization
 * - @EnableAsync: Asynchronous method execution for non-blocking operations
 * - @EnableScheduling: Support for scheduled tasks (cleanup, notifications, etc.)
 * - @EnableTransactionManagement: Declarative transaction management
 * 
 * Architecture Notes:
 * - Follows hexagonal architecture principles with clear separation of concerns
 * - Uses Spring Boot's auto-configuration for rapid development
 * - Implements comprehensive logging, monitoring, and health checks
 * - Designed for scalability with caching, async processing, and database optimization
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableJpaAuditing
@EnableConfigurationProperties(ExternalApiProperties.class)
// @EnableCaching // TODO: Re-enable when Redis is configured
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class ShowSyncApplication {

    /**
     * Main method to start the ShowSync application.
     * 
     * This method:
     * 1. Initializes the Spring Boot application context
     * 2. Starts the embedded Tomcat server
     * 3. Enables all configured features (JPA, caching, security, etc.)
     * 4. Loads application properties and profiles
     * 5. Sets up monitoring and health check endpoints
     * 
     * The application will be available at:
     * - Main API: http://localhost:8080/api
     * - Health Check: http://localhost:8080/actuator/health
     * - API Documentation: http://localhost:8080/swagger-ui.html
     * - Metrics: http://localhost:8080/actuator/metrics
     * 
     * @param args Command line arguments passed to the application.
     *             Common arguments include:
     *             --spring.profiles.active=dev/test/prod
     *             --server.port=8080
     *             --logging.level.com.showsync=DEBUG
     */
    public static void main(String[] args) {
        // Configure system properties before starting the application
        configureSystemProperties();
        
        // Start the Spring Boot application
        SpringApplication application = new SpringApplication(ShowSyncApplication.class);
        
        // Add custom application listeners if needed
        // application.addListeners(new CustomApplicationListener());
        
        // Set default properties that can be overridden by application.properties
        // Properties defaultProperties = new Properties();
        // defaultProperties.setProperty("server.port", "8080");
        // application.setDefaultProperties(defaultProperties);
        
        // Start the application
        application.run(args);
    }
    
    /**
     * Configure system-level properties before application startup.
     * 
     * This method sets up:
     * - JVM timezone to UTC for consistent timestamp handling
     * - File encoding to UTF-8 for proper character handling
     * - Security properties for enhanced protection
     * - Logging configuration optimizations
     */
    private static void configureSystemProperties() {
        // Set timezone to UTC for consistent timestamp handling across different environments
        System.setProperty("user.timezone", "UTC");
        
        // Ensure UTF-8 encoding for file operations
        System.setProperty("file.encoding", "UTF-8");
        
        // Security enhancements
        System.setProperty("java.security.egd", "file:/dev/./urandom");
        
        // Spring Boot specific optimizations
        System.setProperty("spring.jpa.open-in-view", "false");
        System.setProperty("spring.main.lazy-initialization", "false");
        
        // Logging optimizations
        System.setProperty("logging.pattern.level", "%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]");
    }
} 