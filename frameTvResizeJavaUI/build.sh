#!/bin/bash

# Samsung Pro Frame TV Image Resizer - GUI Build Script
# This script compiles the Java GUI application using Java 25

set -e

# Set Java 25 as default for Maven
export JAVA_HOME="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"

echo "Building Frame TV Resizer GUI..."
echo "================================"
echo "Java version:"
java -version

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "❌ Maven is not installed"
    echo "Install Maven with: brew install maven"
    exit 1
fi

# Run Maven clean package
mvn clean package

echo ""
echo "✓ Build completed successfully!"
echo "Run the application with: ./run.sh"
