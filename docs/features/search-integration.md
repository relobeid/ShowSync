# Search Integration

**Status:** Backend Complete, Frontend Pending  
**Branch:** `feature/search-integration-backend-fix`  
**Date:** 2025-07-19

## Overview

This document outlines the search integration feature that enables users to search for movies, TV shows, and books through external APIs. The backend implementation is complete and functional.

## Architecture

### External APIs
- **TMDb API**: Movie and TV show data
- **OpenLibrary API**: Book data

### Backend Components
- `MediaSearchController`: REST endpoints for search operations
- `ExternalMediaService`: Service layer for external API calls
- `WebClientConfig`: HTTP client configuration with timeouts and retry logic

### Security
All search endpoints require JWT authentication with `ROLE_USER` or higher.

## API Endpoints

### Movie Search
```
GET /api/media/search/movies?query={query}&page={page}
```
- Query parameter: `query` (required, 1-100 chars)
- Query parameter: `page` (optional, default: 1, max: 1000)
- Returns: TMDb search response with movie results

### TV Show Search
```
GET /api/media/search/tv-shows?query={query}&page={page}
```
- Query parameter: `query` (required, 1-100 chars) 
- Query parameter: `page` (optional, default: 1, max: 1000)
- Returns: TMDb search response with TV show results

### Book Search
```
GET /api/media/search/books?query={query}&limit={limit}&offset={offset}
```
- Query parameter: `query` (required, 1-100 chars)
- Query parameter: `limit` (optional, default: 10, max: 100)
- Query parameter: `offset` (optional, default: 0)
- Returns: OpenLibrary search response with book results

### Media Details
```
GET /api/media/search/movies/{id}
GET /api/media/search/tv-shows/{id}
```
- Path parameter: `id` (TMDb media ID)
- Returns: Detailed media information

## Response Examples

### Movie Search Response
```json
{
  "page": 1,
  "totalResults": 90,
  "totalPages": 5,
  "results": [
    {
      "id": 603,
      "title": "The Matrix",
      "originalTitle": "The Matrix",
      "overview": "Set in the 22nd century...",
      "releaseDate": "1999-03-30",
      "voteAverage": 8.2,
      "voteCount": 26573,
      "posterPath": "/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
      "backdropPath": "/fNG7i7RqMErkcqhohV2a6cV1Ehy.jpg",
      "genreIds": [28, 878]
    }
  ]
}
```

### Book Search Response
```json
{
  "numFound": 150,
  "start": 0,
  "docs": [
    {
      "key": "/works/OL82563W",
      "title": "Harry Potter and the Philosopher's Stone",
      "authorName": ["J. K. Rowling"],
      "firstPublishYear": 1997,
      "isbn": ["9780747532699"],
      "coverId": 258027
    }
  ]
}
```

## Configuration

### Required Environment Variables
```bash
TMDB_API_KEY=your_tmdb_api_key_here
```

### Application Properties
```yaml
external-apis:
  tmdb:
    baseUrl: https://api.themoviedb.org/3
    timeout: 10000
    rateLimit: 40
  openLibrary:
    baseUrl: https://openlibrary.org
    timeout: 15000
    rateLimit: 100
```

## Error Handling

- **400 Bad Request**: Invalid query parameters
- **401 Unauthorized**: Missing or invalid JWT token
- **503 Service Unavailable**: External API timeout or failure

Fallback responses return empty result sets with appropriate HTTP status codes.

## Testing Results

**Verified endpoints:**
- Movie search: 90 results for "matrix"
- TV search: 4 results for "game of thrones", 44 results for "invincible"
- Authentication: JWT tokens working correctly
- Error handling: Graceful fallbacks for API failures

**Known Issues:**
- OpenLibrary API has JSON deserialization issues with certain book records
- Book search returns empty results due to response format inconsistencies

## Implementation Notes

### Controller Fix
Converted from reactive `Mono<ResponseEntity<>>` to traditional `ResponseEntity<>` to resolve authentication conflicts between WebFlux and Servlet security stacks.

### Caching
Responses cached using Spring Cache with parameterized keys. Cache TTL configured per API (1 hour for TMDb, 2 hours for OpenLibrary).

### Rate Limiting
Implemented using Resilience4j with circuit breaker pattern and retry logic.

## Frontend Requirements

### Next Steps
1. Create search page at `/app/search/page.tsx`
2. Build search components (SearchBox, MediaCard, ResultsList, Pagination)
3. Add API client methods to `lib/api.ts`
4. Update TypeScript types to match backend DTOs
5. Implement error handling and loading states
6. Add responsive design with Tailwind CSS

### Required Frontend Files
```
frontend/src/app/search/page.tsx
frontend/src/components/search/SearchBox.tsx
frontend/src/components/search/MediaCard.tsx
frontend/src/components/search/ResultsList.tsx
frontend/src/components/search/Pagination.tsx
frontend/src/hooks/useSearch.ts
frontend/src/hooks/useDebounce.ts
```

### API Integration
Frontend will consume backend endpoints using JWT authentication. No direct external API calls required from client. 