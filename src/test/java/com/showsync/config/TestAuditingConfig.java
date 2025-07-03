package com.showsync.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

/**
 * Lightweight test configuration for @DataJpaTest scenarios.
 * 
 * This configuration only provides JPA auditing support without 
 * any security components, making it compatible with @DataJpaTest.
 * 
 * @author ShowSync Development Team
 * @version 1.0
 * @since 2024-12-16
 */
@TestConfiguration
public class TestAuditingConfig {

    /**
     * Provides a test-specific auditor for JPA auditing.
     * Always returns "test-user" as the current auditor.
     */
    @Bean
    @Primary
    public AuditorAware<String> testAuditorProvider() {
        return () -> Optional.of("test-user");
    }
} 