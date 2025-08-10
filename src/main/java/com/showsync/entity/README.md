# Entities

JPA entities representing the ShowSync database schema. All entities use Lombok annotations for cleaner code and follow JPA best practices.

## Core Entities

| Entity | Purpose | Key Relationships |
|--------|---------|-------------------|
| `User` | User accounts and profiles | One-to-Many with UserMediaInteraction, GroupMembership |
| `Media` | Movies, TV shows, and books | One-to-Many with UserMediaInteraction, Review |
| `UserMediaInteraction` | User's relationship with media | Many-to-One with User, Media |
| `Review` | User reviews and ratings | Many-to-One with User, Media |
| `Group` | User groups for shared activities | One-to-Many with GroupMembership, GroupMediaList |
| `GroupMembership` | User membership in groups | Many-to-One with User, Group |
| `GroupMediaList` | Media items in group lists | Many-to-One with Group, Media |
| `GroupMediaVote` | User votes on group media | Many-to-One with User, GroupMediaList |
| `GroupActivity` | Activity feed for groups | Many-to-One with Group, User |
| `ReviewVote` | Votes on reviews (helpful/not) | Many-to-One with User, Review |
| `UserPreferenceProfile` | Calculated taste profile per user | One-to-One with User |
| `ContentRecommendation` | Content-level recommendations | Many-to-One with User, Media |
| `GroupRecommendation` | Group suggestions for users | Many-to-One with User, Group |
| `RecommendationFeedback` | Explicit feedback on recs | Many-to-One with User |

## Entity Details

### User
Primary user entity with authentication and profile data.

**Key Fields:**
- `id` - Primary key
- `username` - Unique username
- `email` - User email address
- `password` - Encrypted password
- `displayName` - Public display name
- `role` - User role (USER, ADMIN)
- `isActive` - Account status
- `createdAt`, `updatedAt` - Audit timestamps

### Media
Unified entity for all media types (movies, TV shows, books).

**Key Fields:**
- `id` - Primary key
- `externalId` - ID from external API (TMDb, OpenLibrary)
- `externalSource` - Source API (TMDB, OPENLIBRARY)
- `mediaType` - Type enum (MOVIE, TV_SHOW, BOOK)
- `title` - Media title
- `description` - Synopsis/description
- `releaseDate` - Release date (LocalDateTime)
- `type` - Enum (MOVIE, TV_SHOW, BOOK)
- `averageRating`, `ratingCount`
### UserPreferenceProfile
Stores per-user preferences (genres/platforms/eras), viewing personality, and confidence.

**Key Fields:**
- `user` - One-to-one User
- `genrePreferences` - JSON string map
- `platformPreferences` - JSON string map
- `eraPreferences` - JSON string map
- `viewingPersonality` - Enum
- `confidenceScore` - BigDecimal

### ContentRecommendation
Recommendation records for media.

**Key Fields:**
- `user`, `media`
- `type` - Recommendation type enum (PERSONAL, TRENDING, etc.)
- `relevanceScore` - BigDecimal
- `reasonCode` - Enum reason
- `explanation` - Human-readable why
- `viewed`, `dismissed`, `addedToLibraryAt` timestamps

### GroupRecommendation
Precalculated group suggestions.

**Key Fields:**
- `user`, `group`
- `compatibilityScore` - BigDecimal
- `reasonCode`, `explanation`
- `viewed`, `dismissed`, `joinedAt` timestamps

### RecommendationFeedback
Explicit user feedback on recommendations.

**Key Fields:**
- `user`
- `feedbackType` and reason
- `targetType` (CONTENT/GROUP) + `targetId`
- `rating` (1–5), `comment`
- `posterUrl` - Poster image URL
- `averageRating` - Calculated average rating

### UserMediaInteraction
Tracks user's interaction with specific media.

**Key Fields:**
- `id` - Primary key
- `user` - User reference
- `media` - Media reference
- `status` - Watching status (WATCHING, COMPLETED, etc.)
- `rating` - User's rating (1-10)
- `progress` - Progress percentage
- `isFavorite` - Favorite status
- `review` - User's review text
- `watchedAt` - Completion date

### Group
User groups for shared media experiences.

**Key Fields:**
- `id` - Primary key
- `name` - Group name
- `description` - Group description
- `isPrivate` - Privacy setting
- `maxMembers` - Member limit
- `createdBy` - Group creator
- `createdAt` - Creation timestamp

### Review
User reviews for media items.

**Key Fields:**
- `id` - Primary key
- `user` - Reviewer
- `media` - Reviewed media
- `rating` - Review rating (1-10)
- `content` - Review text
- `helpfulVotes` - Count of helpful votes
- `notHelpfulVotes` - Count of not helpful votes
- `isVisible` - Visibility status

## Design Patterns

### Base Entity
```java
@MappedSuperclass
public abstract class BaseEntity {
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

### Audit Annotations
- `@CreatedDate` - Automatic creation timestamp
- `@LastModifiedDate` - Automatic update timestamp
- `@CreatedBy` - User who created the entity
- `@LastModifiedBy` - User who last modified

### Lombok Annotations
- `@Entity` - JPA entity marker
- `@Data` - Getters, setters, toString, equals, hashCode
- `@NoArgsConstructor` - Default constructor
- `@AllArgsConstructor` - Constructor with all fields
- `@Builder` - Builder pattern

### Validation
- `@NotNull` - Required fields
- `@Size` - String length constraints
- `@Email` - Email format validation
- `@Min/@Max` - Numeric range validation

## Database Schema

### Indexes
- User: username, email (unique)
- Media: externalId + externalSource (unique)
- UserMediaInteraction: user + media (unique)
- Review: media, user
- Group: createdBy

### Constraints
- Foreign key constraints on all relationships
- Unique constraints on business keys
- Check constraints for enums and ranges
- Not null constraints on required fields

### JSON Fields
- Media.genres - JSON array of genre strings
- User.preferences - JSON object for user settings 