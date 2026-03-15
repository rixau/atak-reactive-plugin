#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

BUILD_TYPE="debug"
APK_PATTERN="*CivDebug*.apk"

if [[ "${1:-}" == "--release" ]]; then
    BUILD_TYPE="release"
    APK_PATTERN="*CivRelease*.apk"
fi

echo "==> Building plugin ($BUILD_TYPE)..."
"$SCRIPT_DIR/build.sh" ${1:-}

APK=$(find "$ROOT_DIR/app/build" -name "$APK_PATTERN" -type f | head -1)
if [ -z "$APK" ]; then
    echo "ERROR: No $BUILD_TYPE APK found"
    exit 1
fi

echo "==> Installing APK: $(basename "$APK")"
adb install -r "$APK"

echo "==> Restarting ATAK..."
adb shell am force-stop com.atakmap.app.civ
sleep 1
adb shell am start -n com.atakmap.app.civ/com.atakmap.app.ATAKActivity

echo "==> Done. ATAK is restarting with the updated plugin."
