# Configuration

Spring Boot configuration classes for ShowSync backend. Handles security, external services, caching, and application setup.

## Configuration Classes

| Class | Purpose | Key Beans |
|-------|---------|-----------|
| `SecurityConfig` | Authentication and authorization | `SecurityFilterChain`, `PasswordEncoder` |
| `WebClientConfig` | HTTP clients for external APIs | `tmdbWebClient`, `openLibraryWebClient` |
| `CacheConfig` | Redis caching configuration | `CacheManager`, `RedisTemplate` |
| `RecommendationConfig` | AI recommendations configuration | `@ConfigurationProperties` for tuning |
| `SchedulingConfig` | Enables Spring scheduling | `@EnableScheduling` |
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
- `recommendations` - Per-user recommendation lists
- `trendingRecommendations` - Trending feed cache (6h)
- `recommendationAnalytics` - Aggregated analytics (6h)
- `userRecommendationInsights` - Per-user insights (1h)
- `userCompatibility` - Compatibility scores (12h)
- `userGenrePreferences`, `userPlatformPreferences`, `userEraPreferences` - Derived preferences (6h)

## AI Recommendations Configuration

### RecommendationConfig
Type-safe configuration for the AI recommendation system (prefix: `showsync.recommendations`). Key knobs:

- General: `enabled`, `max-recommendations-per-user`, `default-batch-size`
- Weights (must sum to 1.0): `genre-weight`, `rating-weight`, `platform-weight`, `era-weight`
- Thresholds: `min-confidence-threshold`, `min-interactions-for-recommendations`
- Expiration: `content-recommendation-expiry`, `group-recommendation-expiry`
- Algorithm: `collaborative-filtering-user-count`, `min-similarity-score`, `time-decay-factor`
- Personalization: `personalization-balance`, `diversity-factor`, `exploration-factor`
- Feedback: `positive-feedback-weight`, `negative-feedback-weight`, `feedback-learning-rate`
- Performance: `enable-caching`, `cache-expiry`, `async-generation`, `async-thread-pool-size`
- Feature flags: `features.*` (personal, group, trending, collaborative, content-based, seasonal)

Example:
```yaml
showsync:
  recommendations:
    enabled: true
    max-recommendations-per-user: 20
    genre-weight: 0.4
    rating-weight: 0.3
    platform-weight: 0.2
    era-weight: 0.1
    enable-schedulers: true
```

## Scheduling

### SchedulingConfig & properties
- Enabled via `@EnableScheduling`
- Controlled by `RecommendationConfig`:
  - `enable-schedulers` (default: true)
  - `daily-generation-cron` (default: `0 15 3 * * *`)
  - `active-users-refresh-cron` (default: `0 10 * * * *`)
  - `active-users-hours-back` (default: 24)

Scheduled tasks (in `RecommendationScheduler`):
- Daily generation for all users
- Hourly refresh for recently active users

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