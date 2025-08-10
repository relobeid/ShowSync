# Controllers

REST API controllers for ShowSync backend. All controllers follow RESTful conventions and require authentication unless otherwise specified.

## Structure

| Controller | Endpoint Base | Purpose | Authentication |
|------------|---------------|---------|----------------|
| `AuthController` | `/api/auth` | User authentication and registration | Public |
| `MediaSearchController` | `/api/media/search` | External media search integration | Required |
| `UserMediaLibraryController` | `/api/library` | User's personal media library | Required |
| `ReviewController` | `/api/reviews` | Media reviews and ratings | Required |
| `GroupController` | `/api/groups` | Group management | Required |
| `GroupMediaController` | `/api/groups/{id}/media` | Group media activities | Required |
| `RecommendationController` | `/api/recommendations` | AI recommendations (content, groups, trending) | Required |
| `HealthController` | `/actuator/health` | System health checks | Public |

## Authentication Endpoints

### AuthController (`/api/auth`)
- `POST /register` - User registration
- `POST /login` - User authentication  
- `GET /profile` - Get current user profile (requires auth)

## Media Search Endpoints

### MediaSearchController (`/api/media/search`)
- `GET /movies?query={query}&page={page}` - Search movies via TMDb
- `GET /tv-shows?query={query}&page={page}` - Search TV shows via TMDb
- `GET /books?query={query}&limit={limit}` - Search books via OpenLibrary
- `GET /movies/{id}` - Get movie details by TMDb ID
- `GET /tv-shows/{id}` - Get TV show details by TMDb ID

## User Library Endpoints

### UserMediaLibraryController (`/api/library`)
- `GET /` - Get user's media library
- `POST /` - Add media to library
- `PUT /{mediaId}` - Update media in library
- `DELETE /{mediaId}` - Remove media from library
- `GET /favorites` - Get user's favorite media

## Group Management Endpoints
## Recommendations Endpoints

### RecommendationController (`/api/recommendations`)
- `GET /personal?page=&size=` - Personal content recommendations
- `GET /realtime?mediaId=&limit=` - Real-time recs (content-based when `mediaId` provided)
- `GET /trending?limit=` - Trending content
- `GET /groups?page=&size=` - Group suggestions
- `GET /groups/{groupId}/content?page=&size=` - Group-specific content
- `POST /view/{type}/{id}` - Mark viewed (idempotent)
- `POST /dismiss/{type}/{id}?reason=` - Dismiss recommendation
- `POST /feedback/{type}/{id}?rating=&comment=` - Submit feedback (1–5)
- `POST /generate` - Generate for all users (admin)
- `POST /generate/me` - Generate for current user
- `GET /analytics?days=` - System analytics (MVP)
- `GET /insights/me` - User insights (confidence, personality)
- `GET /summary/me` - Compact summary for dashboard
- `GET /similar/{mediaId}?limit=` - Similar content
- `GET /by-type?type=&limit=` - Filtered recommendations

All endpoints require JWT auth; some admin-only operations may be restricted.

### GroupController (`/api/groups`)
- `GET /` - List user's groups
- `POST /` - Create new group
- `GET /{id}` - Get group details
- `PUT /{id}` - Update group
- `DELETE /{id}` - Delete group
- `POST /{id}/join` - Join group
- `POST /{id}/leave` - Leave group

## Common Patterns

### Request/Response Format
- All endpoints accept/return JSON
- Use standard HTTP status codes
- Include validation error details in 400 responses

### Error Handling
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Missing/invalid authentication
- `403 Forbidden` - Insufficient permissions
- `404 Not Found` - Resource not found
- `500 Internal Server Error` - System errors

### Pagination
Query parameters for paginated endpoints:
- `page` - Page number (1-based)
- `size` - Items per page
- `sort` - Sort criteria

### Security
- JWT token required in Authorization header: `Bearer {token}`
- User can only access their own resources
- Group access controlled by membership 