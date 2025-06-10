package com.showsync.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.boot.actuator.health.Health; // TODO: Add when Actuator is configured
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive unit tests for HealthController.
 * 
 * This test class verifies the behavior of all health check endpoints
 * under various scenarios including:
 * - Normal operation with healthy components
 * - Database connectivity issues
 * - System resource monitoring
 * - Error handling and recovery
 * - Performance characteristics
 * 
 * Testing Strategy:
 * - Unit tests with mocked dependencies for isolation
 * - Integration tests for end-to-end health check flows
 * - Performance tests to verify response time requirements
 * - Error scenario tests to ensure proper error handling
 * 
 * Architecture Notes:
 * - Uses MockMvc for web layer testing
 * - Mockito for dependency mocking and behavior verification
 * - AssertJ for fluent assertions
 * - JUnit 5 for modern testing features
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HealthController Tests")
class HealthControllerTest {

    /**
     * MockMvc instance for testing web layer behavior.
     * Provides comprehensive testing of HTTP request/response handling.
     */
    private MockMvc mockMvc;

    /**
     * Controller under test.
     * Instantiated with mocked dependencies for isolated testing.
     */
    private HealthController healthController;

    /**
     * Mocked DataSource for database connectivity testing.
     * Allows simulation of various database states and scenarios.
     */
    @Mock
    private DataSource dataSource;

    /**
     * Mocked database connection for testing database health checks.
     */
    @Mock
    private Connection connection;

    /**
     * Mocked database metadata for testing database information retrieval.
     */
    @Mock
    private DatabaseMetaData databaseMetaData;

    /**
     * ObjectMapper for JSON serialization/deserialization in tests.
     */
    private ObjectMapper objectMapper;

    /**
     * Test setup method executed before each test.
     * 
     * Initializes the test environment with:
     * - Controller instance with mocked dependencies
     * - MockMvc configuration for web layer testing
     * - ObjectMapper for JSON processing
     * - Common mock behavior setup
     */
    @BeforeEach
    void setUp() {
        // Initialize controller with mocked dependencies
        healthController = new HealthController();
        
        // Use reflection to inject the mocked DataSource
        // In a real Spring context, this would be handled by @Autowired
        try {
            var dataSourceField = HealthController.class.getDeclaredField("dataSource");
            dataSourceField.setAccessible(true);
            dataSourceField.set(healthController, dataSource);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocked DataSource", e);
        }

        // Configure MockMvc for web layer testing
        mockMvc = MockMvcBuilders.standaloneSetup(healthController)
            .build();

        // Initialize ObjectMapper for JSON processing
        objectMapper = new ObjectMapper();
    }

    /**
     * Test simple health check endpoint with healthy system.
     * 
     * Verifies that:
     * - HTTP 200 status is returned
     * - Response contains correct application information
     * - Response time is included and reasonable
     * - JSON structure matches expected format
     */
    @Test
    @DisplayName("Simple health check should return UP status with basic information")
    void simpleHealthCheck_ShouldReturnUpStatus() throws Exception {
        // Given: System is healthy (no special setup needed for simple check)
        
        // When: Simple health check endpoint is called
        mockMvc.perform(get("/api/health/simple"))
            // Then: Verify HTTP response
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.application").value("ShowSync Backend"))
            .andExpect(jsonPath("$.version").value("0.1.0-SNAPSHOT"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.responseTimeMs").exists())
            .andExpect(jsonPath("$.responseTimeMs").isNumber());

        // Verify that the response time is reasonable (< 100ms for simple check)
        ResponseEntity<Map<String, Object>> response = healthController.simpleHealthCheck();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.get("responseTimeMs")).isInstanceOf(Number.class);
        
        Number responseTime = (Number) body.get("responseTimeMs");
        assertThat(responseTime.longValue()).isLessThan(100L);
    }

    /**
     * Test detailed health check with healthy database.
     * 
     * Verifies comprehensive health check behavior when all components
     * are functioning correctly.
     */
    @Test
    @DisplayName("Detailed health check should return comprehensive status with healthy database")
    void detailedHealthCheck_WithHealthyDatabase_ShouldReturnUpStatus() throws Exception {
        // Given: Healthy database connection
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(databaseMetaData.getDriverName()).thenReturn("PostgreSQL JDBC Driver");

        // When: Detailed health check endpoint is called
        mockMvc.perform(get("/api/health/detailed"))
            // Then: Verify comprehensive response
            .andExpect(status().isOk())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.application").value("ShowSync Backend"))
            .andExpect(jsonPath("$.version").value("0.1.0-SNAPSHOT"))
            .andExpect(jsonPath("$.timestamp").exists())
            .andExpect(jsonPath("$.responseTimeMs").exists())
            .andExpect(jsonPath("$.components").exists())
            .andExpect(jsonPath("$.components.database").exists())
            .andExpect(jsonPath("$.components.database.status").value("UP"))
            .andExpect(jsonPath("$.components.database.database").value("PostgreSQL"))
            .andExpect(jsonPath("$.components.database.driver").value("PostgreSQL JDBC Driver"))
            .andExpect(jsonPath("$.components.system").exists())
            .andExpect(jsonPath("$.components.system.status").value("UP"))
            .andExpect(jsonPath("$.components.jvm").exists())
            .andExpect(jsonPath("$.components.jvm.status").value("UP"));

        // Verify mock interactions
        verify(dataSource).getConnection();
        verify(connection).isValid(5);
        verify(connection).getMetaData();
        verify(connection).close();
    }

    /**
     * Test detailed health check with unhealthy database.
     * 
     * Verifies error handling and appropriate status codes when
     * database connectivity fails.
     */
    @Test
    @DisplayName("Detailed health check should return DOWN status with unhealthy database")
    void detailedHealthCheck_WithUnhealthyDatabase_ShouldReturnDownStatus() throws Exception {
        // Given: Database connection failure
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection failed"));

        // When: Detailed health check endpoint is called
        mockMvc.perform(get("/api/health/detailed"))
            // Then: Verify error response
            .andExpect(status().isServiceUnavailable())
            .andExpect(content().contentType("application/json"))
            .andExpect(jsonPath("$.status").value("DOWN"))
            .andExpect(jsonPath("$.components.database.status").value("DOWN"))
            .andExpect(jsonPath("$.components.database.error").exists())
            .andExpect(jsonPath("$.components.database.error").value(containsString("Connection failed")));

        // Verify mock interactions
        verify(dataSource).getConnection();
        verifyNoInteractions(connection);
    }

    /**
     * Test Spring Boot Actuator health indicator implementation.
     * 
     * TODO: Implement these tests when Actuator is properly configured.
     * For now, we focus on testing the custom health endpoints.
     */
    // @Test
    // @DisplayName("Health indicator should return UP with healthy database")
    // void healthIndicator_WithHealthyDatabase_ShouldReturnUp() throws Exception {
    //     // Implementation will be added when Actuator is configured
    // }

    // @Test
    // @DisplayName("Health indicator should return DOWN with unhealthy database")
    // void healthIndicator_WithUnhealthyDatabase_ShouldReturnDown() throws Exception {
    //     // Implementation will be added when Actuator is configured
    // }

    /**
     * Test system information collection.
     * 
     * Verifies that system metrics are properly collected and formatted.
     */
    @Test
    @DisplayName("System information should include valid metrics")
    void systemInformation_ShouldIncludeValidMetrics() throws Exception {
        // Given: Normal system operation
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(databaseMetaData.getDriverName()).thenReturn("PostgreSQL JDBC Driver");

        // When: Detailed health check is performed
        ResponseEntity<Map<String, Object>> response = healthController.detailedHealthCheck();

        // Then: Verify system information
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) body.get("components");
        assertThat(components).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> systemInfo = (Map<String, Object>) components.get("system");
        assertThat(systemInfo).isNotNull();
        assertThat(systemInfo.get("status")).isEqualTo("UP");
        assertThat(systemInfo.get("processors")).isInstanceOf(Number.class);
        assertThat(systemInfo.get("osName")).isInstanceOf(String.class);
        assertThat(systemInfo.get("osVersion")).isInstanceOf(String.class);
        assertThat(systemInfo.get("osArch")).isInstanceOf(String.class);
        
        // Verify processor count is reasonable
        Number processors = (Number) systemInfo.get("processors");
        assertThat(processors.intValue()).isGreaterThan(0);
        assertThat(processors.intValue()).isLessThan(1000); // Sanity check
    }

    /**
     * Test JVM information collection.
     * 
     * Verifies that JVM metrics are properly collected and calculated.
     */
    @Test
    @DisplayName("JVM information should include valid memory metrics")
    void jvmInformation_ShouldIncludeValidMemoryMetrics() throws Exception {
        // Given: Normal JVM operation
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(databaseMetaData.getDriverName()).thenReturn("PostgreSQL JDBC Driver");

        // When: Detailed health check is performed
        ResponseEntity<Map<String, Object>> response = healthController.detailedHealthCheck();

        // Then: Verify JVM information
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        
        @SuppressWarnings("unchecked")
        Map<String, Object> body = response.getBody();
        assertThat(body).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> components = (Map<String, Object>) body.get("components");
        assertThat(components).isNotNull();
        
        @SuppressWarnings("unchecked")
        Map<String, Object> jvmInfo = (Map<String, Object>) components.get("jvm");
        assertThat(jvmInfo).isNotNull();
        assertThat(jvmInfo.get("status")).isEqualTo("UP");
        assertThat(jvmInfo.get("version")).isInstanceOf(String.class);
        assertThat(jvmInfo.get("vendor")).isInstanceOf(String.class);
        assertThat(jvmInfo.get("memoryUsedMB")).isInstanceOf(Number.class);
        assertThat(jvmInfo.get("memoryFreeMB")).isInstanceOf(Number.class);
        assertThat(jvmInfo.get("memoryTotalMB")).isInstanceOf(Number.class);
        assertThat(jvmInfo.get("memoryMaxMB")).isInstanceOf(Number.class);
        assertThat(jvmInfo.get("memoryUsagePercent")).isInstanceOf(Number.class);
        
        // Verify memory metrics are reasonable
        Number memoryUsagePercent = (Number) jvmInfo.get("memoryUsagePercent");
        assertThat(memoryUsagePercent.doubleValue()).isBetween(0.0, 100.0);
        
        Number memoryUsedMB = (Number) jvmInfo.get("memoryUsedMB");
        assertThat(memoryUsedMB.longValue()).isGreaterThan(0L);
        
        Number memoryMaxMB = (Number) jvmInfo.get("memoryMaxMB");
        assertThat(memoryMaxMB.longValue()).isGreaterThan(memoryUsedMB.longValue());
    }

    /**
     * Test performance characteristics of health checks.
     * 
     * Verifies that health check endpoints respond within acceptable time limits.
     */
    @Test
    @DisplayName("Health checks should complete within performance requirements")
    void healthChecks_ShouldMeetPerformanceRequirements() throws Exception {
        // Given: Healthy system
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(5)).thenReturn(true);
        when(connection.getMetaData()).thenReturn(databaseMetaData);
        when(databaseMetaData.getDatabaseProductName()).thenReturn("PostgreSQL");
        when(databaseMetaData.getDriverName()).thenReturn("PostgreSQL JDBC Driver");

        // When & Then: Test simple health check performance
        long startTime = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> simpleResponse = healthController.simpleHealthCheck();
        long simpleResponseTime = System.currentTimeMillis() - startTime;
        
        assertThat(simpleResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(simpleResponseTime).isLessThan(100L); // Simple check should be < 100ms

        // When & Then: Test detailed health check performance
        startTime = System.currentTimeMillis();
        ResponseEntity<Map<String, Object>> detailedResponse = healthController.detailedHealthCheck();
        long detailedResponseTime = System.currentTimeMillis() - startTime;
        
        assertThat(detailedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(detailedResponseTime).isLessThan(1000L); // Detailed check should be < 1000ms

        // Verify response time is included in response
        @SuppressWarnings("unchecked")
        Map<String, Object> detailedBody = detailedResponse.getBody();
        assertThat(detailedBody).isNotNull();
        assertThat(detailedBody.get("responseTimeMs")).isInstanceOf(Number.class);
    }
} 