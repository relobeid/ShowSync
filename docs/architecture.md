# ShowSync Architecture & Design Document

## System Overview

ShowSync is designed as a scalable, microservice-ready application that combines social media functionality with AI-powered recommendations. The architecture emphasizes modularity, testability, and future extensibility.

## High-Level Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   API Gateway   │    │   Load Balancer │
│   (Next.js)     │◄──►│   (Optional)    │◄──►│                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                  │
                        ┌─────────┼─────────┐
                        │         │         │
              ┌─────────▼───┐ ┌───▼────┐ ┌──▼─────────┐
              │ Auth Service│ │Core API│ │Real-time   │
              │(Spring Boot)│ │Service │ │Service     │
              └─────────────┘ └────────┘ └────────────┘
                        │         │         │
                ┌───────┼─────────┼─────────┼───────┐
                │       │         │         │       │
         ┌──────▼─┐ ┌───▼──┐ ┌────▼───┐ ┌──▼───┐ ┌─▼──────┐
         │PostgreSQL│ │Redis │ │External│ │Queue │ │AI/ML   │
         │Database  │ │Cache │ │APIs    │ │System│ │Service │
         └──────────┘ └──────┘ └────────┘ └──────┘ └────────┘
```

## Core Components

### 1. Data Layer Architecture

#### Entity Relationship Design
```
User ────┬──── Group Membership ──── Group
│        │                            │
│        └──── User Media ────────────┼──── Media
│                  │                  │
│                  │                  │
└──── User Preference Profile         └──── Group Media Activity
                  │
                  │
            Recommendation Engine
```

#### Database Schema Strategy
- **Users**: Core user data, preferences, authentication
- **Media**: Unified media table for movies/TV/books with type discrimination
- **Groups**: Group metadata, settings, and statistics
- **Interactions**: User-media interactions (ratings, status, reviews)
- **Group Activities**: Time-series data for group interactions
- **Recommendations**: Cached recommendation results with explanations

### 2. Service Layer Architecture

#### Core Services
```java
@Service
public class UserService {
    // User management, preferences, social features
}

@Service
public class MediaService {
    // External API integration, media metadata, search
}

@Service
public class GroupService {
    // Group management, member operations, group activities
}

@Service
public class RecommendationService {
    // AI-powered matching, content recommendations
}

@Service
public class NotificationService {
    // Real-time notifications, email alerts, push notifications
}
```

#### Repository Pattern
```java
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    @Query("SELECT u FROM User u WHERE u.preferences.genres OVERLAP :genres")
    List<User> findByGenrePreferences(@Param("genres") Set<String> genres);
}
```

### 3. AI & Recommendation System Architecture

#### User Preference Modeling
```java
@Entity
public class UserPreferenceProfile {
    private Long userId;
    private Map<String, Double> genreScores;        // Genre preferences (0.0-1.0)
    private Map<String, Double> platformScores;     // Platform preferences
    private Map<String, Double> yearScores;         // Era preferences
    private Double averageRating;                   // Rating tendency
    private String preferredContentLength;          // Short/medium/long content
    private LocalDateTime lastUpdated;
    
    // Calculated fields
    private String primaryGenres;                   // Top 3 genres
    private String viewingPersonality;              // Calculated personality type
}
```

#### Group Matching Algorithm
```java
@Component
public class GroupMatchingEngine {
    
    public List<GroupRecommendation> findCompatibleGroups(User user) {
        // 1. Calculate user preference vector
        UserVector userVector = calculateUserVector(user);
        
        // 2. Find groups with compatible preferences
        List<Group> candidateGroups = findCandidateGroups(userVector);
        
        // 3. Score groups based on multiple factors
        return candidateGroups.stream()
            .map(group -> scoreGroupCompatibility(user, group))
            .sorted(Comparator.comparing(GroupRecommendation::getScore).reversed())
            .collect(Collectors.toList());
    }
    
    private double scoreGroupCompatibility(User user, Group group) {
        double preferenceAlignment = calculatePreferenceAlignment(user, group);
        double activityLevel = calculateActivityLevel(group);
        double socialFit = calculateSocialFit(user, group);
        double contentFreshness = calculateContentFreshness(group);
        
        return (preferenceAlignment * 0.4) + 
               (activityLevel * 0.2) + 
               (socialFit * 0.3) + 
               (contentFreshness * 0.1);
    }
}
```

#### Content Recommendation Logic
```java
@Component
public class ContentRecommendationEngine {
    
    public List<MediaRecommendation> recommendForGroup(Group group) {
        // 1. Analyze group's collective preferences
        GroupPreferenceProfile groupProfile = analyzeGroupPreferences(group);
        
        // 2. Find content that matches group preferences
        List<Media> candidates = findCandidateContent(groupProfile);
        
        // 3. Filter out already consumed content
        candidates = filterConsumedContent(candidates, group);
        
        // 4. Score and rank recommendations
        return scoreAndRankContent(candidates, groupProfile);
    }
    
    private GroupPreferenceProfile analyzeGroupPreferences(Group group) {
        List<User> members = group.getMembers();
        
        // Aggregate member preferences using weighted averaging
        // More active members have higher weight
        Map<String, Double> aggregatedGenres = aggregateGenrePreferences(members);
        Map<String, Double> aggregatedPlatforms = aggregatePlatformPreferences(members);
        
        return new GroupPreferenceProfile(aggregatedGenres, aggregatedPlatforms);
    }
}
```

## Performance & Scalability Considerations

### 1. Caching Strategy
```java
@Service
@CacheConfig(cacheNames = "showsync")
public class CachedRecommendationService {
    
    @Cacheable(key = "'user:' + #userId + ':groups'", unless = "#result.isEmpty()")
    public List<GroupRecommendation> getUserGroupRecommendations(Long userId) {
        return recommendationEngine.findCompatibleGroups(userService.findById(userId));
    }
    
    @Cacheable(key = "'group:' + #groupId + ':content'", unless = "#result.isEmpty()")
    public List<MediaRecommendation> getGroupContentRecommendations(Long groupId) {
        return recommendationEngine.recommendForGroup(groupService.findById(groupId));
    }
    
    @CacheEvict(key = "'user:' + #userId + ':groups'")
    public void invalidateUserRecommendations(Long userId) {
        // Called when user preferences change
    }
}
```

### 2. Database Optimization
```sql
-- Optimized indexes for common queries
CREATE INDEX idx_user_media_user_id_status ON user_media(user_id, status);
CREATE INDEX idx_group_members_group_id_active ON group_members(group_id, is_active);
CREATE INDEX idx_media_genre_type ON media USING GIN(genres) WHERE type = ?;
CREATE INDEX idx_user_preferences_updated ON user_preference_profiles(last_updated);

-- Partitioning for time-series data
CREATE TABLE group_activities (
    id BIGSERIAL,
    group_id BIGINT,
    activity_type VARCHAR(50),
    created_at TIMESTAMP,
    data TEXT -- JSON data stored as TEXT for H2/PostgreSQL compatibility
) PARTITION BY RANGE (created_at);
```

### 3. Real-time Features Architecture
```java
@Component
public class WebSocketNotificationHandler {
    
    @MessageMapping("/group/{groupId}/join")
    public void handleGroupJoin(@DestinationVariable Long groupId, 
                               @Payload GroupJoinMessage message,
                               SimpMessageHeaderAccessor headerAccessor) {
        
        // Authenticate user
        User user = authService.getUserFromSession(headerAccessor);
        
        // Add user to group WebSocket session
        messagingTemplate.convertAndSend(
            "/topic/group/" + groupId + "/members", 
            new MemberJoinedEvent(user.getId(), user.getDisplayName())
        );
        
        // Update group activity
        groupActivityService.recordActivity(groupId, "MEMBER_JOINED", user.getId());
    }
}
```

## Security Architecture

### 1. Authentication & Authorization
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/media/**").hasRole("USER")
                .requestMatchers("/api/groups/**").hasRole("USER")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
            .build();
    }
}
```

### 2. Data Privacy & GDPR Compliance
```java
@Entity
public class UserDataRetention {
    private Long userId;
    private LocalDateTime dataRetentionUntil;
    private Set<String> dataCategories;
    private boolean hasOptedOutOfRecommendations;
    private String gdprConsentVersion;
}

@Service
public class DataPrivacyService {
    
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupExpiredData() {
        List<UserDataRetention> expiredRetentions = 
            dataRetentionRepository.findExpiredRetentions(LocalDateTime.now());
            
        expiredRetentions.forEach(this::anonymizeUserData);
    }
    
    public void exportUserData(Long userId) throws IOException {
        // Generate GDPR-compliant data export
        UserDataExport export = createUserDataExport(userId);
        generateExportFile(export);
    }
}
```

## Testing Strategy

### 1. Unit Testing Approach
```java
@ExtendWith(MockitoExtension.class)
class GroupMatchingEngineTest {
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private GroupRepository groupRepository;
    
    @InjectMocks
    private GroupMatchingEngine groupMatchingEngine;
    
    @Test
    void shouldRecommendCompatibleGroups() {
        // Given
        User user = createTestUser();
        List<Group> candidateGroups = createTestGroups();
        
        when(groupRepository.findActiveGroups()).thenReturn(candidateGroups);
        
        // When
        List<GroupRecommendation> recommendations = 
            groupMatchingEngine.findCompatibleGroups(user);
        
        // Then
        assertThat(recommendations).hasSize(3);
        assertThat(recommendations.get(0).getScore()).isGreaterThan(0.7);
    }
}
```

### 2. Integration Testing
```java
@SpringBootTest
@Testcontainers
class GroupRecommendationIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("showsync_test")
            .withUsername("test")
            .withPassword("test");
    
    @Test
    void shouldProvidePersonalizedGroupRecommendations() {
        // End-to-end test of recommendation flow
    }
}
```

## Monitoring & Observability

### 1. Application Metrics
```java
@Component
public class RecommendationMetrics {
    
    private final Counter recommendationRequests;
    private final Timer recommendationLatency;
    private final Gauge activeGroups;
    
    public RecommendationMetrics(MeterRegistry meterRegistry) {
        this.recommendationRequests = Counter.builder("showsync.recommendations.requests")
            .description("Number of recommendation requests")
            .register(meterRegistry);
            
        this.recommendationLatency = Timer.builder("showsync.recommendations.latency")
            .description("Recommendation generation latency")
            .register(meterRegistry);
    }
    
    @EventListener
    public void handleRecommendationRequest(RecommendationRequestEvent event) {
        recommendationRequests.increment(
            Tags.of("type", event.getType(), "user_id", event.getUserId().toString())
        );
    }
}
```

### 2. Health Checks
```java
@Component
public class ShowSyncHealthIndicator implements HealthIndicator {
    
    @Override
    public Health health() {
        boolean databaseHealthy = checkDatabaseConnectivity();
        boolean redisHealthy = checkRedisConnectivity();
        boolean externalApisHealthy = checkExternalApiConnectivity();
        
        if (databaseHealthy && redisHealthy && externalApisHealthy) {
            return Health.up()
                .withDetail("database", "UP")
                .withDetail("cache", "UP")
                .withDetail("external_apis", "UP")
                .build();
        } else {
            return Health.down()
                .withDetail("database", databaseHealthy ? "UP" : "DOWN")
                .withDetail("cache", redisHealthy ? "UP" : "DOWN")
                .withDetail("external_apis", externalApisHealthy ? "UP" : "DOWN")
                .build();
        }
    }
}
```

## Future Architecture Considerations

### 1. Microservices Migration Path
- **Phase 1**: Extract recommendation service
- **Phase 2**: Separate user service and group service
- **Phase 3**: Split media service and notification service
- **Phase 4**: Implement service mesh with Istio

### 2. Machine Learning Enhancement
- Implement recommendation model training pipeline
- Add A/B testing framework for recommendation algorithms
- Integrate with MLflow for model versioning and monitoring
- Consider real-time model serving with TensorFlow Serving

### 3. Global Scale Considerations
- Implement multi-region deployment
- Add CDN for static content and API responses
- Consider event sourcing for audit trails
- Plan for data locality and GDPR compliance across regions

---

## Development Best Practices

1. **Code Organization**: Follow hexagonal architecture principles
2. **API Design**: RESTful APIs with OpenAPI 3.0 documentation
3. **Error Handling**: Consistent error responses with problem details
4. **Logging**: Structured logging with correlation IDs
5. **Documentation**: Keep architecture decisions recorded (ADRs)
6. **Performance**: Monitor and optimize database queries regularly
7. **Security**: Regular security audits and dependency updates 