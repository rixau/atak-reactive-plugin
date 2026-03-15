#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
ROOT_DIR="$(dirname "$SCRIPT_DIR")"

echo "==> Setting up adb reverse port forwarding..."
if adb reverse tcp:5173 tcp:5173 2>/dev/null; then
    echo "    Port 5173 forwarded to device"
else
    echo "    WARNING: adb reverse failed. Is a device connected?"
    echo "    The WebView won't be able to reach the dev server."
    echo "    Connect a device and re-run, or use 'adb reverse tcp:5173 tcp:5173' manually."
fi

echo "==> Starting Vite dev server..."
cd "$ROOT_DIR/web"

if [ ! -d "node_modules" ]; then
    echo "==> Installing dependencies..."
    npm install
fi

exec npm run dev
