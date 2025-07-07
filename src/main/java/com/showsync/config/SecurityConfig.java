package com.showsync.config;

import com.showsync.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private Environment environment;
    
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:5173}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(authenticationEntryPoint()))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/health").permitAll()  // Basic health only
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**", "/v3/api-docs").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                
                // Media endpoints - require authentication
                .requestMatchers(HttpMethod.GET, "/api/media/**").hasRole("USER")
                .requestMatchers(HttpMethod.POST, "/api/media/**").hasRole("USER")
                
                // Review endpoints - require authentication
                .requestMatchers("/api/reviews/**").hasRole("USER")
                
                // Library endpoints - require authentication  
                .requestMatchers("/api/library/**").hasRole("USER")
                
                // Trending endpoint - public access
                .requestMatchers(HttpMethod.GET, "/api/trending").permitAll()
                
                // Group endpoints - require authentication
                .requestMatchers("/api/groups/**").hasRole("USER")
                
                // User profile endpoints - require authentication
                .requestMatchers("/api/user/**").hasRole("USER")
                
                // Admin endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            );

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // For H2 Console (development only)
        http.headers(headers -> headers.frameOptions().sameOrigin());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Environment-specific CORS configuration
        List<String> origins = getEnvironmentSpecificOrigins();
        configuration.setAllowedOrigins(origins);
        
        // Allowed methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        
        // Allowed headers
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Requested-With",
            "Accept",
            "Origin"
        ));
        
        // Expose headers that client needs to access
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        
        // Allow credentials only for trusted origins
        configuration.setAllowCredentials(true);
        
        // Preflight cache duration (24 hours)
        configuration.setMaxAge(86400L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Get environment-specific allowed origins for CORS.
     * Development: Local development servers
     * Production: Only production frontend URLs
     */
    private List<String> getEnvironmentSpecificOrigins() {
        String[] profiles = environment.getActiveProfiles();
        
        // Production: Use only configured production origins
        if (Arrays.asList(profiles).contains("prod")) {
            String prodOrigins = System.getenv("CORS_ALLOWED_ORIGINS");
            if (prodOrigins != null && !prodOrigins.trim().isEmpty()) {
                return Arrays.asList(prodOrigins.split(","));
            }
            // Fallback for production - NO wildcards!
            return Arrays.asList("https://showsync.app", "https://www.showsync.app");
        }
        
        // Staging: Staging-specific origins
        if (Arrays.asList(profiles).contains("staging")) {
            return Arrays.asList(
                "https://staging.showsync.app",
                "http://localhost:3000",
                "http://localhost:5173"
            );
        }
        
        // Development: Local development origins only
        return Arrays.asList(allowedOrigins.split(","));
    }
} 