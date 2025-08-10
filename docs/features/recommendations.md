# Recommendations (AI)

This document describes the AI recommendation system: endpoints, flows, configuration, caching, and scheduling.

## Overview
- Personalized and real-time content recommendations
- Group suggestions and group content discovery
- Trending fallback for cold-start
- Feedback loop (ratings) to refine results

## Key endpoints
Base: `/api/recommendations`

- GET `/personal?page=&size=`: Page of personal content recommendations
- GET `/realtime?mediaId=&limit=`: Real-time recommendations; if `mediaId` provided, content-based; otherwise blends collaborative + trending
- GET `/trending?limit=`: Platform trending content
- GET `/groups?page=&size=`: Group suggestions for the user
- GET `/groups/{groupId}/content?page=&size=`: Group-specific content recommendations
- POST `/view/{type}/{id}`: Mark recommendation viewed (type = CONTENT|GROUP)
- POST `/dismiss/{type}/{id}?reason=`: Dismiss recommendation (idempotent)
- POST `/feedback/{type}/{id}?rating=&comment=`: Submit explicit feedback (1–5)
- POST `/generate`: Trigger batch generation for all users (admin ops)
- POST `/generate/me`: Generate recommendations for current user
- GET `/analytics?days=`: System-level analytics (minimal by default)
- GET `/insights/me`: User insights (confidence, personality)
- GET `/summary/me`: Quick summary for dashboard
- GET `/similar/{mediaId}?limit=`: Similar content to a media item
- GET `/by-type?type=&limit=`: Filtered recommendations by type

All endpoints require authentication; `@AuthenticationPrincipal` is used to obtain the user.

## Scheduling
Enabled via `@EnableScheduling` and configurable in `RecommendationConfig`:

- `showsync.recommendations.enable-schedulers` (default: true)
- `showsync.recommendations.daily-generation-cron` (default: `0 15 3 * * *`)
- `showsync.recommendations.active-users-refresh-cron` (default: `0 10 * * * *`)
- `showsync.recommendations.active-users-hours-back` (default: 24)

Jobs:
- Daily all-users generation
- Hourly refresh for recently active users

## Caching
Configured in `CacheConfig` with Redis (fallback to in-memory):
- `trendingRecommendations`: 6h
- `recommendationAnalytics`: 6h
- `userRecommendationInsights`: 1h
- `userCompatibility`: 12h
- `userGenrePreferences`, `userPlatformPreferences`, `userEraPreferences`: 6h

## Configuration knobs (RecommendationConfig)
- Weights: genre/rating/platform/era must sum to 1.0
- Confidence thresholds & interaction minimums
- Personalization/diversity/exploration factors
- Expirations: content (14d), group (7d)
- Async generation & thread pool size
- Feature flags: collaborative, content-based, trending, seasonal, experimental

## Business logic notes
- Cold-start: trending + exploration
- Replacement: cap active recs/user; evict dismissed/expired first
- Feedback: 1–5 rating maps to feedback type; marks as viewed
- Privacy: suggest public groups; private/group-relations can be added later

## Frontend
- API client in `frontend/src/lib/api.ts`
- MVP page at `/recommendations` showing personal + trending lists

## Future enhancements
- Rich analytics and metrics
- Deeper diversification controls
- Per-user caching with invalidation on feedback/generation
