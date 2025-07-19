# Configuration

Spring Boot configuration classes for ShowSync backend. Handles security, external services, caching, and application setup.

## Configuration Classes

| Class | Purpose | Key Beans |
|-------|---------|-----------|
| `SecurityConfig` | Authentication and authorization | `SecurityFilterChain`, `PasswordEncoder` |
| `WebClientConfig` | HTTP clients for external APIs | `tmdbWebClient`, `openLibraryWebClient` |
| `CacheConfig` | Redis caching configuration | `CacheManager`, `RedisTemplate` |
| `ExternalApiProperties` | External API settings | Configuration properties binding |
| `OpenApiConfig` | Swagger/OpenAPI documentation | `OpenAPI`, `GroupedOpenApi` |

## Security Configuration

### SecurityConfig
- JWT-based authentication
- Public endpoints for auth and health
- Protected endpoints require `ROLE_USER`
- CORS configuration for frontend integration
- Session management (stateless)

```java
// Public endpoints
"/api/auth/**", "/actuator/health"

// Protected endpoints  
"/api/**" requires ROLE_USER
```

### Authentication Flow
1. User authenticates via `/api/auth/login`
2. JWT token returned in response
3. Token included in `Authorization: Bearer {token}` header
4. `JwtAuthenticationFilter` validates token on each request

## External API Configuration

### WebClientConfig
- TMDb API client with authentication headers
- OpenLibrary API client with User-Agent
- Timeout and retry configuration
- Request/response logging

### ExternalApiProperties
Type-safe configuration binding for:
- API base URLs
- Timeout settings
- Rate limiting parameters
- Cache TTL values

Configuration example:
```yaml
external-apis:
  tmdb:
    baseUrl: https://api.themoviedb.org/3
    apiKey: ${TMDB_API_KEY}
    timeout: 10000
    rateLimit: 40
```

## Caching Configuration

### CacheConfig
- Redis integration for distributed caching
- Cache TTL configuration
- Cache key strategies
- Serialization configuration

### Cache Names
- `external-api-responses` - External API response caching
- `user-sessions` - User session data
- `media-details` - Media information caching

## Development vs Production

### Profile-Specific Configuration
- `application-dev.yml` - Development settings
- `application-prod.yml` - Production settings
- `application-test.yml` - Test environment

### Environment Variables
Required for production:
- `TMDB_API_KEY` - TMDb API authentication
- `JWT_SECRET` - JWT signing secret
- `REDIS_URL` - Redis connection string
- `DATABASE_URL` - PostgreSQL connection

## Monitoring and Documentation

### OpenApiConfig
- Swagger UI available at `/swagger-ui.html`
- API documentation at `/v3/api-docs`
- Grouped endpoints by functionality
- Authentication configuration for testing

### Health Checks
- Basic health: `/actuator/health`
- Detailed health: `/actuator/health/detailed` (authenticated)
- Database connectivity checks
- External service health monitoring 