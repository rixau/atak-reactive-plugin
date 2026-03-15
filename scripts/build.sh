#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

BUILD_TYPE="debug"
GRADLE_TASK="assembleCivDebug"
APK_PATTERN="*CivDebug*.apk"

if [[ "${1:-}" == "--release" ]]; then
    BUILD_TYPE="release"
    GRADLE_TASK="assembleCivRelease"
    APK_PATTERN="*CivRelease*.apk"
fi

echo "==> Building ($BUILD_TYPE)..."

# Ensure web dependencies are installed (gradle buildWeb task needs them)
cd "$ROOT_DIR/web"
if [ ! -d "node_modules" ]; then
    echo "==> Installing npm dependencies..."
    npm install
fi

# Gradle handles: npm build → copy assets → compile Android
cd "$ROOT_DIR"
./gradlew "$GRADLE_TASK"

APK=$(find app/build -name "$APK_PATTERN" -type f | head -1)
if [ -n "$APK" ]; then
    echo "==> Build complete: $APK"
else
    echo "==> Build complete (check app/build/outputs/)"
fi
