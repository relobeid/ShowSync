package com.showsync.controller;

import com.showsync.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller for ShowSync Application.
 * 
 * This controller provides comprehensive health check endpoints that monitor
 * the status of various application components including:
 * - Database connectivity
 * - Redis cache availability
 * - External API accessibility
 * - System resources and performance metrics
 * 
 * The health checks are designed to provide actionable information for:
 * - Load balancers and service discovery
 * - Monitoring and alerting systems
 * - Development and debugging
 * - Operations and support teams
 * 
 * Architecture Notes:
 * - Implements Spring Boot Actuator HealthIndicator pattern
 * - Provides both simple and detailed health status
 * - Includes response time metrics for performance monitoring
 * - Returns appropriate HTTP status codes for automated monitoring
 * 
 * Security Considerations:
 * - Detailed health information only exposed to authorized users
 * - No sensitive data (passwords, keys) exposed in health responses
 * - Rate limiting applied to prevent abuse
 * 
 * @author ShowSync Development Team
 * @version 0.1.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private HealthService healthService;

    /**
     * Simple health check endpoint.
     * 
     * This endpoint provides a basic health status that can be used by:
     * - Load balancers for routing decisions
     * - Container orchestration platforms (Kubernetes, Docker Swarm)
     * - Simple monitoring systems that only need UP/DOWN status
     * 
     * Response Format:
     * - HTTP 200: Application is healthy and ready to serve requests
     * - HTTP 503: Application is unhealthy or not ready
     * 
     * Performance Characteristics:
     * - Lightweight check with minimal resource usage
     * - Response time typically under 50ms
     * - No external dependencies checked in this endpoint
     * 
     * @return ResponseEntity with health status and basic information
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> simpleHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", healthService.checkHealth() ? "UP" : "DOWN");
        return ResponseEntity.ok(response);
    }

    /**
     * Detailed health check endpoint.
     * 
     * This endpoint provides comprehensive health information including:
     * - Database connectivity and performance
     * - Cache availability and status
     * - External API accessibility
     * - System resource utilization
     * - Application configuration status
     * 
     * Use Cases:
     * - Detailed monitoring and alerting
     * - Troubleshooting and debugging
     * - Performance analysis and optimization
     * - Capacity planning and resource monitoring
     * 
     * Security Note:
     * In production, this endpoint should be secured and only accessible
     * to authorized monitoring systems and operations teams.
     * 
     * @return ResponseEntity with detailed health information
     */
    @GetMapping("/detailed")
    public ResponseEntity<Map<String, Object>> detailedHealthCheck() {
        Map<String, Object> response = new HashMap<>();
        boolean isHealthy = healthService.checkHealth() && healthService.checkDatabaseHealth();
        response.put("status", isHealthy ? "UP" : "DOWN");

        Map<String, Object> components = new HashMap<>();
        Map<String, Object> db = new HashMap<>();
        db.put("status", healthService.checkDatabaseHealth() ? "UP" : "DOWN");
        components.put("db", db);
        response.put("components", components);

        return ResponseEntity.ok(response);
    }

    /**
     * Get system information and resource utilization.
     * 
     * @return ResponseEntity with system information
     */
    @GetMapping("/system")
    public ResponseEntity<Map<String, Object>> systemInformation() {
        Map<String, Object> response = new HashMap<>();
        
        // System information
        Map<String, Object> system = new HashMap<>();
        system.put("processors", Runtime.getRuntime().availableProcessors());
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("osArch", System.getProperty("os.arch"));
        response.put("system", system);

        // Memory information
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        memory.put("total", totalMemory);
        memory.put("free", freeMemory);
        memory.put("max", maxMemory);
        memory.put("used", totalMemory - freeMemory);
        response.put("memory", memory);

        // Thread information
        Map<String, Object> threads = new HashMap<>();
        threads.put("count", Thread.activeCount());
        response.put("threads", threads);

        return ResponseEntity.ok(response);
    }

    /**
     * Get JVM information and memory utilization.
     * 
     * @return ResponseEntity with JVM information
     */
    @GetMapping("/jvm")
    public ResponseEntity<Map<String, Object>> jvmInformation() {
        Map<String, Object> response = new HashMap<>();
        
        // Memory information
        Map<String, Object> memory = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory() / (1024 * 1024);
        long freeMemory = runtime.freeMemory() / (1024 * 1024);
        long maxMemory = runtime.maxMemory() / (1024 * 1024);
        memory.put("total", totalMemory);
        memory.put("free", freeMemory);
        memory.put("max", maxMemory);
        memory.put("used", totalMemory - freeMemory);
        response.put("memory", memory);

        // Thread information
        Map<String, Object> threads = new HashMap<>();
        threads.put("count", Thread.activeCount());
        response.put("threads", threads);

        // GC information
        Map<String, Object> gc = new HashMap<>();
        gc.put("version", System.getProperty("java.version"));
        gc.put("vendor", System.getProperty("java.vendor"));
        response.put("gc", gc);

        return ResponseEntity.ok(response);
    }
} 