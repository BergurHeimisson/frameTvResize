#!/bin/bash

# Samsung Pro Frame TV Image Resizer - GUI Run Script
# This script runs the Java GUI application using Java 25

JAR_FILE="target/FrameTvResizer.jar"
JAVA_25="/opt/homebrew/opt/openjdk/libexec/openjdk.jdk/Contents/Home/bin/java"

if [ ! -f "$JAR_FILE" ]; then
    echo "❌ JAR file not found. Please run ./build.sh first"
    exit 1
fi

# Check if Java 25 is available
if [ ! -f "$JAVA_25" ]; then
    echo "❌ Java 25 not found at $JAVA_25"
    echo "Install with: brew install openjdk"
    exit 1
fi

echo "Starting Frame TV Resizer GUI (Java 25)..."
"$JAVA_25" -jar "$JAR_FILE"
