package com.showsync.config;

import com.showsync.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletResponse;

@TestConfiguration
public class TestSecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public AuthenticationEntryPoint testAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        };
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf().disable()
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
                
                // Review endpoints - require authentication (this is critical for the failing tests)
                .requestMatchers("/api/reviews/**").hasRole("USER")
                
                // Library endpoints - require authentication  
                .requestMatchers("/api/library/**").hasRole("USER")
                
                // Group endpoints - require authentication
                .requestMatchers("/api/groups/**").hasRole("USER")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );
        
        // Add JWT filter to process authentication
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
} 