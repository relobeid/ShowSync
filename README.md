# ShowSync

**AI-powered social platform for discovering and discussing movies, TV shows, and books**

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-17+-orange.svg)](https://openjdk.org/)
[![Next.js](https://img.shields.io/badge/Next.js-14-black.svg)](https://nextjs.org/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15+-blue.svg)](https://postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

## The Problem

Many media enthusiasts experience "media loneliness" - the frustration of consuming great content but having no one to discuss it with. Traditional social platforms lack the depth for meaningful media conversations, and finding like-minded individuals with similar tastes is challenging and time-consuming.

## Our Solution

ShowSync uses algorithmic matching to connect users with compatible media preferences, creating focused discussion groups similar to real-world book clubs. Our recommendation engine analyzes viewing patterns, ratings, and preferences to form communities where members genuinely share common interests, fostering engaging conversations about movies, TV shows, and books.

## Features

- **Algorithmic Matching**: Connect with users who share your media preferences and viewing patterns
- **Discussion Groups**: Join focused chat communities with like-minded media enthusiasts
- **Personal Library**: Track and rate movies, TV shows, and books to improve matching accuracy
- **Content Discovery**: AI-powered recommendations based on group preferences and individual taste
- **Real-time Chat**: Engage in meaningful discussions about current and upcoming media

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

| Topic | Description |
|-------|-------------|
| [Setup Guide](docs/setup/) | Installation and configuration instructions |
| [Architecture](docs/architecture.md) | System design and technical overview |
| [Features](docs/features/) | Implementation details and configuration |
| [API Reference](docs/controller/) | REST API documentation |

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
