# ATAK Reactive Plugin

An ATAK plugin template that uses a **React + TypeScript** UI rendered in a WebView, with **instant hot-reload** during development. No more rebuilding and restarting ATAK for every UI change.

## How It Works

```
Dev Machine                          Android Device
┌──────────────┐    adb reverse     ┌──────────────────────┐
│ Vite Dev      │◄──────────────────│ ATAK                 │
│ Server :5173  │   tcp:5173        │  ┌──────────────────┐ │
└──────────────┘                    │  │ Plugin           │ │
                                    │  │  ┌────────────┐  │ │
                                    │  │  │ WebView    │  │ │
                                    │  │  │ (React UI) │  │ │
                                    │  │  └─────┬──────┘  │ │
                                    │  │   @JavascriptInterface
                                    │  │  ┌─────▼──────┐  │ │
                                    │  │  │ AtakBridge  │  │ │
                                    │  │  │ (Java)      │  │ │
                                    │  │  └────────────┘  │ │
                                    │  └──────────────────┘ │
                                    └──────────────────────┘
```

- **Dev mode**: WebView tries `http://localhost:5173` (Vite dev server via ADB port forwarding) for instant HMR. If the dev server isn't running, it automatically falls back to bundled assets.
- **Prod mode**: Web assets are bundled into the APK and served via `WebViewAssetLoader`. No dev server, no network dependency.
- **JS Bridge**: A typed `@JavascriptInterface` bridge connects React to ATAK — markers, map events, GPS, preferences.

Web assets are bundled into **every** build (debug and release), so the plugin always works standalone. The dev server is purely optional for faster iteration.

## Quick Start

### Prerequisites

- JDK 17
- Android SDK with compileSdk 36
- TAK dev kit credentials (or local `atak-gradle-takdev.jar`)
- Node.js 18+
- An Android device or emulator running ATAK

### Setup

1. Clone the repo:
   ```bash
   git clone https://github.com/rixau/atak-reactive-plugin.git
   cd atak-reactive-plugin
   ```

2. Configure TAK dev kit credentials in `local.properties`:
   ```properties
   takrepo.url=https://your-tak-repo-url
   takrepo.user=your-username
   takrepo.password=your-password
   ```
   Or point to a local JAR:
   ```properties
   takdev.plugin=/path/to/atak-gradle-takdev.jar
   ```

3. Install web dependencies:
   ```bash
   cd web && npm install && cd ..
   ```

## Development Workflows

### UI Development (Hot Reload)

Edit React code and see changes instantly in ATAK — no rebuild needed.

```bash
# Terminal 1: Start Vite dev server + ADB port forwarding
./scripts/dev.sh

# Terminal 2 (one-time): Build and install the plugin APK
./scripts/install.sh
```

Open the plugin in ATAK. Any changes to files in `web/src/` will hot-reload in the WebView immediately.

### Java Development (Plugin Reload)

For changes to the Java bridge or plugin logic:

```bash
# Rebuild, install, and reload plugin without restarting ATAK
./scripts/reload.sh
```

This uses an experimental mechanism that unloads and reloads the plugin via `AtakPluginRegistry`. If the plugin behaves unexpectedly after a reload, use `install.sh` for a clean ATAK restart.

### Release Build

```bash
# Build a release APK (bundled web assets, ProGuard, no dev features)
./scripts/build.sh --release
./scripts/install.sh --release
```

### Debug Build (Standalone)

```bash
# Debug APK with bundled assets — works without dev server
./scripts/build.sh
```

### Browser-Only Development

You can iterate on pure UI without a device at all. The mock bridge provides fake data:

```bash
cd web && npm run dev
```

Open `http://localhost:5173` in a browser. The badge will show "MOCK" and bridge calls will log to the console.

## Bridge API

### Functions

```tsx
import { getSelfLocation, getMapCenter, addMarker, updateMarker, removeMarker, panTo, getPreference } from './atak';

// GPS position
const loc = getSelfLocation();
// { lat: 38.89, lng: -77.03, alt: 15.0, bearing: 45, speed: 0 }

// Map center
const center = getMapCenter();

// Markers
const uid = addMarker({ lat: 38.89, lng: -77.03, title: "My Pin" });
updateMarker(uid, { title: "Updated Pin" });
removeMarker(uid);

// Navigation
panTo(38.89, -77.03, 15);

// Preferences
const value = getPreference("some.atak.pref.key");
```

### React Hooks

```tsx
import { useSelfLocation, useMapEvent } from './atak';

function MyPlugin() {
  const location = useSelfLocation();
  const lastClick = useMapEvent('mapClick');
  const selected = useMapEvent('itemSelected');

  return (
    <div>
      <p>Position: {location?.lat}, {location?.lng}</p>
      <p>Last click: {lastClick?.lat}, {lastClick?.lng}</p>
      <p>Selected: {selected?.title}</p>
    </div>
  );
}
```

### Events

| Event | Payload | Description |
|-------|---------|-------------|
| `selfLocationChanged` | `{ lat, lng, alt, bearing, speed }` | GPS position updated |
| `mapClick` | `{ lat, lng }` | User tapped the map |
| `mapLongPress` | `{ lat, lng }` | User long-pressed the map |
| `itemSelected` | `{ uid, type, title, lat, lng }` | User tapped a map item |

## Project Structure

```
atak-reactive-plugin/
├── app/                              # Android plugin
│   ├── build.gradle
│   └── src/main/
│       ├── java/.../reactive/
│       │   ├── plugin/
│       │   │   ├── ReactivePlugin.java        # IPlugin entry point
│       │   │   └── ReactivePluginTool.java    # Toolbar button
│       │   ├── ReactiveMapComponent.java      # Component registration
│       │   ├── ReactiveWebViewReceiver.java   # WebView host + lifecycle
│       │   ├── DevReloadReceiver.java         # Debug-only plugin reload
│       │   └── bridge/
│       │       ├── AtakBridge.java            # @JavascriptInterface methods
│       │       ├── BridgeEventEmitter.java    # Java → JS event push
│       │       └── MarkerManager.java         # Marker CRUD
│       ├── assets/plugin.xml
│       └── AndroidManifest.xml
│
├── web/                              # React + Vite + TypeScript
│   ├── src/
│   │   ├── atak/                     # TypeScript bridge SDK
│   │   │   ├── types.ts             # Type definitions
│   │   │   ├── bridge.ts            # Typed bridge wrappers
│   │   │   ├── events.ts            # Event emitter (Java → JS)
│   │   │   ├── hooks.ts             # React hooks
│   │   │   ├── mock.ts             # Mock bridge for browser dev
│   │   │   └── index.ts            # Public API
│   │   ├── components/              # Demo components
│   │   └── App.tsx                  # Demo app
│   └── vite.config.ts
│
└── scripts/
    ├── dev.sh                        # Start Vite + adb reverse
    ├── build.sh                      # Build web + gradle
    ├── install.sh                    # Build + install + restart ATAK
    └── reload.sh                     # Build + install + reload (no restart)
```

## Extending the Bridge

To expose additional ATAK functionality to your React UI:

1. Add a `@JavascriptInterface` method to `AtakBridge.java`
2. Add a typed wrapper in `web/src/atak/bridge.ts`
3. Export it from `web/src/atak/index.ts`

For push events (Java → JS), add a listener in `BridgeEventEmitter.java` and emit via the `emit()` method. Subscribe from the TypeScript side using `on()` or `useMapEvent()`.

## Compatibility

- ATAK 5.5.x / 5.6.x
- Android 5.0+ (API 21+)
- Java 17
- Node.js 18+

## License

Public domain. Use as a starting point for your own ATAK plugins.
