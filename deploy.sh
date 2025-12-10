#!/bin/bash

# Deploy All Services Script
# Usage: ./deploy.sh [start|stop|restart|status|logs]

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
COMPOSE_FILE="docker-compose.yml"
if [ -f "docker-compose-fixed.yml" ]; then
    COMPOSE_FILE="docker-compose-fixed.yml"
fi

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
        print_message "$YELLOW" "Please start Docker and try again."
        exit 1
    fi
}

# Start services
start_services() {
    print_header "Starting Services"
    
    check_docker
    
    print_message "$YELLOW" "🚀 Starting all services..."
    docker-compose -f "$COMPOSE_FILE" up -d
    
    print_message "$GREEN" ""
    print_message "$GREEN" "✅ Services started!"
    print_message "$YELLOW" ""
    print_message "$YELLOW" "⏳ Please wait 2-3 minutes for all services to fully start..."
    print_message "$NC" ""
    print_message "$NC" "You can check the status with: ./deploy.sh status"
    print_message "$NC" "Or view logs with: ./deploy.sh logs"
}

# Stop services
stop_services() {
    print_header "Stopping Services"
    
    check_docker
    
    print_message "$YELLOW" "🛑 Stopping all services..."
    docker-compose -f "$COMPOSE_FILE" down
    
    print_message "$GREEN" "✅ All services stopped!"
}

# Restart services
restart_services() {
    print_header "Restarting Services"
    
    stop_services
    sleep 2
    start_services
}

# Show status
show_status() {
    print_header "Service Status"
    
    check_docker
    
    docker-compose -f "$COMPOSE_FILE" ps
    
    echo ""
    print_message "$YELLOW" "Health Check URLs:"
    print_message "$NC" "  Eureka Server:       http://localhost:8761/actuator/health"
    print_message "$NC" "  API Gateway:         http://localhost:8080/actuator/health"
    print_message "$NC" "  Service Anggota:     http://localhost:8081/actuator/health"
    print_message "$NC" "  Service Buku:        http://localhost:8082/actuator/health"
    print_message "$NC" "  Service Peminjaman:  http://localhost:8083/actuator/health"
    print_message "$NC" "  Service Pengembalian: http://localhost:8084/actuator/health"
    
    echo ""
    print_message "$YELLOW" "Web Interfaces:"
    print_message "$NC" "  Eureka Dashboard:    http://localhost:8761"
    print_message "$NC" "  Grafana:             http://localhost:3000 (admin/admin)"
    print_message "$NC" "  Prometheus:          http://localhost:9090"
    print_message "$NC" "  Kibana:              http://localhost:5601"
    print_message "$NC" "  Zipkin:              http://localhost:9411"
}

# Show logs
show_logs() {
    print_header "Service Logs"
    
    check_docker
    
    if [ -z "$2" ]; then
        print_message "$YELLOW" "Showing logs for all services (Ctrl+C to exit)..."
        docker-compose -f "$COMPOSE_FILE" logs -f
    else
        print_message "$YELLOW" "Showing logs for $2 (Ctrl+C to exit)..."
        docker-compose -f "$COMPOSE_FILE" logs -f "$2"
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
    
    echo ""
    print_message "$BLUE" "Summary: $HEALTHY healthy, $UNHEALTHY unhealthy"
    
    if [ $UNHEALTHY -gt 0 ]; then
        print_message "$YELLOW" ""
        print_message "$YELLOW" "⚠️  Some services are not healthy."
        print_message "$YELLOW" "Wait a few more minutes and try again."
        print_message "$YELLOW" "Or check logs with: ./deploy.sh logs"
    fi
}

# Show help
show_help() {
    print_message "$BLUE" "╔════════════════════════════════════════╗"
    print_message "$BLUE" "║    Library Microservices Deploy       ║"
    print_message "$BLUE" "╚════════════════════════════════════════╝"
    echo ""
    print_message "$YELLOW" "Usage: ./deploy.sh [command]"
    echo ""
    print_message "$NC" "Commands:"
    print_message "$GREEN" "  start        Start all services"
    print_message "$GREEN" "  stop         Stop all services"
    print_message "$GREEN" "  restart      Restart all services"
    print_message "$GREEN" "  status       Show service status"
    print_message "$GREEN" "  logs [name]  Show logs (optionally for specific service)"
    print_message "$GREEN" "  health       Run health checks on all services"
    print_message "$GREEN" "  help         Show this help message"
    echo ""
    print_message "$NC" "Examples:"
    print_message "$YELLOW" "  ./deploy.sh start"
    print_message "$YELLOW" "  ./deploy.sh logs eureka-server"
    print_message "$YELLOW" "  ./deploy.sh health"
}

# Main function
main() {
    case "${1:-help}" in
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