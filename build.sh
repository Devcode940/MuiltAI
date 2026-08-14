#!/bin/bash

# Auto-build script for MultiAI Android App
# Supports both 'app' and 'appJava' modules with correct Java versions

set -e  # Exit on error

echo "🔨 MultiAI Auto-Build Script"
echo "============================"

# Detect OS and set JAVA_HOME paths
if [[ "$OSTYPE" == "linux-gnu"* ]]; then
    JAVA_17_PATH="/usr/lib/jvm/java-17-openjdk-amd64"
    JAVA_21_PATH="/usr/lib/jvm/java-21-openjdk-amd64"
elif [[ "$OSTYPE" == "darwin"* ]]; then
    JAVA_17_PATH="$(/usr/libexec/java_home -v 17)"
    JAVA_21_PATH="$(/usr/libexec/java_home -v 21)"
else
    echo "⚠️  Unknown OS, using default JAVA_HOME detection"
    JAVA_17_PATH=""
    JAVA_21_PATH=""
fi

# Function to check Java installation
check_java() {
    local version=$1
    local path=$2
    
    if [ -n "$path" ] && [ -d "$path" ]; then
        echo "✅ Java $version found at: $path"
        return 0
    else
        # Try to find via update-alternatives or java_home
        if command -v java &> /dev/null; then
            current_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2 | cut -d'.' -f1)
            if [ "$current_version" == "$version" ]; then
                echo "✅ Java $version is currently active"
                return 0
            fi
        fi
        echo "❌ Java $version not found. Please install it."
        return 1
    fi
}

# Function to build a module
build_module() {
    local module=$1
    local java_version=$2
    local java_path=$3
    
    echo ""
    echo "📦 Building module: $module"
    echo "   Required Java: $java_version"
    
    if [ -n "$java_path" ] && [ -d "$java_path" ]; then
        export JAVA_HOME="$java_path"
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
    
    # Verify Java version
    current_java=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "   Using Java: $current_java"
    
    # Clean and build
    echo "   Running: ./gradlew :$module:clean :$module:assembleDebug"
    ./gradlew :$module:clean :$module:assembleDebug --no-daemon
    
    if [ $? -eq 0 ]; then
        echo "✅ Build successful for $module"
        echo "   APK location: $module/build/outputs/apk/debug/"
    else
        echo "❌ Build failed for $module"
        return 1
    fi
}

# Check if gradlew exists
if [ ! -f "./gradlew" ]; then
    echo "❌ gradlew not found. Make sure you're in the project root directory."
    exit 1
fi

# Parse command line arguments
MODULE_TO_BUILD="${1:-all}"

case $MODULE_TO_BUILD in
    "app")
        check_java "17" "$JAVA_17_PATH" || exit 1
        build_module "app" "17" "$JAVA_17_PATH"
        ;;
    "appJava")
        check_java "21" "$JAVA_21_PATH" || exit 1
        build_module "appJava" "21" "$JAVA_21_PATH"
        ;;
    "all")
        echo "Building all modules..."
        check_java "17" "$JAVA_17_PATH" || exit 1
        check_java "21" "$JAVA_21_PATH" || exit 1
        
        build_module "app" "17" "$JAVA_17_PATH"
        build_module "appJava" "21" "$JAVA_21_PATH"
        
        echo ""
        echo "🎉 All builds completed successfully!"
        echo "APKs located in:"
        echo "  - app/build/outputs/apk/debug/"
        echo "  - appJava/build/outputs/apk/debug/"
        ;;
    *)
        echo "Usage: $0 [app|appJava|all]"
        echo "  app     - Build only the app module (Java 17)"
        echo "  appJava - Build only the appJava module (Java 21)"
        echo "  all     - Build both modules (default)"
        exit 1
        ;;
esac
