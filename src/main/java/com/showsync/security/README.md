# Security

JWT-based authentication and authorization system for ShowSync backend. Implements stateless security with role-based access control.

## Security Components

| Class | Purpose | Responsibility |
|-------|---------|----------------|
| `JwtUtil` | JWT token management | Token generation, validation, parsing |
| `JwtAuthenticationFilter` | Request authentication | Token extraction and validation |
| `UserPrincipal` | User details implementation | Spring Security user representation |
| `UserDetailsServiceImpl` | User loading | Load user details for authentication |

## Authentication Flow

### 1. User Registration/Login
```
1. User submits credentials to /api/auth/login
2. AuthService validates credentials
3. JwtUtil generates JWT token
4. Token returned to client
```

### 2. Request Authentication
```
1. Client includes token in Authorization header
2. JwtAuthenticationFilter extracts token
3. JwtUtil validates token signature and expiration
4. UserDetailsService loads user details
5. Authentication context set for request
```

### 3. Authorization
```
1. Spring Security checks required roles/permissions
2. Access granted/denied based on user authorities
3. Controller methods execute with security context
```

## JWT Token Structure

### Token Claims
```json
{
  "sub": "username",
  "userId": 123,
  "role": "USER",
  "iat": 1642781600,
  "exp": 1642868000
}
```

### Token Configuration
- **Algorithm**: HS384 (HMAC SHA-384)
- **Expiration**: 24 hours
- **Secret**: Environment variable `JWT_SECRET`
- **Header**: `Authorization: Bearer {token}`

## Security Classes

### JwtUtil
Handles all JWT operations:
- `generateToken(User user)` - Create JWT for authenticated user
- `extractUsername(String token)` - Get username from token
- `extractClaim(String token, Function<Claims, T> claimsResolver)` - Extract specific claim
- `isTokenValid(String token, UserDetails userDetails)` - Validate token
- `isTokenExpired(String token)` - Check expiration

### JwtAuthenticationFilter
Servlet filter that processes JWT authentication:
- Extracts JWT from Authorization header
- Validates token using JwtUtil
- Loads user details if token is valid
- Sets authentication in SecurityContextHolder
- Continues filter chain

### UserPrincipal
Implementation of Spring Security's UserDetails:
- Wraps User entity
- Provides authorities based on user role
- Handles account status (enabled, expired, locked)
- Maps user roles to Spring Security authorities

### UserDetailsServiceImpl
Spring Security service for loading user details:
- Implements `UserDetailsService` interface
- Loads user by username from database
- Returns UserPrincipal instance
- Throws `UsernameNotFoundException` if user not found

## Role-Based Access Control

### User Roles
```java
public enum Role {
    USER,    // Regular user access
    ADMIN    // Administrative access
}
```

### Authority Mapping
- `USER` role → `ROLE_USER` authority
- `ADMIN` role → `ROLE_ADMIN` authority

### Endpoint Security
```java
// Public endpoints (no authentication)
"/api/auth/**"
"/actuator/health"

// Protected endpoints (requires ROLE_USER)
"/api/**"

// Admin endpoints (requires ROLE_ADMIN)
"/api/admin/**"
```

## Security Configuration

### Password Encoding
- Uses BCrypt with strength 12
- Salted hashing for password storage
- Automatic password verification

### Session Management
- Stateless session creation policy
- No server-side session storage
- JWT tokens carry all necessary information

### CORS Configuration
- Configured for frontend integration
- Allows credentials for authenticated requests
- Specific origin allowlist for production

## Security Best Practices

### Token Security
- Tokens stored securely on client side
- Short expiration times (24 hours)
- Secure token transmission (HTTPS only)
- Token rotation on security events

### Password Security
- BCrypt hashing with salt
- Minimum password complexity requirements
- Password change requires current password
- Account lockout after failed attempts

### API Security
- All endpoints require authentication by default
- Explicit public endpoint configuration
- Role-based authorization checks
- Input validation and sanitization

### Development Security
- Test security configuration disabled
- Separate security rules for test environment
- Mock authentication for integration tests
- Security headers in production 