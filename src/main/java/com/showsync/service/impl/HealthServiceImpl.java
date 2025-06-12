package com.showsync.service.impl;

import com.showsync.service.HealthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;

@Service
public class HealthServiceImpl implements HealthService {

    @Autowired
    private DataSource dataSource;

    @Override
    public boolean checkHealth() {
        return true; // Basic health check always returns true
    }

    @Override
    public boolean checkDatabaseHealth() {
        try (Connection connection = dataSource.getConnection()) {
            return connection.isValid(5);
        } catch (Exception e) {
            return false;
        }
    }
} 