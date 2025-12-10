#!/bin/bash

# Enhanced Deploy All Services Script
# Usage: ./deploy-enhanced.sh [start|stop|restart|status|logs|health|build]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
COMPOSE_FILE="docker-compose.yml"
MAX_WAIT_TIME=180  # Maximum wait time for services to be healthy (3 minutes)

print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

print_header() {
    echo ""
    print_message "$BLUE" "=========================================="
    print_message "$BLUE" "$1"
    print_message "$BLUE" "=========================================="
}

# Check if Docker is running
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_message "$RED" "❌ Docker is not running!"
        print_message "$YELLOW" "Please start Docker Desktop and try again."
        exit 1
    fi
}

# Check if compose file exists
check_compose_file() {
    if [ ! -f "$COMPOSE_FILE" ]; then
        print_message "$RED" "❌ $COMPOSE_FILE not found!"
        print_message "$YELLOW" "Please ensure the file exists in the current directory."
        exit 1
    fi
}

# Build Docker images
build_images() {
    print_header "Building Docker Images"
    
    check_docker
    check_compose_file
    
    print_message "$YELLOW" "🔨 Building all Docker images..."
    print_message "$YELLOW" "This may take several minutes..."
    
    if docker-compose -f "$COMPOSE_FILE" build --no-cache; then
        print_message "$GREEN" "✅ All images built successfully!"
    else
        print_message "$RED" "❌ Build failed!"
        exit 1
    fi
}

# Start services
start_services() {
    print_header "Starting Services"
    
    check_docker
    check_compose_file
    
    print_message "$YELLOW" "🚀 Starting all services..."
    docker-compose -f "$COMPOSE_FILE" up -d
    
    print_message "$GREEN" ""
    print_message "$GREEN" "✅ Services started!"
    print_message "$YELLOW" ""
    print_message "$YELLOW" "⏳ Waiting for services to become healthy..."
    print_message "$YELLOW" "This may take 2-3 minutes..."
    
    # Wait for services
    wait_for_services
}

# Stop services
stop_services() {
    print_header "Stopping Services"
    
    check_docker
    check_compose_file
    
    print_message "$YELLOW" "🛑 Stopping all services..."
    docker-compose -f "$COMPOSE_FILE" down
    
    print_message "$GREEN" "✅ All services stopped!"
}

# Restart services
restart_services() {
    print_header "Restarting Services"
    
    stop_services
    sleep 3
    start_services
}

# Wait for services to be healthy
wait_for_services() {
    local elapsed=0
    local check_interval=10
    
    while [ $elapsed -lt $MAX_WAIT_TIME ]; do
        print_message "$YELLOW" "Checking services... (${elapsed}s/${MAX_WAIT_TIME}s)"
        
        # Check Eureka first (most important)
        if curl -f -s http://localhost:8761/actuator/health > /dev/null 2>&1; then
            print_message "$GREEN" "✅ Eureka Server is healthy!"
            
            # Check other services
            local all_healthy=true
            local services=(
                "API Gateway:8080"
                "Service Anggota:8081"
                "Service Buku:8082"
                "Service Peminjaman:8083"
                "Service Pengembalian:8084"
            )
            
            for service in "${services[@]}"; do
                IFS=':' read -r name port <<< "$service"
                if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
                    print_message "$GREEN" "✅ $name is healthy"
                else
                    print_message "$YELLOW" "⏳ $name is still starting..."
                    all_healthy=false
                fi
            done
            
            if [ "$all_healthy" = true ]; then
                print_message "$GREEN" ""
                print_message "$GREEN" "🎉 All services are healthy and ready!"
                show_access_urls
                return 0
            fi
        else
            print_message "$YELLOW" "⏳ Eureka Server is still starting..."
        fi
        
        sleep $check_interval
        elapsed=$((elapsed + check_interval))
    done
    
    print_message "$RED" "⚠️  Timeout waiting for services to be healthy"
    print_message "$YELLOW" "Some services may still be starting. Check with: ./deploy-enhanced.sh status"
}

# Show access URLs
show_access_urls() {
    print_message "$BLUE" ""
    print_message "$BLUE" "╔════════════════════════════════════════╗"
    print_message "$BLUE" "║          Access Information            ║"
    print_message "$BLUE" "╚════════════════════════════════════════╝"
    
    print_message "$YELLOW" ""
    print_message "$YELLOW" "🌐 Service URLs:"
    print_message "$NC" "  Eureka Dashboard:    http://localhost:8761"
    print_message "$NC" "  API Gateway:         http://localhost:8080"
    
    print_message "$YELLOW" ""
    print_message "$YELLOW" "📊 Monitoring Tools:"
    print_message "$NC" "  Grafana:             http://localhost:3000 (admin/admin)"
    print_message "$NC" "  Prometheus:          http://localhost:9090"
    print_message "$NC" "  Kibana:              http://localhost:5601"
    print_message "$NC" "  Zipkin:              http://localhost:9411"
    
    print_message "$YELLOW" ""
    print_message "$YELLOW" "🔍 Health Check URLs:"
    print_message "$NC" "  Eureka:              http://localhost:8761/actuator/health"
    print_message "$NC" "  API Gateway:         http://localhost:8080/actuator/health"
    print_message "$NC" "  Service Anggota:     http://localhost:8081/actuator/health"
    print_message "$NC" "  Service Buku:        http://localhost:8082/actuator/health"
    print_message "$NC" "  Service Peminjaman:  http://localhost:8083/actuator/health"
    print_message "$NC" "  Service Pengembalian:http://localhost:8084/actuator/health"
}

# Show status
show_status() {
    print_header "Service Status"
    
    check_docker
    check_compose_file
    
    print_message "$YELLOW" "Container Status:"
    docker-compose -f "$COMPOSE_FILE" ps
    
    show_access_urls
}

# Show logs
show_logs() {
    print_header "Service Logs"
    
    check_docker
    check_compose_file
    
    if [ -z "$2" ]; then
        print_message "$YELLOW" "Showing logs for all services (Ctrl+C to exit)..."
        docker-compose -f "$COMPOSE_FILE" logs -f --tail=100
    else
        print_message "$YELLOW" "Showing logs for $2 (Ctrl+C to exit)..."
        docker-compose -f "$COMPOSE_FILE" logs -f --tail=100 "$2"
    fi
}

# Health check
health_check() {
    print_header "Running Health Checks"
    
    check_docker
    
    SERVICES=(
        "Eureka Server:8761"
        "API Gateway:8080"
        "Service Anggota:8081"
        "Service Buku:8082"
        "Service Peminjaman:8083"
        "Service Pengembalian:8084"
    )
    
    print_message "$YELLOW" "Checking service health..."
    echo ""
    
    HEALTHY=0
    UNHEALTHY=0
    
    for service in "${SERVICES[@]}"; do
        IFS=':' read -r name port <<< "$service"
        
        if curl -f -s "http://localhost:$port/actuator/health" > /dev/null 2>&1; then
            print_message "$GREEN" "✅ $name is healthy"
            ((HEALTHY++))
        else
            print_message "$RED" "❌ $name is not responding"
            ((UNHEALTHY++))
        fi
    done
    
    # Check monitoring tools
    print_message "$YELLOW" ""
    print_message "$YELLOW" "Checking monitoring tools..."
    
    if curl -f -s "http://localhost:9411/health" > /dev/null 2>&1; then
        print_message "$GREEN" "✅ Zipkin is healthy"
    else
        print_message "$RED" "❌ Zipkin is not responding"
    fi
    
    if curl -f -s "http://localhost:9090/-/healthy" > /dev/null 2>&1; then
        print_message "$GREEN" "✅ Prometheus is healthy"
    else
        print_message "$RED" "❌ Prometheus is not responding"
    fi
    
    echo ""
    print_message "$BLUE" "Summary: $HEALTHY healthy, $UNHEALTHY unhealthy"
    
    if [ $UNHEALTHY -gt 0 ]; then
        print_message "$YELLOW" ""
        print_message "$YELLOW" "⚠️  Some services are not healthy."
        print_message "$YELLOW" "Wait a few more minutes and try again, or check logs with:"
        print_message "$NC" "  ./deploy-enhanced.sh logs [service-name]"
        return 1
    else
        print_message "$GREEN" ""
        print_message "$GREEN" "🎉 All services are healthy!"
        return 0
    fi
}

# Show help
show_help() {
    print_message "$BLUE" "╔════════════════════════════════════════╗"
    print_message "$BLUE" "║    Library Microservices Deploy       ║"
    print_message "$BLUE" "║         Enhanced Version 2.0           ║"
    print_message "$BLUE" "╚════════════════════════════════════════╝"
    echo ""
    print_message "$YELLOW" "Usage: ./deploy-enhanced.sh [command]"
    echo ""
    print_message "$NC" "Commands:"
    print_message "$GREEN" "  build        Build all Docker images"
    print_message "$GREEN" "  start        Start all services"
    print_message "$GREEN" "  stop         Stop all services"
    print_message "$GREEN" "  restart      Restart all services"
    print_message "$GREEN" "  status       Show service status"
    print_message "$GREEN" "  logs [name]  Show logs (optionally for specific service)"
    print_message "$GREEN" "  health       Run health checks on all services"
    print_message "$GREEN" "  help         Show this help message"
    echo ""
    print_message "$NC" "Examples:"
    print_message "$YELLOW" "  ./deploy-enhanced.sh build"
    print_message "$YELLOW" "  ./deploy-enhanced.sh start"
    print_message "$YELLOW" "  ./deploy-enhanced.sh logs eureka-server"
    print_message "$YELLOW" "  ./deploy-enhanced.sh health"
}

# Main function
main() {
    case "${1:-help}" in
        build)
            build_images
            ;;
        start)
            start_services
            ;;
        stop)
            stop_services
            ;;
        restart)
            restart_services
            ;;
        status)
            show_status
            ;;
        logs)
            show_logs "$@"
            ;;
        health)
            health_check
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_message "$RED" "❌ Unknown command: $1"
            echo ""
            show_help
            exit 1
            ;;
    esac
}

# Run main
main "$@"