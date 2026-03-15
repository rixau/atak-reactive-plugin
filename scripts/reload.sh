#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "==> Building plugin..."
"$SCRIPT_DIR/build.sh"

APK=$(find "$ROOT_DIR/app/build" -name "*CivDebug*.apk" -type f | head -1)
if [ -z "$APK" ]; then
    echo "ERROR: No debug APK found"
    exit 1
fi

echo "==> Installing APK: $(basename "$APK")"
adb install -r "$APK"

echo "==> Sending reload broadcast (no ATAK restart)..."
adb shell am broadcast -a com.atakmap.android.reactive.DEV_RELOAD

echo "==> Done. Plugin should reload momentarily."
echo "    If the plugin doesn't work correctly, run install.sh for a full restart."
