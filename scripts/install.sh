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

echo "==> Restarting ATAK..."
adb shell am force-stop com.atakmap.app.civ
sleep 1
adb shell am start -n com.atakmap.app.civ/com.atakmap.app.ATAKActivity

echo "==> Done. ATAK is restarting with the updated plugin."
