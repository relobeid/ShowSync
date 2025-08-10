# ShowSync

**AI-powered social platform for discovering and discussing movies, TV shows, and books**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Next.js](https://img.shields.io/badge/Next.js-14-black.svg)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

ShowSync combines intelligent media recommendations with Discord-style communities, enabling users to discover content through taste-based matching and real-time social interaction.

## Features

- **Smart Recommendations**: AI-powered content discovery based on viewing patterns
- **Social Groups**: Real-time chat communities organized around shared interests  
- **Media Library**: Personal tracking and rating system for movies, TV shows, and books
- **External API Integration**: Rich metadata from TMDb and OpenLibrary
- **Taste Matching**: Algorithm-based user compatibility for group formation

## Quick Start

### Prerequisites

- Java 17+
- Node.js 18+
- Docker & Docker Compose
- TMDb API key ([get one here](https://www.themoviedb.org/settings/api))

### Installation

```bash
# Clone the repository
git clone https://github.com/yourusername/ShowSync.git
cd ShowSync

# Set up environment variables
cp .env.example .env
# Edit .env with your TMDb API key

# Start infrastructure
docker-compose up -d

# Start backend
./mvnw spring-boot:run

# Start frontend (in separate terminal)
cd frontend
npm install
npm run dev
```

### Access

- **Application**: http://localhost:3000
- **API Docs**: http://localhost:8080/swagger-ui.html
- **Health Check**: http://localhost:8080/actuator/health

## Architecture

**Backend**: Spring Boot with JWT authentication, PostgreSQL database, Redis caching  
**Frontend**: Next.js with TypeScript and Tailwind CSS  
**External APIs**: TMDb (movies/TV), OpenLibrary (books)  
**Infrastructure**: Docker containerization with multi-environment support

## Documentation

| Topic | Location | Description |
|-------|----------|-------------|
| **Getting Started** | [docs/setup/](docs/setup/) | Detailed setup and configuration |
| **API Reference** | [docs/controller/](docs/controller/) | Complete REST API documentation |
| **Architecture** | [docs/architecture.md](docs/architecture.md) | System design and technical decisions |
| **Development** | [docs/development/](docs/development/) | Workflows, tasks, and processes |
| **Features** | [docs/features/](docs/features/) | Feature implementation guides |
| **Recommendations** | [docs/features/recommendations.md](docs/features/recommendations.md) | AI endpoints, config, caching, scheduling |

## Development

### Project Structure

```
ShowSync/
├── src/                     # Spring Boot backend
├── frontend/                # Next.js frontend  
├── docs/                    # Technical documentation
├── docker/                  # Container configuration
└── scripts/                 # Development utilities
```

### Testing

```bash
# Backend tests
./mvnw test

# Frontend tests  
cd frontend && npm test

# Integration tests
./mvnw test -Dtest="*IntegrationTest"
```

### Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Make your changes with tests
4. Submit a pull request

See [docs/development/workflow.md](docs/development/workflow.md) for detailed guidelines.

## Status

| Component | Status | Notes |
|-----------|--------|-------|
| Authentication | ✅ Complete | JWT-based security |
| Media Search | ✅ Complete | TMDb/OpenLibrary integration |
| User Library | ✅ Complete | Ratings and reviews |
| Groups | ✅ Complete | Basic group management |
| Real-time Chat | 🚧 In Progress | WebSocket implementation |
| AI Recommendations | ✅ Complete (MVP) | Endpoints, schedulers, caching; UI MVP at /recommendations |

## License

MIT License - see [LICENSE](LICENSE) for details.

---

**Questions?** Check the [documentation](docs/) or open an [issue](https://github.com/yourusername/ShowSync/issues).
