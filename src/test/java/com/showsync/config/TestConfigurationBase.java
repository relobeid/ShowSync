package com.showsync.config;

import com.showsync.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;

/**
 * Unified test configuration base class that provides all common test infrastructure.
 * 
 * This configuration combines:
 * - Spring Security setup for JWT authentication
 * - JPA Auditing configuration
 * - Common test beans and utilities
 * 
 * Usage: Add @Import(TestConfigurationBase.class) to any test class that needs
 * authentication, auditing, or other common test infrastructure.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@TestConfiguration
public class TestConfigurationBase {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Provides a test-specific auditor for JPA auditing.
     * Always returns "test-user" as the current auditor.
     */
    @Bean
    @Primary
    public AuditorAware<String> testAuditorProvider() {
        return () -> Optional.of("test-user");
    }

    /**
     * Authentication entry point for test security configuration.
     * Returns 401 Unauthorized for failed authentication attempts.
     */
    @Bean
    public AuthenticationEntryPoint testAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        };
    }

    /**
     * Test-specific security filter chain that mirrors production security rules
     * but is optimized for testing scenarios.
     * 
     * Key features:
     * - JWT authentication required for protected endpoints
     * - Public access to auth, public, health, and H2 console endpoints
     * - Role-based access control (USER/ADMIN roles)
     * - Stateless session management
     */
    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(testAuthenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints for testing
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                
                // Media endpoints - require authentication
                .requestMatchers(HttpMethod.GET, "/api/media/**").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/media/**").hasRole("USER")
                
                // Review endpoints - require authentication
                .requestMatchers("/api/reviews/**").hasRole("USER")
                
                // Library endpoints - require authentication  
                .requestMatchers("/api/library/**").hasRole("USER")
                
                // Group endpoints - require authentication
                .requestMatchers("/api/groups/**").hasRole("USER")
                
                // User profile endpoints - require authentication
                .requestMatchers("/api/user/**").hasRole("USER")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );
        
        // Add JWT filter to process authentication
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        // For H2 Console (development only)
        http.headers(headers -> headers.frameOptions().sameOrigin());
        
        return http.build();
    }
} 