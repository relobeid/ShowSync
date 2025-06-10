# ShowSync

**An AI-powered social media discovery platform for TV, movies, and books**

ShowSync combines the rating/logging functionality of IMDb with the community aspect of book clubs, creating AI-powered groups based on taste similarities for collaborative discovery and discussion across all media types.

## 🎯 Project Overview

ShowSync enables users to:
- Log and rate what they've watched/read (like IMDb for all media types)
- Get matched into groups ("clubs") based on similar tastes using AI
- Discuss, vote, and comment on shows, movies, and books with their groups
- Receive recommendations for new groups and media to experience together
- Join active "watch/read" clubs around ongoing shows and books
- Discover trending media and top-rated content by groups

## 🏗️ Architecture & Planning

This project follows a structured development approach with comprehensive planning documents:

### 📋 [**tasks.md**](./tasks.md)
Detailed development roadmap with 6 phases of small, testable, actionable tasks:
- **Phase 1**: Foundation Setup (Authentication, Database, Basic API)
- **Phase 2**: Core Media Features (External APIs, User Library, Reviews)
- **Phase 3**: Group Foundation (Group Management, Activities, Communication)
- **Phase 4**: AI-Powered Matching (Preference Analysis, Recommendations)
- **Phase 5**: Advanced Features (Social Features, Watch Parties, Mobile)
- **Phase 6**: Performance & Scale (Optimization, Security, Analytics)

### 🏛️ [**architecture.md**](./architecture.md)
Comprehensive system design and technical architecture:
- High-level system architecture
- Database schema strategy
- AI & recommendation system design
- Performance & scalability considerations
- Security architecture
- Testing strategy
- Monitoring & observability

### ⚙️ [**workflow.md**](./workflow.md)
Step-by-step development workflow for each feature:
- Planning & setup process
- Test-driven development approach
- Quality assurance gates
- Documentation & commit standards
- Stakeholder validation process
- Deployment & monitoring

## 🛠️ Technology Stack

### Backend
- **Spring Boot 3.x** with Java 17+
- **PostgreSQL** for primary data storage
- **Redis** for caching and real-time data
- **Spring Security** for authentication/authorization
- **Spring WebSocket** for real-time features

### Frontend (Planned)
- **Next.js 14** with TypeScript
- **Tailwind CSS** + **shadcn/ui** for styling
- **React Query** for state management

### External APIs
- **The Movie Database (TMDb)** - Movies and TV shows
- **Open Library API** - Books data
- **Google Books API** - Additional book information
- **JustWatch API** - Streaming availability
- **OpenAI/Anthropic API** - AI-powered recommendations

## 🚀 Getting Started

### Prerequisites
- Java 17+
- Maven 3.6+
- Docker & Docker Compose
- PostgreSQL 15+
- Redis 7+

### Quick Start
```bash
# Clone the repository
git clone https://github.com/yourusername/ShowSync.git
cd ShowSync

# Start required services
docker-compose up -d postgres redis

# Build and run the application
./mvnw spring-boot:run

# Check health
curl http://localhost:8080/actuator/health
```

## 📈 Development Process

### Current Phase: Foundation Setup
Following the structured approach in `tasks.md`, starting with:
1. **Task 1.1**: Project Initialization
2. **Task 1.2**: Database Schema Setup  
3. **Task 1.3**: Authentication System

### Testing & Validation
Each task includes:
- Specific test cases to validate functionality
- Unit and integration tests
- Manual testing procedures
- Stakeholder validation checkpoints

### Quality Gates
Before moving to the next task:
- ✅ All tests pass (unit + integration)
- ✅ Code coverage >= 80%
- ✅ No critical security vulnerabilities
- ✅ Stakeholder approval received
- ✅ Documentation updated

## 🎯 Key Features (Planned)

### Core Features
- [ ] User registration and authentication
- [ ] Media library management (movies, TV, books)
- [ ] Rating and review system
- [ ] AI-powered group matching
- [ ] Group discussion and activities
- [ ] Real-time notifications

### Advanced Features
- [ ] Watch party scheduling
- [ ] Social following system
- [ ] Advanced recommendation engine
- [ ] Mobile Progressive Web App
- [ ] Analytics and insights

## 📊 AI & Recommendation System

ShowSync's core differentiator is its AI-powered group matching and content recommendation system:

### User Preference Modeling
- Genre preferences analysis
- Rating patterns and tendencies
- Content consumption behaviors
- Social interaction patterns

### Group Matching Algorithm
- Preference alignment scoring
- Activity level compatibility
- Social fit analysis
- Content freshness factors

### Content Recommendations
- Group-based collaborative filtering
- Individual preference weighting
- Trending content integration
- Diversity and serendipity factors

## 🔐 Security & Privacy

- JWT-based authentication
- GDPR compliance features
- Data retention policies
- Privacy-focused recommendation system
- Secure API design

## 📚 Documentation

- [Development Tasks](./tasks.md) - Detailed development roadmap
- [Architecture Guide](./architecture.md) - System design and technical decisions
- [Development Workflow](./workflow.md) - Step-by-step development process
- API Documentation - Generated with OpenAPI/Swagger (coming soon)

## 🤝 Contributing

This project follows a structured development workflow. Please see [workflow.md](./workflow.md) for detailed contribution guidelines.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

**Built with ❤️ for the community of media enthusiasts who want to discover and discuss great content together.**
