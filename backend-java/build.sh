#!/usr/bin/env bash
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

mkdir -p bin

# Compile Java source files
javac -d bin $(find src/main/java -name "*.java")

# Copy classpath resources
if [ -d "src/main/resources" ]; then
    cp -r src/main/resources/* bin/ 2>/dev/null || true
fi

echo "Compilation successful. Classes and resources built into bin/."
