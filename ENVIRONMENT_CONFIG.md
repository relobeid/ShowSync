# ShowSync Environment Configuration Guide

## Overview

ShowSync now supports multiple environment configurations to ensure proper separation between development, staging, and production environments. This guide explains how to configure and use each environment.

## Available Profiles

### 🛠️ Development (`dev`)
- **File**: `application-dev.yml`
- **Database**: H2 in-memory (default) or PostgreSQL (optional)
- **Cache**: In-memory fallback when Redis unavailable
- **Flyway**: Enabled with clean allowed
- **Logging**: Enhanced debugging, SQL logging enabled

**Usage:**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=dev
```

### 🧪 Staging (`staging`)
- **File**: `application-staging.yml`
- **Database**: PostgreSQL (separate from production)
- **Cache**: Redis (separate instance/database from production)
- **Flyway**: Enabled with clean allowed for testing
- **Logging**: Debug level with detailed error reporting

**Usage:**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=staging
```

### 🚀 Production (`prod`)
- **File**: `application-prod.yml`
- **Database**: PostgreSQL
- **Cache**: Redis
- **Flyway**: Enabled, clean disabled for safety
- **Logging**: INFO level, file logging enabled

**Usage:**
```bash
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

## Environment Variables

All sensitive configuration is externalized through environment variables:

### Required for Production/Staging
```bash
# Database
DB_URL=jdbc:postgresql://your-host:5432/showsync_prod
DB_USERNAME=showsync_user
DB_PASSWORD=your-secure-password

# Redis
REDIS_HOST=your-redis-host
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password

# Security (JWT Authentication)
# IMPORTANT: Use different secrets for each environment!
# Generate new secrets with: openssl rand -base64 32

# FOR ANY ENVIRONMENT - Generate your own secret:
JWT_SECRET=<paste-your-generated-secret-here>

# Example generation:
# $ openssl rand -base64 32
# abc123XYZ789... (use the output here)

# External APIs
TMDB_API_KEY=your-tmdb-api-key
```

### Optional
```bash
# Server
SERVER_PORT=8080

# JWT Expiration
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Logging
LOG_FILE_PATH=logs/showsync.log
```

## Configuration Changes Made

### ✅ Development Environment Improvements
1. **Enabled Flyway**: Now uses proper database migrations instead of JPA auto-DDL
2. **PostgreSQL Support**: Added option to use PostgreSQL in development
3. **Schema Validation**: Changed from `create-drop` to `validate` for consistency

### ✅ Production Configuration
1. **Environment Variables**: All secrets externalized
2. **Connection Pooling**: Optimized Hikari settings
3. **Security**: Minimal error exposure, secure headers
4. **Caching**: Production-ready Redis configuration
5. **Monitoring**: Limited actuator endpoints for security

### ✅ Staging Configuration
1. **Debug Logging**: Enhanced logging for troubleshooting
2. **Separate Infrastructure**: Different database/Redis instances
3. **Testing Features**: More permissive settings for testing
4. **Full Monitoring**: All actuator endpoints available

## Migration Notes

### From Previous Setup
1. **Flyway**: Development now uses migrations (previously disabled)
2. **Database**: Development can now use PostgreSQL (previously H2 only)
3. **Configuration**: Secrets moved to environment variables

### Database Migrations
The following migrations are available:
- `V1__initial_schema.sql` - Initial database schema
- `V2__add_authentication_fields.sql` - User authentication
- `V3__add_review_system.sql` - Review and rating system
- `V4__add_group_system.sql` - Group management
- `V5__add_group_media_activities.sql` - Group activities
- `V6__fix_jsonb_compatibility.sql` - H2/PostgreSQL compatibility

## Quick Start

### Development (Default)
```bash
# Start with H2 database
./mvnw spring-boot:run

# Start with PostgreSQL (requires Docker)
docker-compose up -d postgres redis
./mvnw spring-boot:run
```

### Staging
```bash
# Set environment variables
export DB_URL=jdbc:postgresql://staging-host:5432/showsync_staging
export DB_USERNAME=staging_user
export DB_PASSWORD=staging_password
export JWT_SECRET=your-staging-secret
export TMDB_API_KEY=your-api-key

# Run staging profile
./mvnw spring-boot:run -Dspring.profiles.active=staging
```

### Production
```bash
# Set all required environment variables
export DB_URL=jdbc:postgresql://prod-host:5432/showsync_prod
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_prod_password
export REDIS_HOST=prod-redis-host
export REDIS_PASSWORD=secure_redis_password
export JWT_SECRET=your-production-secret
export TMDB_API_KEY=your-api-key

# Run production profile
./mvnw spring-boot:run -Dspring.profiles.active=prod
```

## Security Considerations

### Production Security
- All secrets in environment variables
- Minimal error information exposure
- Limited actuator endpoints
- Secure JWT configuration
- Connection pool protection

### Development Security
- Safe defaults for local development
- Optional PostgreSQL for production-like testing
- H2 console available for debugging

## Monitoring

### Health Checks
- Basic: `/actuator/health`
- Detailed: `/actuator/health` (staging/dev only)

### Metrics
- Prometheus: `/actuator/prometheus`
- Application metrics: `/actuator/metrics`

### Logging
- Development: Console + detailed SQL logging
- Staging: File + console with debug information
- Production: File logging with rotation

## Troubleshooting

### Common Issues
1. **Database Connection**: Ensure environment variables are set
2. **Flyway Errors**: Check migration files and database permissions
3. **Redis Connection**: Verify Redis is running and accessible
4. **JWT Errors**: Ensure JWT_SECRET is properly base64 encoded

## JWT Secret Management

### Generating Secure Secrets
Always generate cryptographically secure secrets for each environment:
```bash
# Generate a new 256-bit base64-encoded secret
openssl rand -base64 32
```

### Environment-Specific Secrets
- **Development**: Use the provided development secret for local testing
- **Staging**: Use a separate secret for staging environment 
- **Production**: Use a unique secret for production (never reuse staging/dev secrets)
- **Testing**: Use the test-specific secret for unit tests

### Secret Rotation Procedures
1. **Generate new secret**: `openssl rand -base64 32`
2. **Update environment variables** in deployment systems
3. **Restart application** to load new secret
4. **Verify authentication** still works
5. **Monitor logs** for any JWT validation errors

### Security Best Practices
- Rotate JWT secrets every 3-6 months
- Never commit secrets to version control
- Use different secrets for each environment
- Store secrets securely (environment variables, secret managers)
- Monitor for exposed secrets in logs or error messages
- Use 256-bit keys minimum for production

### Debugging
```bash
# Check configuration
./mvnw spring-boot:run -Dspring.profiles.active=dev -Ddebug

# Check environment variables
./mvnw spring-boot:run -Dspring.profiles.active=staging --logging.level.org.springframework.boot.context.config=DEBUG
``` 