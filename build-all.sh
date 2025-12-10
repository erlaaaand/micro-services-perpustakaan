#!/bin/bash

# Enhanced Build All Microservices Script
# Usage: ./build-all-enhanced.sh

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Build log file
BUILD_LOG="build-$(date +%Y%m%d-%H%M%S).log"

# Print colored message
print_message() {
    local color=$1
    local message=$2
    echo -e "${color}${message}${NC}" | tee -a "$BUILD_LOG"
}

# Print section header
print_header() {
    echo "" | tee -a "$BUILD_LOG"
    print_message "$BLUE" "=========================================="
    print_message "$BLUE" "$1"
    print_message "$BLUE" "=========================================="
}

# Build service function
build_service() {
    local service_name=$1
    local service_dir=$2
    
    print_header "Building $service_name"
    
    # Check if directory exists
    if [ ! -d "$service_dir" ]; then
        print_message "$RED" "❌ Directory $service_dir not found!"
        return 1
    fi
    
    # Check if pom.xml exists
    if [ ! -f "$service_dir/pom.xml" ]; then
        print_message "$RED" "❌ pom.xml not found in $service_dir!"
        return 1
    fi
    
    cd "$service_dir"
    
    # Clean
    print_message "$YELLOW" "🧹 Cleaning previous builds..."
    if ! mvn clean >> "../$BUILD_LOG" 2>&1; then
        print_message "$RED" "❌ Clean failed!"
        cd ..
        return 1
    fi
    
    # Build
    print_message "$YELLOW" "🔨 Building JAR..."
    if mvn package -DskipTests >> "../$BUILD_LOG" 2>&1; then
        # Verify JAR was created
        JAR_FILE=$(find target -name "*.jar" -not -name "*-sources.jar" -not -name "*-javadoc.jar" 2>/dev/null | head -n1)
        if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
            JAR_SIZE=$(du -h "$JAR_FILE" | cut -f1)
            print_message "$GREEN" "✅ Build successful! JAR: $JAR_SIZE"
            cd ..
            return 0
        else
            print_message "$RED" "❌ Build completed but JAR not found!"
            cd ..
            return 1
        fi
    else
        print_message "$RED" "❌ Build failed! Check log: $BUILD_LOG"
        cd ..
        return 1
    fi
}

# Verify prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"
    
    local all_good=true
    
    # Check Java
    if command -v java &> /dev/null; then
        JAVA_VERSION=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
        print_message "$GREEN" "✅ Java $JAVA_VERSION found"
    else
        print_message "$RED" "❌ Java not found! Please install Java 17+"
        all_good=false
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        MVN_VERSION=$(mvn -version | head -n1 | awk '{print $3}')
        print_message "$GREEN" "✅ Maven $MVN_VERSION found"
    else
        print_message "$RED" "❌ Maven not found! Please install Maven 3.8+"
        all_good=false
    fi
    
    # Check Docker
    if command -v docker &> /dev/null; then
        if docker info &> /dev/null; then
            DOCKER_VERSION=$(docker --version | awk '{print $3}' | tr -d ',')
            print_message "$GREEN" "✅ Docker $DOCKER_VERSION found and running"
        else
            print_message "$YELLOW" "⚠️  Docker found but not running"
        fi
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
    
    if [ "$all_good" = false ]; then
        print_message "$RED" "Missing required prerequisites!"
        exit 1
    fi
}

# Verify directory structure
verify_structure() {
    print_header "Verifying Directory Structure"
    
    local required_dirs=(
        "eureka-server"
        "api-gateway"
        "service-anggota"
        "service-buku"
        "service-peminjaman"
        "service-pengembalian"
    )
    
    local missing=false
    
    for dir in "${required_dirs[@]}"; do
        if [ -d "$dir" ]; then
            print_message "$GREEN" "✅ Found: $dir"
        else
            print_message "$RED" "❌ Missing: $dir"
            missing=true
        fi
    done
    
    if [ "$missing" = true ]; then
        print_message "$RED" "Missing required directories!"
        exit 1
    fi
}

# Main build process
main() {
    print_message "$BLUE" "╔════════════════════════════════════════╗"
    print_message "$BLUE" "║  Library Microservices Build Script   ║"
    print_message "$BLUE" "║         Enhanced Version 2.0           ║"
    print_message "$BLUE" "╚════════════════════════════════════════╝"
    
    print_message "$YELLOW" "Build log: $BUILD_LOG"
    
    # Check prerequisites
    check_prerequisites
    
    # Verify structure
    verify_structure
    
    # Track build status
    TOTAL_SERVICES=6
    SUCCESSFUL_BUILDS=0
    FAILED_BUILDS=0
    declare -a FAILED_SERVICES
    
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
            FAILED_SERVICES+=("$name")
        fi
    done
    
    # Print summary
    print_header "Build Summary"
    print_message "$GREEN" "✅ Successful: $SUCCESSFUL_BUILDS/$TOTAL_SERVICES"
    
    if [ $FAILED_BUILDS -gt 0 ]; then
        print_message "$RED" "❌ Failed: $FAILED_BUILDS/$TOTAL_SERVICES"
        print_message "$RED" "Failed services:"
        for service in "${FAILED_SERVICES[@]}"; do
            print_message "$RED" "  - $service"
        done
        print_message "$YELLOW" ""
        print_message "$YELLOW" "⚠️  Check build log: $BUILD_LOG"
        exit 1
    else
        print_message "$GREEN" ""
        print_message "$GREEN" "🎉 All services built successfully!"
        print_message "$GREEN" ""
        print_message "$YELLOW" "Next steps:"
        print_message "$NC" "  1. Build Docker images: docker-compose -f docker-compose-fixed.yml build"
        print_message "$NC" "  2. Start services: docker-compose -f docker-compose-fixed.yml up -d"
        print_message "$NC" "  3. Check status: ./deploy.sh status"
        print_message "$NC" "  4. View Eureka: http://localhost:8761"
        exit 0
    fi
}

# Run main function
main