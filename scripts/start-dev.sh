#!/bin/bash

# =============================================================================
# ShowSync Development Environment Startup Script
# =============================================================================
# This script provides a convenient way to start the complete ShowSync
# development environment with all required services.
#
# Usage:
#   ./scripts/start-dev.sh [options]
#
# Options:
#   --clean     : Clean and rebuild all containers
#   --logs      : Show logs after starting services
#   --test      : Run tests after starting services
#   --help      : Show this help message
#
# Services Started:
#   - PostgreSQL database (port 5432)
#   - Redis cache (port 6379)
#   - pgAdmin web interface (port 5050) - optional
#   - Redis Insight web interface (port 8001) - optional
#   - ShowSync Spring Boot application (port 8080)
# =============================================================================

set -e  # Exit on any error

# Script configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
LOG_FILE="$PROJECT_ROOT/logs/startup.log"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default options
CLEAN_BUILD=false
SHOW_LOGS=false
RUN_TESTS=false
SKIP_APP=false

# =============================================================================
# UTILITY FUNCTIONS
# =============================================================================

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')] $1${NC}"
}

warn() {
    echo -e "${YELLOW}[$(date +'%Y-%m-%d %H:%M:%S')] WARNING: $1${NC}"
}

error() {
    echo -e "${RED}[$(date +'%Y-%m-%d %H:%M:%S')] ERROR: $1${NC}"
    exit 1
}

info() {
    echo -e "${BLUE}[$(date +'%Y-%m-%d %H:%M:%S')] INFO: $1${NC}"
}

show_help() {
    cat << EOF
ShowSync Development Environment Startup Script

Usage: $0 [options]

Options:
    --clean      Clean and rebuild all containers
    --logs       Show logs after starting services
    --test       Run tests after starting services
    --skip-app   Skip starting the Spring Boot application
    --help       Show this help message

Services:
    - PostgreSQL database (localhost:5432)
    - Redis cache (localhost:6379)
    - pgAdmin (localhost:5050) - username: admin@showsync.local, password: admin
    - Redis Insight (localhost:8001)
    - ShowSync API (localhost:8080)

Examples:
    $0                    # Start all services
    $0 --clean --test     # Clean build and run tests
    $0 --logs             # Start services and show logs
    $0 --skip-app         # Start infrastructure only

EOF
}

check_prerequisites() {
    log "Checking prerequisites..."
    
    # Check if Docker is installed and running
    if ! command -v docker &> /dev/null; then
        error "Docker is not installed. Please install Docker and try again."
    fi
    
    if ! docker info &> /dev/null; then
        error "Docker is not running. Please start Docker and try again."
    fi
    
    # Check if Docker Compose is available
    if ! command -v docker-compose &> /dev/null; then
        error "Docker Compose is not installed. Please install Docker Compose and try again."
    fi
    
    # Check if Java is installed
    if ! command -v java &> /dev/null; then
        error "Java is not installed. Please install Java 17+ and try again."
    fi
    
    # Check Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n1 | cut -d'"' -f2 | cut -d'.' -f1)
    if [ "$JAVA_VERSION" -lt "17" ]; then
        error "Java 17 or higher is required. Current version: $JAVA_VERSION"
    fi
    
    log "Prerequisites check passed ✓"
}

create_directories() {
    log "Creating required directories..."
    
    mkdir -p "$PROJECT_ROOT/logs"
    mkdir -p "$PROJECT_ROOT/docker/postgres/init"
    mkdir -p "$PROJECT_ROOT/docker/postgres/conf"
    mkdir -p "$PROJECT_ROOT/docker/pgadmin"
    mkdir -p "$PROJECT_ROOT/src/main/resources/db/migration"
    
    log "Directories created ✓"
}

start_infrastructure() {
    log "Starting infrastructure services..."
    
    cd "$PROJECT_ROOT"
    
    if [ "$CLEAN_BUILD" = true ]; then
        log "Cleaning existing containers and volumes..."
        docker-compose down -v --remove-orphans
        docker-compose build --no-cache
    fi
    
    # Start infrastructure services
    docker-compose up -d postgres redis
    
    log "Waiting for services to be ready..."
    
    # Wait for PostgreSQL
    local postgres_ready=false
    local attempts=0
    local max_attempts=30
    
    while [ $postgres_ready = false ] && [ $attempts -lt $max_attempts ]; do
        if docker-compose exec -T postgres pg_isready -U showsync_user -d showsync_dev &> /dev/null; then
            postgres_ready=true
            log "PostgreSQL is ready ✓"
        else
            info "Waiting for PostgreSQL... (attempt $((attempts + 1))/$max_attempts)"
            sleep 2
            attempts=$((attempts + 1))
        fi
    done
    
    if [ $postgres_ready = false ]; then
        error "PostgreSQL failed to start after $max_attempts attempts"
    fi
    
    # Wait for Redis
    local redis_ready=false
    attempts=0
    
    while [ $redis_ready = false ] && [ $attempts -lt $max_attempts ]; do
        if docker-compose exec -T redis redis-cli ping &> /dev/null; then
            redis_ready=true
            log "Redis is ready ✓"
        else
            info "Waiting for Redis... (attempt $((attempts + 1))/$max_attempts)"
            sleep 2
            attempts=$((attempts + 1))
        fi
    done
    
    if [ $redis_ready = false ]; then
        error "Redis failed to start after $max_attempts attempts"
    fi
    
    # Start optional management interfaces
    log "Starting management interfaces..."
    docker-compose up -d pgadmin redis-insight
    
    log "Infrastructure services started successfully ✓"
}

start_application() {
    if [ "$SKIP_APP" = true ]; then
        log "Skipping Spring Boot application startup"
        return
    fi
    
    log "Starting ShowSync Spring Boot application..."
    
    cd "$PROJECT_ROOT"
    
    # Build the application
    log "Building application..."
    ./mvnw clean compile -q
    
    # Run database migrations (if any)
    log "Running database migrations..."
    ./mvnw flyway:migrate -q || warn "Flyway migration failed or no migrations found"
    
    # Start the application in background
    log "Starting Spring Boot application..."
    nohup ./mvnw spring-boot:run > "$LOG_FILE" 2>&1 &
    APP_PID=$!
    
    # Save PID for later cleanup
    echo $APP_PID > "$PROJECT_ROOT/.app.pid"
    
    log "Application starting with PID: $APP_PID"
    log "Application logs: $LOG_FILE"
    
    # Wait for application to be ready
    local app_ready=false
    local attempts=0
    local max_attempts=60
    
    while [ $app_ready = false ] && [ $attempts -lt $max_attempts ]; do
        if curl -s http://localhost:8080/actuator/health &> /dev/null; then
            app_ready=true
            log "ShowSync application is ready ✓"
        else
            info "Waiting for application... (attempt $((attempts + 1))/$max_attempts)"
            sleep 3
            attempts=$((attempts + 1))
        fi
    done
    
    if [ $app_ready = false ]; then
        error "Application failed to start after $max_attempts attempts. Check logs: $LOG_FILE"
    fi
}

run_tests() {
    if [ "$RUN_TESTS" = false ]; then
        return
    fi
    
    log "Running tests..."
    
    cd "$PROJECT_ROOT"
    
    # Run unit tests
    log "Running unit tests..."
    ./mvnw test
    
    # Run integration tests
    log "Running integration tests..."
    ./mvnw integration-test
    
    log "All tests completed ✓"
}

show_summary() {
    log "ShowSync development environment is ready!"
    echo
    echo "📊 Service URLs:"
    echo "  🚀 ShowSync API:     http://localhost:8080"
    echo "  📋 API Documentation: http://localhost:8080/swagger-ui.html"
    echo "  ❤️  Health Check:     http://localhost:8080/actuator/health"
    echo "  🐘 pgAdmin:          http://localhost:5050 (admin@showsync.local / admin)"
    echo "  🔴 Redis Insight:    http://localhost:8001"
    echo
    echo "📝 Development Commands:"
    echo "  View logs:           tail -f $LOG_FILE"
    echo "  Stop application:    ./scripts/stop-dev.sh"
    echo "  Run tests:           ./mvnw test"
    echo "  Database console:    docker-compose exec postgres psql -U showsync_user -d showsync_dev"
    echo "  Redis console:       docker-compose exec redis redis-cli"
    echo
    echo "🔧 Useful curl commands:"
    echo "  curl http://localhost:8080/actuator/health"
    echo "  curl http://localhost:8080/api/health/simple"
    echo "  curl http://localhost:8080/api/health/detailed"
    echo
}

show_logs_if_requested() {
    if [ "$SHOW_LOGS" = true ]; then
        log "Showing application logs (Ctrl+C to exit)..."
        tail -f "$LOG_FILE"
    fi
}

# =============================================================================
# MAIN SCRIPT EXECUTION
# =============================================================================

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --clean)
            CLEAN_BUILD=true
            shift
            ;;
        --logs)
            SHOW_LOGS=true
            shift
            ;;
        --test)
            RUN_TESTS=true
            shift
            ;;
        --skip-app)
            SKIP_APP=true
            shift
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            error "Unknown option: $1. Use --help for usage information."
            ;;
    esac
done

# Main execution flow
main() {
    log "Starting ShowSync development environment..."
    
    check_prerequisites
    create_directories
    start_infrastructure
    start_application
    run_tests
    show_summary
    show_logs_if_requested
}

# Run main function
main "$@" 