# ShowSync Backend

This directory contains the Spring Boot backend application for ShowSync.

## Structure

```
src/
├── main/java/com/showsync/          # Application source code
│   ├── config/                      # Configuration classes
│   ├── controller/                  # REST API controllers
│   ├── dto/                         # Data Transfer Objects
│   ├── entity/                      # JPA entities
│   ├── exception/                   # Exception handling
│   ├── repository/                  # Data access layer
│   ├── security/                    # Authentication & authorization
│   ├── service/                     # Business logic layer
│   └── ShowSyncApplication.java     # Main application class
├── main/resources/                  # Configuration files and migrations
│   ├── application.yml              # Main configuration
│   ├── application-*.yml            # Environment-specific configs
│   └── db/migration/                # Flyway database migrations
└── test/java/                       # Test classes
```

## Key Components

| Package | Purpose | Key Classes |
|---------|---------|-------------|
| `config` | Application configuration | `SecurityConfig`, `WebClientConfig`, `CacheConfig` |
| `controller` | REST API endpoints | `AuthController`, `MediaSearchController`, `GroupController` |
| `service` | Business logic | `AuthService`, `ExternalMediaService`, `GroupService` |
| `entity` | Database models | `User`, `Media`, `Group`, `Review` |
| `repository` | Data access | `UserRepository`, `MediaRepository`, `GroupRepository` |
| `security` | Authentication/Authorization | `JwtUtil`, `JwtAuthenticationFilter` |

## Development Guidelines

- Follow Spring Boot conventions
- Use interface-based service design
- Implement proper exception handling
- Write unit tests for business logic
- Document public APIs with OpenAPI annotations

## Getting Started

1. Start the application: `./mvnw spring-boot:run`
2. Access API docs: `http://localhost:8080/swagger-ui.html`
3. Health check: `http://localhost:8080/actuator/health`

## Testing

Run all tests: `./mvnw test`
Run specific test class: `./mvnw test -Dtest=ClassName` 