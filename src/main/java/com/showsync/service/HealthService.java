package com.showsync.service;

public interface HealthService {
    boolean checkHealth();
    boolean checkDatabaseHealth();
    boolean checkExternalApiHealth();
    boolean checkTmdbApiHealth();
    boolean checkOpenLibraryApiHealth();
} 