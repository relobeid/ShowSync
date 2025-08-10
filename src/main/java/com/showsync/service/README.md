# Services

Business logic layer for ShowSync backend. All services follow interface-based design for testability and maintainability.

## Structure

| Service | Implementation | Purpose |
|---------|----------------|---------|
| `AuthService` | - | User authentication and JWT management |
| `ExternalMediaService` | `ExternalMediaServiceImpl` | External API integration (TMDb, OpenLibrary) |
| `UserMediaLibraryService` | `UserMediaLibraryServiceImpl` | User's personal media library management |
| `ReviewService` | `ReviewServiceImpl` | Media reviews and ratings |
| `GroupService` | `GroupServiceImpl` | Group management and membership |
| `GroupMediaService` | `GroupMediaServiceImpl` | Group media activities and voting |
| `HealthService` | `HealthServiceImpl` | System health monitoring |
| `UserDetailsServiceImpl` | - | Spring Security user details implementation |
| `RecommendationService` | `RecommendationServiceImpl` | AI recommendation engine |
| `UserPreferenceService` | `UserPreferenceServiceImpl` | User taste profile management |

## Core Services

### AuthService
- User registration and login
- JWT token generation and validation
- Password encryption and verification
- User profile management

### ExternalMediaService
- TMDb API integration for movies and TV shows
- OpenLibrary API integration for books
- Response caching and rate limiting
- Circuit breaker pattern for resilience
- Automatic retry logic with exponential backoff

### UserMediaLibraryService
- Add/remove media from user's library
- Update ratings, status, and progress
- Manage favorites and reviews
- Library filtering and search

### GroupService
- Create and manage groups
- Handle membership requests
- Group privacy and permissions
- Member role management

### GroupMediaService
- Group media list management
- Voting system for group decisions
- Activity tracking and feeds
- Statistics and analytics

## Design Patterns

### Interface-Based Design
```java
public interface ServiceName {
    // Method definitions
}

@Service
public class ServiceNameImpl implements ServiceName {
    // Implementation
}
```

### Dependency Injection
- Constructor injection preferred
- Use `@RequiredArgsConstructor` from Lombok
- Inject interfaces, not implementations

### Transaction Management
- Use `@Transactional` for data modifications
- Read-only transactions for queries
- Proper rollback configuration

### Error Handling
- Custom exceptions for business logic errors
- Proper logging at appropriate levels
- Graceful degradation for external service failures

### Caching
- Method-level caching with `@Cacheable`
- Cache eviction with `@CacheEvict`
- TTL configuration per cache type

## External Service Integration
## AI Recommendations Services

### RecommendationService
- Personal, trending, real-time recommendations
- Group suggestions and group content generation
- Analytics, insights, and summary endpoints
- Batch generation (all users) and active-user refresh

### UserPreferenceService
- Build/update per-user preference profiles
- Compute compatibility, diversity, confidence
- Batch maintenance (active users, low-confidence refresh, cleanup)

### Scheduling
- Daily generation and hourly refresh controlled by `RecommendationConfig`

### Caching
- Redis caches for trending, analytics, insights, and preference maps

### Resilience Patterns
- Circuit breaker for external API failures
- Retry with exponential backoff
- Timeout configuration
- Fallback responses

### Rate Limiting
- TMDb API: 40 requests/minute
- OpenLibrary API: 100 requests/minute
- Per-user rate limiting where applicable

### Response Handling
- Reactive programming with WebClient
- Non-blocking I/O operations
- Proper error propagation 