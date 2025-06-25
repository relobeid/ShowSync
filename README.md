# ShowSync

<div align="center">

<img src=".github/assets/logo.png" alt="ShowSync Logo" width="200">

**The AI-powered social platform where Discord meets IMDB**

*Watch, read, and sync up with people who share your taste*

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-brightgreen.svg)](https://github.com/yourusername/ShowSync/actions)

</div>

## What is ShowSync?

ShowSync is the next evolution of media discovery and social engagement. Think **Discord meets IMDB meets Rotten Tomatoest** — an AI-powered platform that transforms how people discover, discuss, and experience movies, TV shows, and books together.

### The Problem We Solve

Traditional media platforms are either:
- **Solo experiences** (IMDB, Goodreads) with great ratings but no community
- **Generic social platforms** (Discord) with great chat but no media intelligence
- **Algorithm bubbles** that only show you what you already like

ShowSync bridges this gap by creating **intelligent, taste-based communities** where AI connects you with the right people to discover your next obsession.

## Key Features

### **AI-Powered Matchmaking**
- Smart group formation based on taste compatibility
- Preference learning from your ratings and interactions
- Dynamic recommendation engine that evolves with your tastes

### **Discord-Style Communities**
- Real-time group chats for immediate reactions and theories
- Organized channels for different shows, genres, and discussions
- Voice/video support for watch parties and book clubs

### **IMDB-Level Intelligence**
- Comprehensive media database with ratings and reviews
- Advanced analytics on your viewing/reading patterns
- Professional-grade recommendation algorithms

### **Taste-Based Discovery**
- Find groups watching exactly what you want to discover
- Join ongoing book clubs and watch parties
- Get matched with users who consistently recommend gems

## How It Works

### 1. **Build Your Profile**
Rate movies, shows, and books you've experienced. Our AI learns your taste patterns, genre preferences, and discovery style.

### 2. **Get Matched**
Our algorithm analyzes your preferences and connects you with groups and individuals who share compatible tastes but different perspectives.

### 3. **Join Communities**
Enter Discord-style group chats organized around specific shows, genres, or ongoing reading/watch parties.

### 4. **Discover Together**
Get AI-curated recommendations from your taste-matched community. Discuss theories, share reactions, and plan group experiences.

## Technology Stack

<table>
<tr>
<td><strong>Backend</strong></td>
<td>
  <img src="https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=spring&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-316192?style=flat&logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Redis-DC382D?style=flat&logo=redis&logoColor=white" alt="Redis"/>
</td>
</tr>
<tr>
<td><strong>Frontend</strong></td>
<td>
  <img src="https://img.shields.io/badge/Next.js-000000?style=flat&logo=next.js&logoColor=white" alt="Next.js"/>
  <img src="https://img.shields.io/badge/TypeScript-007ACC?style=flat&logo=typescript&logoColor=white" alt="TypeScript"/>
  <img src="https://img.shields.io/badge/Tailwind-38B2AC?style=flat&logo=tailwind-css&logoColor=white" alt="Tailwind"/>
</td>
</tr>
<tr>
<td><strong>AI & Data</strong></td>
<td>
  <img src="https://img.shields.io/badge/OpenAI-412991?style=flat&logo=openai&logoColor=white" alt="OpenAI"/>
  <img src="https://img.shields.io/badge/TMDb-01B4E4?style=flat&logo=themoviedatabase&logoColor=white" alt="TMDb"/>
  <img src="https://img.shields.io/badge/Machine%20Learning-FF6F00?style=flat&logo=tensorflow&logoColor=white" alt="ML"/>
</td>
</tr>
</table>

## Quick Start

### Prerequisites
- **Java 17+** and **Maven 3.6+**
- **Docker & Docker Compose**
- **Node.js 18+** (for frontend)

### Backend Setup
```bash
# Clone and navigate
git clone https://github.com/yourusername/ShowSync.git
cd ShowSync

# Start infrastructure
docker-compose up -d postgres redis pgadmin

# Run backend
./mvnw spring-boot:run
```

### Frontend Setup (Coming Soon)
```bash
# Navigate to frontend
cd frontend

# Install dependencies
npm install

# Start development server
npm run dev
```

### Verify Installation
```bash
# Check backend health
curl http://localhost:8080/api/health

# Check API documentation
open http://localhost:8080/swagger-ui.html
```

## API Documentation

### Authentication
```bash
# Register new user
POST /api/auth/register
{
  "username": "moviebuff2024",
  "email": "user@example.com",
  "password": "SecurePass123!"
}

# Login
POST /api/auth/login
{
  "username": "moviebuff2024",
  "password": "SecurePass123!"
}
```

### Media Management
```bash
# Add movie to library
POST /api/library/media
{
  "externalId": "550",
  "source": "tmdb",
  "status": "WATCHING"
}

# Rate and review
PUT /api/library/media/1/rating
{
  "rating": 9,
  "review": "Incredible cinematography and plot twists!"
}
```

### Groups & Social
```bash
# Create a group
POST /api/groups
{
  "name": "Sci-Fi Movie Club",
  "description": "For fans of mind-bending science fiction",
  "privacySetting": "PUBLIC"
}

# Join group
POST /api/groups/1/join
```

Full API documentation available at `/swagger-ui.html` when running locally.

## AI & Recommendation Engine

### Taste Modeling
Our AI engine analyzes multiple dimensions of your preferences:

- **Genre Affinity**: Sci-fi vs Romance vs Horror preferences
- **Content Patterns**: Independent films vs blockbusters
- **Rating Behavior**: Harsh critic vs generous rater
- **Discovery Style**: Mainstream vs niche content seeker
- **Social Interaction**: Discussion leader vs quiet observer

### Group Matching Algorithm
```
Compatibility Score = 
  (Taste Similarity × 0.4) +
  (Discovery Complementarity × 0.3) +
  (Social Style Match × 0.2) +
  (Activity Level Sync × 0.1)
```

### Smart Recommendations
- **Individual**: Personalized suggestions based on your unique taste profile
- **Group**: Content that maximizes enjoyment across all group members
- **Discovery**: Introduces you to new genres through trusted community members
- **Trending**: Popular content filtered through your taste preferences

## Development Roadmap

### Phase 1: Foundation (Complete)
- User authentication and profiles
- Media library management
- Basic rating and review system
- External API integrations (TMDb, Open Library)

### Phase 2: Groups & Social (In Progress)
- Group creation and management
- Real-time messaging system
- Group media lists and voting
- Activity feeds and notifications

### Phase 3: AI Integration (Planned)
- Taste preference modeling
- Group matching algorithms
- Intelligent content recommendations
- Personality-based social matching

### Phase 4: Advanced Features (Future)
- Watch party coordination
- Voice/video chat integration
- Mobile progressive web app
- Advanced analytics and insights

## Project Status

| Component | Status | Coverage | Notes |
|-----------|--------|----------|-------|
| Authentication | Complete | 95% | JWT-based, secure |
| User Management | Complete | 90% | Full CRUD operations |
| Media Library | Complete | 88% | TMDb & OpenLibrary integrated |
| Reviews System | Complete | 92% | Voting and moderation |
| Groups Foundation | Complete | 85% | Basic group management |
| Real-time Chat | In Progress | 60% | WebSocket implementation |
| AI Recommendations | Planned | 0% | Algorithm design phase |
| Mobile App | Planned | 0% | PWA approach |

## Contributing

We welcome contributions from developers, designers, and media enthusiasts!

### Development Process
1. **Pick a task** from our [issues](https://github.com/yourusername/ShowSync/issues)
2. **Fork and branch** from `main`
3. **Follow our testing** standards (80%+ coverage required)
4. **Submit PR** with clear description and tests

### Code Standards
- **Java**: Follow Spring Boot best practices
- **Testing**: Unit tests required for all business logic
- **Documentation**: Update relevant docs with changes
- **Security**: Never commit API keys or credentials

### Getting Help
- [Discord Community](https://discord.gg/showsync) - Join our development discussions
- [Email](mailto:dev@showsync.app) - Direct contact for collaboration
- [GitHub Issues](https://github.com/yourusername/ShowSync/issues) - Bug reports and feature requests

## License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built by media enthusiasts, for media enthusiasts**

[Website](https://showsync.app) • [Discord](https://discord.gg/showsync) • [Twitter](https://twitter.com/showsyncapp) • [Documentation](https://docs.showsync.app)

</div>
