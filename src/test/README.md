# Testing

Comprehensive test suite for ShowSync backend using JUnit 5, Spring Boot Test, and MockMvc.

## Test Structure

```
src/test/java/com/showsync/
├── config/                          # Configuration tests
│   ├── CacheConfigTest.java         # Cache configuration testing
│   ├── ExternalApiPropertiesTest.java # Properties binding tests
│   └── Test*Config.java             # Test-specific configurations
├── controller/                      # Integration tests
│   ├── AuthControllerTest.java      # Authentication endpoint tests
│   ├── GroupControllerTest.java     # Group management tests
│   └── *ControllerTest.java         # Other controller tests
├── repository/                      # Data layer tests
│   ├── UserRepositoryTest.java      # User repository tests
│   └── *RepositoryTest.java         # Other repository tests
├── service/                         # Business logic tests
│   ├── impl/                        # Service implementation tests
│   │   ├── GroupServiceImplTest.java
│   │   └── *ServiceImplTest.java
│   └── external/                    # External service tests
│       └── ExternalMediaServiceTest.java
└── exception/                       # Exception handling tests
```

## Test Categories

### Unit Tests
- Service layer business logic
- Utility classes and helpers
- Custom validation logic
- Isolated component testing

### Integration Tests
- Controller endpoints with MockMvc
- Repository with test database
- Service integration with mocked dependencies
- Full application context loading

### External Service Tests
- Mock external API responses
- Circuit breaker behavior
- Retry logic testing
- Timeout handling

## Test Configuration

### Base Test Setup
```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
class BaseIntegrationTest {
    @Autowired
    protected MockMvc mockMvc;
    
    @Autowired
    protected ObjectMapper objectMapper;
}
```

### Test Profiles
- `application-test.yml` - Test environment configuration
- H2 in-memory database for repository tests
- Disabled external service calls
- Mock security configuration

### Test Dependencies
- **JUnit 5** - Testing framework
- **Spring Boot Test** - Spring testing support
- **MockMvc** - Web layer testing
- **WireMock** - External service mocking
- **Testcontainers** - Database integration testing
- **AssertJ** - Fluent assertions

## Testing Patterns

### Controller Testing
```java
@Test
void shouldCreateUser() throws Exception {
    CreateUserRequest request = new CreateUserRequest("testuser", "test@example.com");
    
    mockMvc.perform(post("/api/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.username").value("testuser"));
}
```

### Service Testing
```java
@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;
    
    @InjectMocks
    private UserServiceImpl userService;
    
    @Test
    void shouldCreateUser() {
        // Given
        when(userRepository.save(any())).thenReturn(savedUser);
        
        // When
        User result = userService.createUser(request);
        
        // Then
        assertThat(result.getUsername()).isEqualTo("testuser");
    }
}
```

### Repository Testing
```java
@DataJpaTest
class UserRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    void shouldFindByUsername() {
        // Given
        User user = createTestUser();
        entityManager.persistAndFlush(user);
        
        // When
        Optional<User> found = userRepository.findByUsername("testuser");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("testuser");
    }
}
```

## Test Data Management

### Test Builders
```java
public class UserTestDataBuilder {
    public static User.UserBuilder defaultUser() {
        return User.builder()
                .username("testuser")
                .email("test@example.com")
                .displayName("Test User")
                .role(Role.USER)
                .isActive(true);
    }
}
```

### Database Cleanup
- `@Transactional` with rollback for tests
- `@DirtiesContext` for tests that modify application context
- Custom cleanup methods for complex scenarios

### Mock Data
- WireMock stubs for external API responses
- JSON response files in `src/test/resources`
- Reusable mock configurations

## Security Testing

### Authentication Tests
- JWT token generation and validation
- Login/logout functionality
- Password encryption verification
- Token expiration handling

### Authorization Tests
- Role-based access control
- Endpoint security configuration
- User permission validation
- Admin-only functionality

### Test Security Configuration
```java
@TestConfiguration
public class TestSecurityConfig {
    @Bean
    @Primary
    public SecurityFilterChain testFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }
}
```

## Performance Testing

### Load Testing
- High-volume request simulation
- Database performance under load
- Cache behavior verification
- External service resilience

### Memory Testing
- Memory leak detection
- Garbage collection monitoring
- Resource cleanup verification

## Test Execution

### Running Tests
```bash
# All tests
./mvnw test

# Specific test class
./mvnw test -Dtest=UserServiceTest

# Integration tests only
./mvnw test -Dtest="*IntegrationTest"

# Exclude slow tests
./mvnw test -Dtest="!*SlowTest"
```

### Coverage Reports
- JaCoCo integration for coverage metrics
- Minimum 80% coverage requirement
- Exclude configuration classes from coverage
- Generate HTML reports in `target/site/jacoco` 