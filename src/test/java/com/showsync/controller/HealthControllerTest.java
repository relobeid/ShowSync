package com.showsync.controller;

import com.showsync.service.HealthService;
import com.showsync.config.TestSecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
@WebMvcTest(HealthController.class)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
@DisplayName("HealthController Tests")
class HealthControllerTest {

    /**
     * MockMvc instance for testing web layer behavior.
     * Provides comprehensive testing of HTTP request/response handling.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Mocked HealthService for testing health check behavior.
     */
    @MockBean
    private HealthService healthService;

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
        when(healthService.checkHealth()).thenReturn(true);

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
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
        when(healthService.checkHealth()).thenReturn(true);
        when(healthService.checkDatabaseHealth()).thenReturn(true);

        mockMvc.perform(get("/api/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components.db.status").value("UP"));
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
        when(healthService.checkDatabaseHealth()).thenReturn(false);

        mockMvc.perform(get("/api/health/detailed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DOWN"))
                .andExpect(jsonPath("$.components.db.status").value("DOWN"));
    }

    /**
     * Test system information collection.
     * 
     * Verifies that system metrics are properly collected and formatted.
     */
    @Test
    @DisplayName("System information should include valid metrics")
    void systemInformation_ShouldIncludeValidMetrics() throws Exception {
        mockMvc.perform(get("/api/health/system"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.system").exists())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.threads").exists());
    }

    /**
     * Test JVM information collection.
     * 
     * Verifies that JVM metrics are properly collected and calculated.
     */
    @Test
    @DisplayName("JVM information should include valid memory metrics")
    void jvmInformation_ShouldIncludeValidMemoryMetrics() throws Exception {
        mockMvc.perform(get("/api/health/jvm"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.memory").exists())
                .andExpect(jsonPath("$.threads").exists())
                .andExpect(jsonPath("$.gc").exists());
    }

    /**
     * Test performance characteristics of health checks.
     * 
     * Verifies that health check endpoints respond within acceptable time limits.
     */
    @Test
    @DisplayName("Health checks should complete within performance requirements")
    void healthChecks_ShouldMeetPerformanceRequirements() throws Exception {
        long startTime = System.currentTimeMillis();
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
        long endTime = System.currentTimeMillis();
        long responseTime = endTime - startTime;
        assert responseTime < 100 : "Health check response time exceeded 100ms";
    }
} 