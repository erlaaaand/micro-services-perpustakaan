#!/bin/bash

# Build All Microservices Script
# Usage: ./build-all.sh

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Print colored message
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}"
}

# Print section header
print_header() {
    echo ""
    print_message "$BLUE" "=========================================="
    print_message "$BLUE" "$1"
    print_message "$BLUE" "=========================================="
}

# Build service function
build_service() {
    local service_name=$1
    local service_dir=$2
    
    print_header "Building $service_name"
    
    if [ ! -d "$service_dir" ]; then
        print_message "$RED" "❌ Directory $service_dir not found!"
        return 1
    fi
    
    if [ ! -f "$service_dir/pom.xml" ]; then
        print_message "$RED" "❌ pom.xml not found in $service_dir!"
        print_message "$YELLOW" "⚠️  Please create pom.xml first!"
        return 1
    fi
    
    cd "$service_dir"
    
    print_message "$YELLOW" "🔨 Cleaning previous builds..."
    mvn clean -q
    
    print_message "$YELLOW" "📦 Building JAR..."
    if mvn package -DskipTests -q; then
        if [ -f "target/*.jar" ]; then
            JAR_FILE=$(ls target/*.jar 2>/dev/null | head -n1)
            JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
            print_message "$GREEN" "✅ Build successful! JAR size: $JAR_SIZE"
        else
            print_message "$GREEN" "✅ Build successful!"
        fi
    else
        print_message "$RED" "❌ Build failed!"
        cd ..
        return 1
    fi
    
    cd ..
    return 0
}

# Verify prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"
    
    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        print_message "$GREEN" "✅ Java $JAVA_VERSION found"
    else
        print_message "$RED" "❌ Java not found! Please install Java 17+"
        exit 1
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n1 | awk '{print $3}')
        print_message "$GREEN" "✅ Maven $MVN_VERSION found"
    else
        print_message "$RED" "❌ Maven not found! Please install Maven 3.8+"
        exit 1
    fi
    
    # Check Docker
    if command -v docker &> /dev/null; then
        DOCKER_VERSION=$(docker --version | awk '{print $3}' | tr -d ',')
        print_message "$GREEN" "✅ Docker $DOCKER_VERSION found"
    else
        print_message "$YELLOW" "⚠️  Docker not found (optional for build)"
    fi
    
    # Check Docker Compose
    if command -v docker-compose &> /dev/null; then
        COMPOSE_VERSION=$(docker-compose --version | awk '{print $4}' | tr -d ',')
        print_message "$GREEN" "✅ Docker Compose $COMPOSE_VERSION found"
    else
        print_message "$YELLOW" "⚠️  Docker Compose not found (optional for build)"
    fi
}

# Main build process
main() {
    print_message "$BLUE" "╔════════════════════════════════════════╗"
    print_message "$BLUE" "║  Library Microservices Build Script   ║"
    print_message "$BLUE" "╚════════════════════════════════════════╝"
    
    # Check prerequisites
    check_prerequisites
    
    # Track build status
    TOTAL_SERVICES=6
    SUCCESSFUL_BUILDS=0
    FAILED_BUILDS=0
    
    # Build services in order
    SERVICES=(
        "Eureka Server:eureka-server"
        "API Gateway:api-gateway"
        "Service Anggota:service-anggota"
        "Service Buku:service-buku"
        "Service Peminjaman:service-peminjaman"
        "Service Pengembalian:service-pengembalian"
    )
    
    for service in "${SERVICES[@]}"; do
        IFS=':' read -r name dir <<< "$service"
        if build_service "$name" "$dir"; then
            ((SUCCESSFUL_BUILDS++))
        else
            ((FAILED_BUILDS++))
        fi
    done
    
    # Print summary
    print_header "Build Summary"
    print_message "$GREEN" "✅ Successful: $SUCCESSFUL_BUILDS/$TOTAL_SERVICES"
    
    if [ $FAILED_BUILDS -gt 0 ]; then
        print_message "$RED" "❌ Failed: $FAILED_BUILDS/$TOTAL_SERVICES"
        print_message "$YELLOW" ""
        print_message "$YELLOW" "⚠️  Some builds failed. Please check the errors above."
        exit 1
    else
        print_message "$GREEN" ""
        print_message "$GREEN" "🎉 All services built successfully!"
        print_message "$GREEN" ""
        print_message "$YELLOW" "Next steps:"
        print_message "$NC" "  1. Run: docker-compose up -d"
        print_message "$NC" "  2. Wait 2-3 minutes for services to start"
        print_message "$NC" "  3. Check: http://localhost:8761 (Eureka Dashboard)"
        exit 0
    fi
}

# Run main function
main