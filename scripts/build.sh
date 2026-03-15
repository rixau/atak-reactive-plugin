#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "==> Building web assets..."
cd "$ROOT_DIR/web"

if [ ! -d "node_modules" ]; then
    echo "==> Installing npm dependencies..."
    npm install
fi

npm run build

echo "==> Copying web assets to plugin..."
mkdir -p "$ROOT_DIR/app/src/main/assets/web"
cp -r dist/* "$ROOT_DIR/app/src/main/assets/web/"

echo "==> Building Android plugin..."
cd "$ROOT_DIR"
./gradlew assembleCivDebug

APK=$(find app/build -name "*CivDebug*.apk" -type f | head -1)
if [ -n "$APK" ]; then
    echo "==> Build complete: $APK"
else
    echo "==> Build complete (APK path not found, check app/build/outputs/)"
fi
